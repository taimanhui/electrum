package org.haobtc.wallet.activities.base;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;


import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import org.haobtc.wallet.BuildConfig;
import org.haobtc.wallet.R;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;

import java.lang.ref.WeakReference;
import java.util.UUID;

import cn.com.heaton.blelibrary.ble.Ble;

public class MyApplication extends Application {
    private static volatile MyApplication mInstance;
    private static final String PRIMARY_SERVICE =      "00000001-0000-1000-8000-00805f9b34fb";//"6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    private static final String WRITE_CHARACTERISTIC = "00000002-0000-1000-8000-00805f9b34fb";
    private static final String READ_CHARACTERISTIC =  "00000003-0000-1000-8000-00805f9b34fb";//


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        initBle();
        initChaquo();
    }
    public static MyApplication getInstance() {
        if (mInstance == null) {
            synchronized (MyApplication.class) {
                if (mInstance == null) {
                    mInstance = new MyApplication();
                }
            }
        }
        return mInstance;
    }
    // init ble
    private void initBle() {
        Ble.options().setLogBleEnable(false)//Set whether to print Bluetooth log
                .setThrowBleException(true)//Set whether to throw Bluetooth exception
                .setLogTAG("AndroidBLE")//Set global Bluetooth operation log TAG
                .setAutoConnect(false)//Set whether to connect automatically
                .setFilterScan(true)//Set whether to filter the founded devices
                .setConnectFailedRetryCount(3)
                .setConnectTimeout(10 * 1000)// Set the connection timeout
                .setScanPeriod(12 * 1000)// Set the Scanning period
                .setServiceBindFailedRetryCount(3)
                .setUuidService(UUID.fromString(PRIMARY_SERVICE))
                .setUuidWriteCha(UUID.fromString(WRITE_CHARACTERISTIC))
                .setUuidNotify(UUID.fromString(READ_CHARACTERISTIC))
                .setUuidReadCha(UUID.fromString(READ_CHARACTERISTIC))
                .create(mInstance);
    }
    private void initChaquo() {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(mInstance));
        }
        Global.py = Python.getInstance();
    }
}
