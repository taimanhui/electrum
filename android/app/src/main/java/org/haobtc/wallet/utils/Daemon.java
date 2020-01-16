package org.haobtc.wallet.utils;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.utils.Global;
import com.chaquo.python.PyObject;

public class Daemon {
    public static PyObject commands = null;
    public static PyObject network = null;
    public Daemon() {
        commands = Global.guiConsole.callAttr("AndroidCommands");
        commands.callAttr("start");
        commands.callAttr("set_callback_fun", this);
    }

    public void onCallback(int event, String msg) {

//        EventBus.getDefault().post(new FirstEvent("22"));
        Log.i("onCallback", "Callback in.....=================="+event +"   ============================    "+ msg);
    }
}
