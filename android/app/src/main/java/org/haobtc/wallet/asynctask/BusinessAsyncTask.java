package org.haobtc.wallet.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import org.haobtc.wallet.utils.Daemon;

public class BusinessAsyncTask extends AsyncTask<String, Void, String> {
    private Helper helper;
    private final static String TAG = BusinessAsyncTask.class.getSimpleName();
    public final static String GET_EXTEND_PUBLIC_KEY = "get_xpub_from_hw";
    public final static String GET_EXTEND_PUBLIC_KEY_SINGLE = "get_xpub_from_hw_single";
    public final static String SIGN_TX = "sign_tx";
    public static final String BACK_UP = "backup_wallet";
    public static final String RECOVER = "wallet_recovery";
    public final static String CHANGE_PIN = "reset_pin";
    public final static String WIPE_DEVICE = "wipe_device";
    public static final String SIGN_MESSAGE = "sign_message";
    public static final String INIT_DEVICE = "init";
    public static final String COUNTER_VERIFICATION = "hardware_verify";

    public BusinessAsyncTask setHelper(Helper helper) {
        this.helper = helper;
        return this;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // EventBus.getDefault().register(this);
        helper.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings) {
        String result = "";
        switch (strings[0]) {
            case GET_EXTEND_PUBLIC_KEY_SINGLE:
            case RECOVER:
            case SIGN_MESSAGE:
                try {
                    result = Daemon.commands.callAttr(strings[0].endsWith("single") ? GET_EXTEND_PUBLIC_KEY : strings[0], strings[1], strings[2]).toString();
                } catch (Exception e) {
                    cancel(true);
                    onException(e);
                }
                break;
            case GET_EXTEND_PUBLIC_KEY:
            case CHANGE_PIN:
            case WIPE_DEVICE:
            case BACK_UP:
            case SIGN_TX:
            case INIT_DEVICE:
                try {
                    result = Daemon.commands.callAttr(strings[0], strings[1]).toString();
                    Log.i(TAG, "doInBackground:$$$$$$$$$$$$$$$$$$$$$$$$$$$$$--- "+result);
                } catch (Exception e) {
                    cancel(true);
                    onException(e);
                }
                break;
        }
        return result;
    }

    private void onException(Exception e) {
        Log.e(TAG, e.getMessage() == null ? "unknown exception" : e.getMessage());
        //  if ("BaseException: waiting passphrase timeout".equals(e.getMessage()) || "BaseException: waiting pin timeout".equals(e.getMessage())) {
        // 操作超时，忽略！！！！！！
        // } else {
        helper.onException(e);
        //  }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        helper.onCancelled();
        // EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        // EventBus.getDefault().unregister(this);
        helper.onResult(s);
    }

    /*@Subscribe
    public void onCancel(CancelEvent cancelEvent) {
        cancel(true);
    }*/
    public interface Helper {
        void onPreExecute();

        void onException(Exception e);

        void onResult(String s);

        void onCancelled();
    }

}
