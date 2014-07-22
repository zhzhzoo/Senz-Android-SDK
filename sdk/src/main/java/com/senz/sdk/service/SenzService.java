package com.senz.sdk.service;

import android.app.AlarmManager;
import android.app.Service;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.os.Messenger;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.os.Parcel;
import android.os.Looper;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Bundle;
import android.os.SystemClock;
import android.location.Location;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Map;
import java.util.ArrayList;
import com.senz.sdk.service.TelepathyPeriod;
import com.senz.sdk.service.GPSInfo;
import com.senz.sdk.network.Query;
import com.senz.sdk.utils.L;
import com.senz.sdk.Beacon;
import com.senz.sdk.Senz;
import com.senz.sdk.BeaconWithSenzes;

public class SenzService extends Service {

    public static final int MSG_START_TELEPATHY = 1;
    public static final int MSG_STOP_TELEPATHY = 2;
    public static final int MSG_TELEPATHY_RESPONSE = 3;
    public static final int MSG_ERROR_RESPONSE = 4;
    public static final int MSG_SET_SCAN_PERIOD = 5;
    private static final Intent START_SCAN_INTENT = new Intent("startScan");
    private static final Intent AFTER_SCAN_INTENT = new Intent("afterScan");
    private static final Intent LOOK_NEARBY_INTENT = new Intent("lookNearby");
    private PendingIntent mStartScanBroadcastPendingIntent;
    private PendingIntent mAfterScanBroadcastPendingIntent;
    private PendingIntent mLookNearbyBroadcastPendingIntent;
    private BroadcastReceiver mBluetoothBroadcastReceiver;
    private BroadcastReceiver mStartScanBroadcastReceiver;
    private BroadcastReceiver mAfterScanBroadcastReceiver;
    private BroadcastReceiver mLookNearbyBroadcastReceiver;
    private final Messenger mMessenger;
    private final BluetoothAdapter.LeScanCallback mLeScanCallback;
    private Messenger mReplyTo;
    private AlarmManager mAlarmManager;
    private BluetoothAdapter mAdapter;
    private ConcurrentHashMap<Beacon, Boolean> mBeaconsInACycle;
    private ConcurrentHashMap<Beacon, Boolean> mBeaconsNearBy;
    private TelepathyPeriod mTelepathyPeriod;
    private Runnable mAfterScanTask;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private boolean mScanning;
    private boolean mStarted;
    private GPSInfo mGPSInfo;
    private Location mLocation;
    private GPSInfo.GPSInfoListener mGPSInfoListener;

    public SenzService() {
        this.mMessenger = new Messenger(new IncomingHandler());
        this.mLeScanCallback = new InternalLeScanCallback();
        this.mBeaconsInACycle = new ConcurrentHashMap();
        this.mBeaconsNearBy = new ConcurrentHashMap();
        this.mTelepathyPeriod = new TelepathyPeriod(TimeUnit.SECONDS.toMillis(1L),
                                                    TimeUnit.SECONDS.toMillis(0L),
                                                    TimeUnit.MINUTES.toMillis(30L));
        this.mStarted = this.mScanning = false;
        this.mGPSInfo = new GPSInfo(this);
        this.mGPSInfoListener = new InternalGPSInfoListener();
    }

