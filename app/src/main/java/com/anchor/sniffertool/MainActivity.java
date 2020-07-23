package com.anchor.sniffertool;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private WifiHelper mWifi;
    private SnifferOperationHelper mSniffer;
    private HandlerThread mCmdThread;
    private Handler mCmdHandler;

    private Button mBtnStart;
    private Button mBtnStop;
    private Button mBtnScan;
    private Button mBtnDel;
    private Spinner mSpinnerChannel;
    private Spinner mSpinnerBandWith;

    private ListView mApListView;
    private List<String> mApList;
    private ArrayAdapter<String> mArrayAdapter;

    String[] permissionArray = {
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS
    };

    private final int MSG_START = 1;
    private final int MSG_STOP = 2;

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


    private void SetUpUi() {
        mBtnStart = (Button) findViewById(R.id.bt_start);
        mBtnStop= (Button) findViewById(R.id.bt_stop);
        mBtnScan = (Button) findViewById(R.id.bt_scan);
        mBtnDel = (Button) findViewById(R.id.bt_delete);

        mBtnStop.setEnabled(false);

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
                try {
                    mWifi.startScan();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mBtnStart.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                Message message = new Message();
                message.what = MSG_START;
                mCmdHandler.sendMessage(message);
                mBtnScan.setEnabled(false);
                mBtnStart.setEnabled(false);
                mBtnStop.setEnabled(true);
                Toast.makeText(MainActivity.this, "start sniffer.", 3000).show();
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                Message message = new Message();
                message.what = MSG_STOP;
                mCmdHandler.sendMessage(message);
                mBtnScan.setEnabled(true);
                mBtnStart.setEnabled(true);
                mBtnStop.setEnabled(false);
                Toast.makeText(MainActivity.this, "Save log to "+ mSniffer.getSnifferLogPath(), 3000).show();
            }
        });

        mBtnDel.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("WrongConstant")
            @Override
            public void onClick(View view) {
                mSniffer.DelAllSnifferLog();
                Toast.makeText(MainActivity.this,
                        "Delete all sniffer log in "+mSniffer.getSnifferLogPath(),
                        3000).show();
            }
        });

        mSpinnerChannel = (Spinner) findViewById(R.id.spinner_channel);
        mSpinnerChannel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {

                String[] channel = getResources().getStringArray(R.array.channel);
                mSniffer.mChannel = Integer.valueOf(channel[pos]);
                Toast.makeText(MainActivity.this, "channel is"+mSniffer.mChannel, 3000).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        mSpinnerBandWith = (Spinner) findViewById(R.id.spinner_bandwith);
        mSpinnerBandWith.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @SuppressLint("WrongConstant")
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {

                String[] bandwidth = getResources().getStringArray(R.array.bandwith);
                mSniffer.mBandWidth = pos;

                switch (bandwidth[pos]) {
                    case "40H":
                        mSniffer.mBandWidth = 40;
                        mSniffer.mScenario = 1;
                        break;
                    case "40L":
                        mSniffer.mBandWidth = 40;
                        mSniffer.mScenario = 0;
                        break;
                    default:
                        mSniffer.mBandWidth = Integer.valueOf(bandwidth[pos]);
                }
                Toast.makeText(MainActivity.this, "bd is "+mSniffer.mBandWidth+"scn is "+mSniffer.mScenario, 2000).show();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions(permissionArray);

        mWifi = new WifiHelper(this);
        mSniffer = new SnifferOperationHelper(this);
        mCmdThread = new HandlerThread("CmdThread");
        mCmdThread.start();
        mCmdHandler = new Handler(mCmdThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case MSG_START:
                        mSniffer.Start();
                        break;
                    case MSG_STOP:
                        mSniffer.Stop();
                        break;
                    default:
                        break;
                }
            }
        };

        SetUpUi();

        registerBroadcast();

        if ( false == mWifi.openWifi()) {
            Toast toast=Toast.makeText(getApplicationContext(),
                    "Please open wifi first!", Toast.LENGTH_SHORT);
            toast.show();
        }
        mWifi.startScan();

        mSniffer.CreateLogDirectory();
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(mReceiver);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Receiver not registered")) {
                // Ignore this exception. This is exactly what is desired
            } else {
                // unexpected, re-throw
                throw e;
            }
        }
        super.onDestroy();
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