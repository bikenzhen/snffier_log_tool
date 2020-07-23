package com.anchor.sniffertool;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class SnifferOperationHelper {
    private static final String TAG = "SnifferOperationHelper";
    private static final String LOG_PATH = "/sniffer_log/";
    //iwpriv parameter : channel / bandwith / scenario
    public int mChannel;
    public int mBandWidth;
    public int mScenario;
    private String mSnifferLogPatch;

    public SnifferOperationHelper(Context context) {
        mChannel = 1;
        mBandWidth = 20;
        mScenario = 0;
        mSnifferLogPatch = getSDPath(context);
    }

    private boolean Cmd(String cmd) {

        Process process = null;

        Log.d(TAG, "send cmd: "+cmd);
        try {
            process = Runtime.getRuntime().exec(cmd);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        while (true) {
            try {
                if (!((line = reader.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "cmd result ==> "+line);
        }
        return true;
    }

    public String Start() {
        String cmd = "";
        cmd = "wifi_sniffer_start "+mChannel+" "+mBandWidth+" "+mScenario;
        Cmd(cmd);
        return cmd;
    }

    public String Stop() {
        Cmd("wifi_sniffer_stop");
        return "wifi_sniffer_stop";
    }

    public String getSnifferLogPath() {
        return mSnifferLogPatch;
    }

    public String getSDPath(Context context) {

        return context.getExternalFilesDir(null).getAbsolutePath();
       /* File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();
        }
        Log.d(TAG, "sdcard dir: "+sdDir.toString());
        return sdDir.toString();*/
    }

    public boolean CreateLogDirectory() {
/*        String path = mSnifferLogPatch+LOG_PATH;

        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdir();
            Log.d(TAG, "create directory: "+path);
        }*/
        Log.d(TAG, "create directory: "+mSnifferLogPatch);
        return true;
    }

    public boolean DelAllSnifferLog() {
        String path = mSnifferLogPatch;
        File dir = new File(path);
        if ((!dir.exists()) || (!dir.isDirectory())) {
            Log.d(TAG, "directory is not found: "+path);
            return false;
        }

        File[] files = dir.listFiles();

        if (files == null)
            return true;

        for (File file : files) {
            Log.d(TAG, "del : "+file.getAbsolutePath());
            if (file.exists() && file.isFile()) {
                deleteSingleFile(file.getAbsolutePath());
            }
        }

        return true;
    }

    private boolean deleteSingleFile(String filePathName) {
        File file = new File(filePathName);

        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.d(TAG, "del "+file.getAbsolutePath()+" ok.");
                return true;
            } else {
                Log.d(TAG, file.getAbsolutePath()+" is not exist, del fail.");
                return false;
            }
        }
        return true;
    }
}
