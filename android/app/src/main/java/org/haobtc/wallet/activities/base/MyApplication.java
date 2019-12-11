package org.haobtc.wallet.activities.base;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;
public class MyApplication extends Application {
    public static MyApplication mInstance;

    public MyApplication() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        Global.app = MyApplication.getInstance();
        Python.start(new AndroidPlatform(Global.app));
        Global.py = Python.getInstance();
        Log.i("JXM", "onCreate++++++: ");
        Global.py.getModule("electrum.constants").callAttr("set_testnet");

        Global.mHandler = null;
        if (Global.mHandler == null) {
            Global.mHandler = new Handler(Looper.getMainLooper());
        }
        Global.guiDaemon = Global.py.getModule("electrum_gui.android.daemon");
        Global.guiConsole = Global.py.getModule("electrum_gui.android.console");
        new Daemon();

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