    public void onCreate() {
        super.onCreate();

        L.i("Creating service");
        
        this.mAlarmManager = (AlarmManager) getSystemService("alarm");
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService("bluetooth");
        this.mAdapter = bluetoothManager.getAdapter();
        this.mAfterScanTask = new AfterScanTask();

        this.mHandlerThread = new HandlerThread("SenzServiceThread", Process.THREAD_PRIORITY_BACKGROUND);
        this.mHandlerThread.start();
        this.mHandler = new Handler(this.mHandlerThread.getLooper());

        this.mBluetoothBroadcastReceiver = createBluetoothBroadcastReceiver();
        this.mStartScanBroadcastReceiver = createStartScanBroadcastReceiver();
        this.mAfterScanBroadcastReceiver = createAfterScanBroadcastReceiver();
        this.mLookNearbyBroadcastReceiver = createLookNearbyBroadcastReceiver();

        registerReceiver(this.mBluetoothBroadcastReceiver, new IntentFilter("android.bluetooth.adapter.action.STATE_CHANGED"));
        registerReceiver(this.mStartScanBroadcastReceiver, new IntentFilter("startScan"));
        registerReceiver(this.mAfterScanBroadcastReceiver, new IntentFilter("afterScan"));
        registerReceiver(this.mLookNearbyBroadcastReceiver, new IntentFilter("lookNearby"));

        this.mStartScanBroadcastPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 233, START_SCAN_INTENT, 0);
        this.mAfterScanBroadcastPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 233, AFTER_SCAN_INTENT, 0);
        this.mLookNearbyBroadcastPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 233, LOOK_NEARBY_INTENT, 0);

        this.mGPSInfo.start(this.mGPSInfoListener);
    }

    public void onDestroy() {
        L.i("Destroying service");
        unregisterReceiver(this.mBluetoothBroadcastReceiver);
        unregisterReceiver(this.mStartScanBroadcastReceiver);
        unregisterReceiver(this.mAfterScanBroadcastReceiver);

        if (this.mAdapter != null) {
            stopScanning();
        }

        this.mGPSInfo.end();

        this.mHandlerThread.quit();

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
        if (!this.mStarted)
            return;
        if (!this.mAdapter.isEnabled()) {
            // TODO: tell manager about the exception
            return;
        }
        if (!this.mAdapter.startLeScan(this.mLeScanCallback)) {
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
            removeAllCallbacks();
            this.mAdapter.stopLeScan(this.mLeScanCallback);
        }
        catch (Exception e) {
            L.wtf("BluetoothAdapter throws unexpected exception", e);
        }
    }

    private void removeAllCallbacks()
    {
        this.mHandler.removeCallbacks(this.mAfterScanTask);
        this.mAlarmManager.cancel(this.mAfterScanBroadcastPendingIntent);
        this.mAlarmManager.cancel(this.mStartScanBroadcastPendingIntent);
    }

    private void lookNearby() {
        this.mAlarmManager.cancel(this.mLookNearbyBroadcastPendingIntent);

        Query.senzesFromLocationAsync(this.mLocation, new Query.SenzesReadyCallback() {
            @Override
            public void onSenzesReady(ArrayList<Senz> senzes) {
            }
        });
        setAlarm(this.mLookNearbyBroadcastPendingIntent, this.mTelepathyPeriod.GPSMillis);
    }

    private void setAlarm(PendingIntent pendingIntent, long delayMillis) {
        this.mAlarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + delayMillis, pendingIntent);
    }

    private BroadcastReceiver createBluetoothBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if ("android.bluetooth.adapter.action.STATE_CHANGED".equals(intent.getAction())) {
                    switch(intent.getIntExtra("android.bluetooth.adapter.extra.STATE", -1)) {

                        case BluetoothAdapter.STATE_ON:
                            SenzService.this.mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (SenzService.this.mStarted) {
                                        L.i("Bluetooth ON: start scanning");
                                        SenzService.this.startScanning();
                                    }
                                }
                            });
                            break;

                        case BluetoothAdapter.STATE_OFF:
                            SenzService.this.mHandler.post(new Runnable() {
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
                SenzService.this.mHandler.post(SenzService.this.mAfterScanTask);
            }
        };
    }

    private BroadcastReceiver createStartScanBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SenzService.this.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        SenzService.this.startScanning();
                    }
                });
            }
        };
    }

    private BroadcastReceiver createLookNearbyBroadcastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SenzService.this.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        SenzService.this.lookNearby();
                    }
                });
            }
        };
    }

    private BroadcastReceiver createStartScanBroadCastReceiver() {
        return new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                SenzService.this.mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        SenzService.this.lookNearby();
                    }
                });
            }
        };
    }

    private class InternalLeScanCallback implements BluetoothAdapter.LeScanCallback {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Beacon beacon = Beacon.fromLeScan(device, rssi, scanRecord);

            if (beacon == null) {
                L.v("Device" + device + "is not a beacon");
                return;
            }
            SenzService.this.mBeaconsInACycle.put(beacon, true);
        }
    }

    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            final int what = msg.what;
            final Bundle bundle = msg.getData();
            final Messenger replyTo = msg.replyTo;
            SenzService.this.mHandler.post(new Runnable() {
                @Override
                public void run() {
                    switch (what) {
                        case MSG_START_TELEPATHY:
                            L.i("Starting telepathy");
                            SenzService.this.mStarted = true;
                            SenzService.this.mReplyTo = replyTo;
                            startScanning();
                            break;
                        case MSG_STOP_TELEPATHY:
                            L.i("Stopping telepathy");
                            SenzService.this.mStarted = false;
                            stopScanning();
                            break;
                        case MSG_SET_SCAN_PERIOD:
                            bundle.setClassLoader(TelepathyPeriod.class.getClassLoader());
                            SenzService.this.mTelepathyPeriod = (TelepathyPeriod) bundle.getParcelable("telepathyPeriod");
                            L.d("Setting scan period: " + SenzService.this.mTelepathyPeriod);
                            break;
                    }
                }
            });
        }
    }

    private class AfterScanTask implements Runnable {
        @Override
        public void run() {
            SenzService.this.stopScanning();

            ArrayList<Beacon> beacons = new ArrayList<Beacon>();
            final Message response = Message.obtain(null, MSG_TELEPATHY_RESPONSE);
            for (Map.Entry<Beacon, Boolean> e : SenzService.this.mBeaconsInACycle.entrySet())
                beacons.add(e.getKey());
            Query.senzesFromBeaconsAsync(beacons, SenzService.this.mLocation, new Query.SenzesReadyCallback() {
                @Override
                public void onSenzesReady(ArrayList<Senz> senzes) {
                    response.getData().putParcelableArrayList("senzes", senzes);
                    try {
                        mReplyTo.send(response);
                    }
                    catch (RemoteException e) {
                        L.e("Error while delivering responses", e);
                    }

                    SenzService.this.mBeaconsInACycle.clear();
                    if (SenzService.this.mStarted == false)
                        return;
                    if (SenzService.this.mTelepathyPeriod.waitMillis == 0L)
                        SenzService.this.startScanning();
                    else
                        SenzService.this.setAlarm(SenzService.this.mStartScanBroadcastPendingIntent, SenzService.this.mTelepathyPeriod.waitMillis);
                }
            });
        }
    }

    private class InternalGPSInfoListener implements GPSInfo.GPSInfoListener {
        @Override
        public void onGPSInfoChanged(Location location) {
            SenzService.this.mLocation = location;
            lookNearby();
        }
    }
}
