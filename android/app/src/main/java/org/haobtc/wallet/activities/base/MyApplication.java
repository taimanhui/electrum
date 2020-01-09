package org.haobtc.wallet.activities.base;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import org.haobtc.wallet.BuildConfig;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;
public class MyApplication extends Application {
    public static MyApplication mInstance;

    public MyApplication() {
    }

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
}
