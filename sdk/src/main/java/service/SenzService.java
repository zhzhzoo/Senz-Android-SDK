package com.senz.sdk.service;

import android.app.AlarmManager;
import android.add.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.os.Message;
import android.os.Messenger;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.Looper;
import android.os.IBinder;
import android.os.Intent;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import com.senz.sdk.service.TelepathyPeriod;
import com.senz.sdk.utils.L;

public class SenzService extends Service {

    public static final int MSG_START_TELEPATHY = 1;
    public static final int MSG_STOP_TELEPATHY = 2;
    public static final int MSG_TELEPATHY_RESPONSE = 3;
    public static final int MSG_REGISTER_ERROR_LISTENER = 4;
    public static final int MSG_ERROR_RESPONSE = 5;
    public static final int MSG_SET_SCAN_PERIOD = 6;
    private final Messenger mMessenger;
    private final BluetoothAdapter.LeScanCallback mLeScanCallback;
    private AlarmManager mAlarmManager;
    private BluetoothAdapter mAdapter;
    private ConcurrentHashMap mBeaconsInACycle;
    private ConcurrentHashMap mBeaconsNearBy;
    private TelepathyPeriod mTelepathyPeriod;
    private Runnable mAfterScanTask;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mScanning;
    private boolean mAssigned;

    public SenzService() {
        this.mMessenger = new Messenger(new IncomingHandler(null));
        this.mLeScanCallback = new InternalLeScanCallback(null);
        this.mSenzesInACycle = new ConcurrentHashMap();
        this.mSenzesDiscovered = new ConcurrentHashMap();
        this.mTelepathyPeriod = new TelepathyPeriod(TimeUnit.SECONDS.toMillis(1L),
                                                    TimeUnit.SECONDS.toMillis(0L),
                                                    TimeUnit.SECONDS.toMillis(10L));
    }

    public void onCreate() {
        super.onCreate();

        L.i("Creating service");
        
        this.mAlarmManager = (AlarmManager) getSystemService("alarm");
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService("bluetooth");
        this.mAdapter = bluetoothManager.getAdapter();
        this.mAfterScanTask = new AfterScanTask(null);

        this.mHandlerThread = new HandlerThread("SenzServiceThread", Process.THREAD_PRIORITY_BACKGROUND);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());

        this.bluetoothBroadcastReceiver = createBluetoothBroadcastReceiver();
        this.startScanBroadcastReceiver = createStartScanBroadcastReceiver();
        this.afterScanBroadcastReceiver = createAfterScanBroadcastReceiver();
        registerReceiver(this.bluetoothBroadcastReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
        registerReceiver(this.startScanBroadcastReceiver, new IntentFilter("startScan"));
        registerReceiver(this.afterScanBroadcastReceiver, new IntentFilter("afterScan"));
    }

    public void onDestroy() {
        L.i("Destroying service");
        unregisterReceiver(this.bluetoothBroadcastReceiver);
        unregisterReceiver(this.startScanBroadcastReceiver);
        unregisterReceiver(this.afterScanBroadcastReceiver);

        if (this.mAdapter != null) {
            stopScanning();
        }

        this.handlerThread.quit();

        super.onDestroy();
    }

    public IBinder onBind(Intent intent) {
        return this.mMessenger.getBinder();
    }

    private void startScanning() {
        if (this.mScanning) {
            L.d("Scanning already in progress, not starting another");
            return;
        }
        if (!this.mAdapter.isEnabled()) {
            // TODO: tell manager about the exception
            return;
        }
        if (!this.mAdapter.startLeScan(this.leScanCallback)) {
            // TODO: tell manager about the exception
            return;
        }
        this.mScanning = true;
        removeAllCallbacks();
        setAlarm(this.mAfterScanBroadcastPendingIntent, this.mTelepathyPeriod.scanMillis);
    }

    private void stopScanning() {
        try {
            this.mScanning = false;
            this.adapter.stopLeScan(this.leScanCallback);
        }
        catch (Exception e) {
            L.wtf("BluetoothAdapter throws unexpected exception", e);
        }
    }

    private void setAlarm(PendingIntent pendingIntent, long delayMillis) {
        this.alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + delayMillis, pendingIntent);
    }

    private BroadcastReceiver createBluetoothBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
                    switch(intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1)) {

                        case BluetoothAdapter.STATE_ON:
                            SenzService.this.handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (SenzService.this.mAssigned) {
                                        L.i("Bluetooth ON: start scanning");
                                        SenzService.this.startScanning();
                                    }
                                }
                            });
                            break;

                        case BluetoothAdapter.STATE_OFF:
                            SenzService.this.handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    L.i("Bluetooth OFF: stop scanning");
                                    SenzService.this.stopScanning();
                                }
                            });
                            break;
                    }
                }
            }
        };
    }

    private BroadcastReceiver createAfterScanBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                BeaconService.this.handler.post(BeaconService.this.afterScanCycleTask);
            }
        }
    }

    private BroadcastReceiver createStartScanBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                BeaconService.this.handler.post(new Runnable() {
                    @Override
                    public void run() {
                        BeaconService.this.startScanning();
                    }
                });
            }
        }
    }

    private class InternalLeScanCallback implements LeScanCallback {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Beacon beacon = Beacon.fromLeScan(device, rssi, scanRecord);

            if (beacon == null) {
                L.v("Device" + device + "is not a beacon");
                return;
            }
            BeaconService.this.mBeaconsInACycle.put(beacon);
        }
    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            final int what = msg.what;
            final Bundle bundle = msg.getData();
            final Messenger replyTo = msg.replyTo;
            BeaconService.this.handler.post(new Runnable() {
                @Override
                public void Run() {
                    switch (what) {
                        case MSG_START_TELEPATHY:
                        case MSG_STOP_TELEPATHY:
                        case MSG_TELEPATHY_RESPONSE:
                        case MSG_REGISTER_ERROR_LISTENER:
                        case MSG_ERROR_RESPONSE:
                        case MSG_SET_SCAN_PERIOD:
                    }
                }
            });
        }
    }
};
