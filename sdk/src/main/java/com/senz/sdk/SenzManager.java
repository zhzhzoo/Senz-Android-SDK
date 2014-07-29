package com.senz.sdk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.os.Message;
import android.os.Messenger;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.Looper;
import android.os.IBinder;
import android.os.Build;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import com.senz.sdk.Senz;
import com.senz.sdk.service.SenzService;
import com.senz.sdk.exception.SenzException;
import com.senz.sdk.utils.L;
import com.senz.sdk.filter.Filter;

public class SenzManager {
    private Context mContext;
    private TelepathyCallback mTelepathyCallback;
    private ErrorHandler mErrorHandler;
    private Messenger mServiceMessenger;
    private Messenger mIncomingMessenger;
    private ServiceConnection mServiceConnection;
    private boolean mStarted;
    private HashMap<Senz, Long> mLastSeen;
    private Filter mFilter;
    private ArrayList<Senz> mLastDiscovered;

    public SenzManager(Context context) {
        this.mContext = context;
        this.mServiceConnection = new InternalServiceConnection();
        this.mIncomingMessenger = new Messenger(new IncomingHandler());
        this.mLastDiscovered = new ArrayList<Senz>();
    }

    public boolean checkPermissions() {
        PackageManager pm = this.mContext.getPackageManager();
        int bluetoothPermission = pm.checkPermission("android.permission.BLUETOOTH", this.mContext.getPackageName());
        int bluetoothAdminPermission = pm.checkPermission("android.permission.BLUETOOTH_ADMIN", this.mContext.getPackageName());
        
        L.i("bluetooth:" + bluetoothPermission + " bluetooth_admin:" + bluetoothAdminPermission);
        return bluetoothPermission == 0 && bluetoothAdminPermission == 0;
    }

    public boolean checkService() {
        PackageManager pm = this.mContext.getPackageManager();
        Intent intent = new Intent(this.mContext, SenzService.class);
        List resolveInfo = pm.queryIntentServices(intent, 65536);

        return resolveInfo.size() > 0;
    }

    public boolean hasBluetooth() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le") && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public boolean bluetoothEnabled() {
        if (!checkPermissions()) {
            L.e("AndroidManifest.xml does not contain android.permission.BLUETOOTH or android.permission.BLUETOOTH_ADMIN permissions.");
            return false;
        }
        if (!checkService()) {
            L.e("SenzService may be not declared in AndroidManifest.xml.");
            return false;
        }
        BluetoothManager bluetoothManager = (BluetoothManager) this.mContext.getSystemService("bluetooth");
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }

    public void init() throws SenzException {
        L.i("initializing senz manager");

        if (!this.hasBluetooth())
            throw new SenzException("No Bluetooth!");
        if (!this.bluetoothEnabled())
            throw new SenzException("Bluetooth not enabled!");

        if (isConnected())
            return;

        boolean bound = this.mContext.bindService(new Intent(this.mContext, SenzService.class),
                                                  this.mServiceConnection,
                                                  Context.BIND_AUTO_CREATE);
        if (!bound) {
            L.e("Could not bind service: make sure that com.senz.sdk.service.SenzService is declared in AndroidManifest.xml");
            throw new SenzException("Can't bind service");
        }
    }

    public void end() {
        if (!isConnected())
            return;
        this.mContext.unbindService(this.mServiceConnection);
        this.mServiceMessenger = null;
    }

    public boolean isConnected() {
        return this.mServiceMessenger != null;
    }

    public void startTelepathy(TelepathyCallback cb) throws RemoteException, SenzException {
        if (cb == null)
            throw new NullPointerException();
        this.mTelepathyCallback = cb;
        this.mStarted = true;

        if (isConnected())
            internalStartTelepathy();
    }

    private void internalStartTelepathy() {
        Message startTelepathyMsg = Message.obtain(null, SenzService.MSG_START_TELEPATHY);
        startTelepathyMsg.replyTo = this.mIncomingMessenger;
        try {
            this.mServiceMessenger.send(startTelepathyMsg);
        }
        catch (RemoteException e) {
            L.e("Error sending start telepathy message: ", e);
        }
    }

    public void stopTelepathy() throws RemoteException {
        this.mStarted = false;
        if (!isConnected())
            return; // Stopping, OK to ignore no connection
        Message stopTelepathyMsg = Message.obtain(null, SenzService.MSG_STOP_TELEPATHY);
        stopTelepathyMsg.replyTo = this.mIncomingMessenger;
        try {
            this.mServiceMessenger.send(stopTelepathyMsg);
        }
        catch (RemoteException e) {
            L.e("Error sending stop telepathy message: ", e);
        }
    }

    public void setErrorHandler(ErrorHandler h) {
        if (h == null)
            throw new NullPointerException();
        this.mErrorHandler = h;
    }

    public List<Senz> getLastDiscoveredSenzes() {
        return this.mLastDiscovered;
    }

    private void reportUnseenAndUpdateTime(ArrayList<Senz> senzes) {
        long now = System.currentTimeMillis();
        ArrayList<Senz> unseens = new ArrayList<Senz>();
        for (Senz senz : senzes)
            this.mLastSeen.put(senz, now);
        for (Entry<Senz, Long> entry : this.mLastSeen.entrySet())
            if (entry.getValue() - now > TimeUnit.SECONDS.toMillis(20))
                unseens.add(entry.getKey());
        for (Senz senz : unseens)
            this.mLastSeen.remove(senz);
        this.mTelepathyCallback.onLeave(unseens);
    }

    private void respondSenz(final ArrayList<Senz> senzes) {
        this.mLastDiscovered = senzes;
        this.mTelepathyCallback.onDiscover(senzes);
    }

    private void respondError(String reason) {
        if (mErrorHandler != null)
            mErrorHandler.onError(new SenzException(reason));
        else
            L.d("Unhandled error: " + reason);
    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SenzService.MSG_TELEPATHY_RESPONSE:
                    ArrayList<Senz> senzes = msg.getData().getParcelableArrayList("senzes");
                    SenzManager.this.reportUnseenAndUpdateTime(senzes);
                    SenzManager.this.respondSenz(SenzManager.this.mFilter.filter(senzes));
                    break;
                case SenzService.MSG_ERROR_RESPONSE:
                    String reason = msg.getData().getString("reason");
                    SenzManager.this.respondError(reason);
                    break;
                default:
                    L.d("Unknown message: " + msg);
                    break;
            }
        }
    }

    private class InternalServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SenzManager.this.mServiceMessenger = new Messenger(service);
            if (SenzManager.this.mStarted)
                internalStartTelepathy();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            L.e("Service disconnected... " + name);
            SenzManager.this.mServiceMessenger = null;
        }
    }

    public interface TelepathyCallback {
        public void onDiscover(List<Senz> senzes);
        public void onLeave(List<Senz> senzes);
    }

    public interface ErrorHandler {
        public void onError(SenzException e);
    };
}
