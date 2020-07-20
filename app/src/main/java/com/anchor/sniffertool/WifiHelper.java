package com.anchor.sniffertool;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class WifiHelper {

    private static final String TAG = "WifiHelper";
    private WifiManager manager;
    List<ScanResult> mScanResults;

    public WifiHelper(Context context) {
        manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public boolean openWifi(){

        if(manager != null && !manager.isWifiEnabled()){
            Log.d(TAG, "set wifi on.");
            //manager.setWifiEnabled(true);
            //pop dialog to notify user to turn on wifi.
            return false;
        }

        return true;
    }

    public boolean closeWifi(){
        if(manager != null && !manager.isWifiEnabled()){
            Log.d(TAG, "set wifi off.");
            manager.setWifiEnabled(false);
        }
        return true;
    }

    public void startScan(){
        Log.d(TAG, "start scan.");
        manager.startScan();
    }

    public List<String> scanResult(){
        List<String> apList = new ArrayList<String>();

        mScanResults = manager.getScanResults();
        int size = mScanResults.size();
        Log.d(TAG, "get scan results("+size+").");
        for (int i = 0; i < size; i++) {
            //Log.d(TAG, "NO"+i+": "+ mScanResults.get(i).toString());
            ScanResult item = mScanResults.get(i);
            StringBuilder str = new StringBuilder();
            String bandwith = "Unknow Bandwith";

            if (item.SSID.isEmpty())
                item.SSID = "**** (hidden)";

            switch(item.channelWidth){
                case 0:
                    bandwith = "20MHZ";
                    break;
                case 1:
                    bandwith = "40MHZ";
                    break;
                case 2:
                    bandwith = "80MHZ";
                    break;
                case 3:
                    bandwith = "160MHZ";
                    break;
                case 4:
                    bandwith = "80+80MHZ";
                    break;
                default:
                    bandwith = "Unknow Bandwith";
                    break;
            };

            int ch = convertFrequencyToChannel(item.frequency);

            if (item.channelWidth == 1) {
                if (item.frequency < item.centerFreq0)
                    bandwith = "[40MHZ-H]";
                else
                    bandwith = "[40MHZ-L]";
            }

            str.append("ssid: "+item.SSID+'\n')
                .append("bssid: "+item.BSSID+'\n')
                .append("rssi: "+String.valueOf(item.level)+'\n')
                .append("channel: "+ch+" ("+String.valueOf(item.frequency)+") ")
                .append(bandwith+" [")
                .append(String.valueOf(item.centerFreq0)+'-')
                .append(String.valueOf(item.centerFreq1)+"]\n")
                .append(item.capabilities);
            apList.add(str.toString());
        }
        apList.sort(null);
        return apList;
    }

    private int convertFrequencyToChannel(int frequency) {
        if (frequency >= 2412 && frequency <= 2472) {
            return (frequency - 2412) / 5 + 1;
        } else if (frequency == 2484) {
            return 14;
        } else if (frequency >= 5170  &&  frequency <= 5825) {
            /* DFS is included. */
            return (frequency - 5170) / 5 + 34;
        }
        return -1;
    }
}
