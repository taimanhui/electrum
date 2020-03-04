package org.haobtc.wallet.activities.base;

import android.app.Application;

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
        new Thread(){
            @Override
            public void run() {
                super.run();
            }
        }.start();
        mInstance = this;
        initBle();

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
        Ble.options().setLogBleEnable(true)//设置是否输出打印蓝牙日志
                .setThrowBleException(true)//设置是否抛出蓝牙异常
                .setLogTAG("AndroidBLE")//设置全局蓝牙操作日志TAG
                .setAutoConnect(false)//设置是否自动连接
                .setFilterScan(false)//设置是否过滤扫描到的设备
                .setConnectFailedRetryCount(3)
                .setConnectTimeout(10 * 1000)//设置连接超时时长
                .setScanPeriod(12 * 1000)//设置扫描时长
                .setServiceBindFailedRetryCount(3)
                .setUuidService(UUID.fromString(PRIMARY_SERVICE))
                .setUuidWriteCha(UUID.fromString(WRITE_CHARACTERISTIC))
                .setUuidNotify(UUID.fromString(READ_CHARACTERISTIC))
                .create(mInstance);
    }
}
