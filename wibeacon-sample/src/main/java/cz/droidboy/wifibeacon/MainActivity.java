package cz.droidboy.wifibeacon;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.List;

import cz.droidboy.wifibeacon.adapter.APAdapter;
import cz.droidboy.wifibeacon.util.PrefUtils;
import cz.droidboy.wibeacon.range.ProximityScanner;
import cz.droidboy.wibeacon.range.ScanFilter;

/**
 * @author Jonas Sevcik
 */
public class MainActivity extends BaseActivity implements ProximityScanner.RangingListener {
    private static final int PERMISSION_REQUEST_CODE = 101;

    private ProximityScanner scanner;
    private APAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!checkPermission()) {
                String[] permission = {Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION
                };
                requestPermissionsSafely(permission, PERMISSION_REQUEST_CODE);
            }
        }

        ListView listView = (ListView) findViewById(R.id.list);
        listView.setEmptyView(findViewById(android.R.id.empty));
        adapter = new APAdapter(this);
        listView.setAdapter(adapter);

        scanner = new ProximityScanner(this, this);

        startService(new Intent(MainActivity.this, WiFiDetectionService.class));

    }

    @Override
    protected void onStart() {
        super.onStart();

        ScanFilter filter = PrefUtils.prepareFilter(this);
        scanner.startRangingAPs(filter);
    }

    @Override
    protected void onStop() {
        super.onStop();

        scanner.stopRangingAPs();
    }

    @Override
    public void onAPsDiscovered(List<ScanResult> results) {
        adapter.replaceData(results);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private boolean checkPermission() {
        return hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                && hasPermission(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissionsSafely(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasPermission(String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

}
