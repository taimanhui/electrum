package org.haobtc.wallet.activities.settings;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;
import org.haobtc.wallet.utils.NfcUtils;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UpgradeBixinKEYActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_test)
    TextView tetTest;
    @BindView(R.id.progressUpgrade)
    ProgressBar progressUpgrade;
    @BindView(R.id.tetUpgradeTest)
    TextView tetUpgradeTest;
    @BindView(R.id.tetUpgradeNum)
    TextView tetUpgradeNum;
    @BindView(R.id.imgdhksjks)
    ImageView imgdhksjks;
    private MyTask mTask;
    private int tag;
    public static final String EXECUTE_TASK = "org.haobtc.wallet.activities.settings.EXECUTE_TASK";
    public static final String TAG = UpgradeBixinKEYActivity.class.getSimpleName();

    public class MyTask extends AsyncTask<String, Object, Void> {
        @Override
        protected void onPreExecute() {
            // todo: 下载升级文件
            tetUpgradeTest.setText(getString(R.string.upgradeing));

        }

        @SuppressLint("SdCardPath")
        @Override
        protected Void doInBackground(String... params) {
            PyObject progress = Global.py.getModule("trezorlib.transport.protocol");
            progress.put("PROCESS_REPORTER", this);
            switch (tag) {
                case 1: // 固件
                    try {
                        Daemon.commands.callAttr("firmware_update", "/sdcard/Android/data/org.haobtc.wallet/files/trezor.bin", params[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        cancel(true);
                    }
                    break;
                case 2: // 蓝牙
                    try {
                        Daemon.commands.callAttr("firmware_update", "/sdcard/Android/data/org.haobtc.wallet/files/ble.bin", params[0], "ble");
                    } catch (Exception e) {
                        e.printStackTrace();
                        cancel(true);
                    }
                    break;
            }
            return null;
        }

        private void showPromptMessage() {
            UpgradeBixinKEYActivity.this.runOnUiThread(() -> {
                Toast.makeText(UpgradeBixinKEYActivity.this, "请确认硬件处于BootLoader模式", Toast.LENGTH_SHORT).show();
            });
        }

        @Override
        protected void onProgressUpdate(Object... progresses) {
            progressUpgrade.setProgress(Integer.parseInt(((progresses[0]).toString())));
            tetUpgradeNum.setText(String.format("%s%%", String.valueOf(progresses[0])));

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mIntent(UpgradeFinishedActivity.class);
            finish();
        }

        @Override
        protected void onCancelled() {
            tetUpgradeTest.setText(getString(R.string.cancled));
            showPromptMessage();
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_upgrade_bixin_key;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        tag = getIntent().getIntExtra("tag", 0);
        NfcUtils.nfc(this);
    }

    @Override
    public void initData() {
        mTask = new MyTask();
        registerReceiver(receiver, new IntentFilter(EXECUTE_TASK));
        if ("bluetooth".equals(getIntent().getStringExtra("way"))) {
            mTask.execute("bluetooth");
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (EXECUTE_TASK.equals(intent.getAction())) {
                mTask.execute("nfc");
            }
        }
    };

    @OnClick({R.id.img_back, R.id.imgdhksjks, R.id.tet_test})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.img_back) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // enable nfc discovery for the app
            Log.i("NFC", "为本App启用NFC感应");
            NfcUtils.mNfcAdapter.enableForegroundDispatch(this, NfcUtils.mPendingIntent, NfcUtils.mIntentFilter, NfcUtils.mTechList);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // disable nfc discovery for the app
            NfcUtils.mNfcAdapter.disableForegroundDispatch(this);
            Log.i("NFC", "禁用本App的NFC感应");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTask = null;
        unregisterReceiver(receiver);
    }
}
