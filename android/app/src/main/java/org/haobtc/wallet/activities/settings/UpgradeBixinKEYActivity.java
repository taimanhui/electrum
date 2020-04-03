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

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.chaquo.python.PyObject;
import com.google.gson.Gson;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.bean.HardwareFeatures;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;
import org.haobtc.wallet.utils.NfcUtils;


import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.executorService;


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

    private boolean isBootloaderMode() throws Exception {
        String feature;
        try {
            feature = executorService.submit(() -> Daemon.commands.callAttr("get_feature", "bluetooth")).get().toString();
            HardwareFeatures features = new Gson().fromJson(feature, HardwareFeatures.class);
            return features.isBootloaderMode();

        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            throw e;
        }
    }

    public class MyTask extends AsyncTask<String, Object, Void> {
        @Override
        protected void onPreExecute() {
            // todo: 下载升级文件
            progressUpgrade.setIndeterminate(true);
            tetUpgradeTest.setText(getString(R.string.upgradeing));
        }

        @SuppressLint("SdCardPath")
        @Override
        protected Void doInBackground(String... params) {
            PyObject progress = Global.py.getModule("trezorlib.transport.protocol");
            progress.put("PROCESS_REPORTER", this);
            if (tag == 1) { // 固件
                try {
                    boolean ready = isBootloaderMode();
                    if (ready) {
                        Daemon.commands.callAttr("firmware_update", "/sdcard/Android/data/org.haobtc.wallet/files/trezor.bin", params[0]);
                    } else {
                        showPromptMessage();
                        cancel(true);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    cancel(true);
                    showErrorMessage();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Object... progresses) {
            progressUpgrade.setIndeterminate(false);
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
        }
    }
    private void showPromptMessage() {
        UpgradeBixinKEYActivity.this.runOnUiThread(() -> {
            Toast.makeText(UpgradeBixinKEYActivity.this, "请确认硬件处于BootLoader模式", Toast.LENGTH_SHORT).show();
        });
    }
    private void showErrorMessage() {
        UpgradeBixinKEYActivity.this.runOnUiThread(() -> {
            Toast.makeText(UpgradeBixinKEYActivity.this, "升级失败,将在2秒内退出此界面", Toast.LENGTH_SHORT).show();
        });
    }
    @Override
    public int getLayoutId() {
        return R.layout.activity_upgrade_bixin_key;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        tag = getIntent().getIntExtra("tag", 0);
        NfcUtils.nfc(this, false);
    }

    @Override
    public void initData() {
        mTask = new MyTask();
        if ("bluetooth".equals(getIntent().getStringExtra("way"))) {
            mTask.execute("bluetooth");
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (EXECUTE_TASK.equals(intent.getAction())) {
                mTask.execute("nfc");
            } else if (VersionUpgradeActivity.UPDATE_PROCESS.equals(intent.getAction())) {
                int percent = intent.getIntExtra("process", 0);
                progressUpgrade.setIndeterminate(false);
                progressUpgrade.setProgress(percent);
                tetUpgradeNum.setText(String.format("%s%%", String.valueOf(percent)));
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
        if (tag == 2) {
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(VersionUpgradeActivity.UPDATE_PROCESS));
            progressUpgrade.setIndeterminate(true);
        }
        registerReceiver(receiver, new IntentFilter(EXECUTE_TASK));
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
        if (tag == 2) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        }
        mTask = null;
        unregisterReceiver(receiver);
    }

}
