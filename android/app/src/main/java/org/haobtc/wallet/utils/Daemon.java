package org.haobtc.wallet.utils;

import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.event.SecondEvent;
import org.haobtc.wallet.utils.Global;
import com.chaquo.python.PyObject;

import java.lang.ref.WeakReference;

public class Daemon {
    public static PyObject commands = null;
    public static PyObject network = null;
    private static WeakReference<Daemon> daemonWeakReference = new WeakReference<>(new Daemon());
    static {
        commands = Global.guiConsole.callAttr("AndroidCommands");
        commands.callAttr("start");
        commands.callAttr("set_callback_fun", daemonWeakReference.get());
    }

    public void onCallback(String event, String msg) {
        Log.i("onCallback", "=================="+event +"   ============================    "+ msg);
        if (event.equals("update_status")){
            EventBus.getDefault().post(new SecondEvent(msg));
        }else{
            EventBus.getDefault().post(new FirstEvent("22"));
        }

    }
}
