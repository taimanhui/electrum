package org.haobtc.wallet.utils;

import android.app.Application;
import android.os.Handler;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;

public class Global {
    public static Application app;
    public static Python py;
    public static Handler mHandler;
    public static PyObject guiDaemon;
    public static PyObject guiConsole;
}
