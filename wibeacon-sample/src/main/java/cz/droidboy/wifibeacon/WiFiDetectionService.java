package cz.droidboy.wifibeacon;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

import cz.droidboy.wibeacon.range.ProximityMonitor;
import cz.droidboy.wifibeacon.util.PrefUtils;
import cz.droidboy.wibeacon.range.ProximityScanner;
import cz.droidboy.wibeacon.range.ScanFilter;

/**
 * @author Jonas Sevcik
 */
public class WiFiDetectionService extends Service implements ProximityMonitor.MonitoringListener {

    public static final String UPDATE_COMMAND_KEY = "update";
    public static final int UPDATE_COMMAND = 0;
    private static final String TAG = WiFiDetectionService.class.getSimpleName();
    private static final int UPDATE_INTERVAL = 10_000; //millis
    private static final int NOTIFICATION_ID = 0;
    private ProximityMonitor mMonitor;
    private WifiManager.WifiLock mWifiLock;
    private BroadcastReceiver mWifiDisabledReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                if (state == WifiManager.WIFI_STATE_DISABLING || state == WifiManager.WIFI_STATE_DISABLED) {
                    stopSelf();
                }
            }
        }
    };
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mBuilder;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();
        if (Build.VERSION.SDK_INT < 18) {
            acquireWifiLock();
            registerReceiver(mWifiDisabledReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        }

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
                .setHintHideIcon(true);
        PendingIntent viewPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);
        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_wifi_notification)
                .setContentTitle("APs found")
                .setContentIntent(viewPendingIntent)
                .setLights(0x0048a610, 500, 5000)
                .extend(wearableExtender);

        mMonitor = new ProximityMonitor(this, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        mMonitor.startMonitoringAPs(PrefUtils.prepareFilter(this), UPDATE_INTERVAL);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        mMonitor.stopMonitoringAPs();

        if (Build.VERSION.SDK_INT < 18) {
            releaseWifiLock();
            unregisterReceiver(mWifiDisabledReceiver);
        }
    }

    private void acquireWifiLock() {
        WifiManager manager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWifiLock = manager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, TAG);
        mWifiLock.setReferenceCounted(false);
        mWifiLock.acquire();
    }

    private void releaseWifiLock() {
        if (mWifiLock != null) {
            if (mWifiLock.isHeld()) {
                mWifiLock.release();
            }
            mWifiLock = null;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onEnterRegion(List<ScanResult> results) {
        Log.d(TAG, "onEnterRegion");
        mBuilder.setContentText(String.valueOf(results.size()));
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    @Override
    public void onDwellRegion(List<ScanResult> results) {
        Log.d(TAG, "onDwellRegion");
        //nothing
    }

    @Override
    public void onExitRegion(ScanFilter filter) {
        Log.d(TAG, "onExitRegion");
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

}
