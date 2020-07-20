package com.anchor.sniffertool;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private WifiHelper mWifi;
    private Button mBtnStart;
    private Button mBtnStop;
    private Button mBtnScan;

    private ListView mApListView;
    private List<String> mApList;
    private ArrayAdapter<String> mArrayAdapter;

    String[] permissionArray = {
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                mApList.clear();
                mApList.addAll(mWifi.scanResult());
                mArrayAdapter.notifyDataSetChanged();
                mApListView.setAdapter(mArrayAdapter);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions(permissionArray);

        mWifi = new WifiHelper(this);

        mBtnStart = (Button) findViewById(R.id.bt_start);
        mBtnStop= (Button) findViewById(R.id.bt_stop);
        mBtnScan = (Button) findViewById(R.id.bt_scan);

        mApListView = (ListView) findViewById(R.id.lv);

        mApList = new ArrayList<String>();
        mArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.simple_list_item_1,
                mApList);
        mApListView.setAdapter(mArrayAdapter);

        mBtnScan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if ( false == mWifi.openWifi()) {
                    Toast toast=Toast.makeText(getApplicationContext(),
                            "Please open wifi first!", Toast.LENGTH_SHORT);
                    toast.show();
                    return ;
                }
                mWifi.startScan();
            }
        });

        registerBroadcast();

        if ( false == mWifi.openWifi()) {
            Toast toast=Toast.makeText(getApplicationContext(),
                    "Please open wifi first!", Toast.LENGTH_SHORT);
            toast.show();
        }
        mWifi.startScan();
    }

    private void registerBroadcast(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(mReceiver, filter);
    }

    public void checkPermissions(@NonNull String... permissions) {
        if (Build.VERSION.SDK_INT < 23) return;

        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = this.checkSelfPermission(permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, permission+" is granted.");
            } else {
                Log.d(TAG, permission+" is rejected.");
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            Log.d(TAG, "request "+deniedPermissions);
            this.requestPermissions(deniedPermissions, 13);
        }
    }


}