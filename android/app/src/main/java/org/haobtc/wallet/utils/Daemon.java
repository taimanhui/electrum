package org.haobtc.wallet.utils;

import org.haobtc.wallet.utils.Global;
import com.chaquo.python.PyObject;

public class Daemon {
    public static PyObject commands = null;
    public static PyObject network = null;
    public Daemon() {
        commands = Global.guiConsole.callAttr("AndroidCommands", Global.app);
        network = commands.get("network");
        network.callAttr("register_callback", Global.guiDaemon.callAttr("make_callback", this), Global.guiConsole.get("CALLBACKS"));
        commands.callAttr("start");
    }
}
