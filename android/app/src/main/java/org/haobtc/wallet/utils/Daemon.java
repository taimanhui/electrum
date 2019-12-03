package org.haobtc.wallet.utils;

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
    public static void onCallback(int event, String msg) {
        System.out.println("DaemonModel.kt onCallback in.....========================================================================="+event);
    }
}
