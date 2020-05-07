package org.haobtc.wallet.activities.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.listener.OnDownloadListener;
import com.azhon.appupdate.manager.DownloadManager;
import com.chaquo.python.PyObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.event.ExceptionEvent;
import org.haobtc.wallet.event.ExecuteEvent;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;
import org.haobtc.wallet.utils.NfcUtils;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class UpgradeBixinKEYActivity extends BaseActivity implements OnDownloadListener {

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
    public static final String TAG = UpgradeBixinKEYActivity.class.getSimpleName();
    private boolean done;
    private DownloadManager manager;

    private void updateFiles() {
        // todo: 从服务器获取最新 版本信息
        UpdateConfiguration configuration = new UpdateConfiguration()
                .setEnableLog(true)
                .setJumpInstallPage(false)
                .setShowBgdToast(true)
                .setOnDownloadListener(this);
        manager = DownloadManager.getInstance(this);
        manager.setConfiguration(configuration)
                .setApkName(tag == 1  ? "bixin.bin" : "bixin.zip")
                .setApkUrl(tag == 1 ? "https://key.bixin.com/bixin.bin" : "https://key.bixin.com/bixin.zip")
                .setShowNewerToast(true)
                .setSmallIcon(R.drawable.app_icon)
                .setApkMD5("")
                .download();
    }

    @Override
    public void start() {
        tetUpgradeTest.setText("正在下载最新的升级文件");
    }

    @Override
    public void downloading(int max, int progress) {

    }

    @Override
    public void done(File apk) {
        done = true;

    }

    @Override
    public void cancel() {

    }

    @Override
    public void error(Exception e) {

    }

    public class MyTask extends AsyncTask<String, Object, Void> {
        @Override
        protected void onPreExecute() {
            /*updateFiles();
            for (;;) {
               if (done) {
                   break;
               }
            }*/
            progressUpgrade.setIndeterminate(true);
            tetUpgradeTest.setText(getString(R.string.upgradeing));
        }

        @Override
        protected Void doInBackground(String... params) {
            PyObject protocol = Global.py.getModule("trezorlib.transport.protocol");
                try {
                        protocol.put("PROCESS_REPORTER", this);
                        switch (tag) {
                            case 1:
                                if (TextUtils.isEmpty(VersionUpgradeActivity.filePath)) {
                                    File file = new File(String.format("%s/bixin.bin", getExternalCacheDir().getPath()));
                                    if (!file.exists()) {
                                        showPromptMessage(R.string.update_file_not_exist);
                                        cancel(true);
                                    }
                                }
                                Daemon.commands.callAttr("firmware_update", TextUtils.isEmpty(VersionUpgradeActivity.filePath) ? String.format("%s/bixin.bin", getExternalCacheDir().getPath()) : VersionUpgradeActivity.filePath, params[0]);
                                break;
                            case 2:
                                if (TextUtils.isEmpty(VersionUpgradeActivity.filePath)) {
                                    File file = new File(String.format("%s/bixin.zip", getExternalCacheDir().getPath()));
                                    if (!file.exists()) {
                                        showPromptMessage(R.string.update_file_not_exist);
                                        cancel(true);
                                    }
                                } else if (!VersionUpgradeActivity.filePath.endsWith(".zip")) {
                                    showPromptMessage(R.string.update_file_format_error);
                                    cancel(true);
                                }
                                Daemon.commands.callAttr("firmware_update", TextUtils.isEmpty(VersionUpgradeActivity.filePath) ? String.format("%s/bixin.zip", getExternalCacheDir().getPath()) : VersionUpgradeActivity.filePath, params[0], "ble_ota");
                                break;
                            default:
                        }

                } catch (Exception e) {
                    e.printStackTrace();
                    // clear state
                    protocol.put("HTTP", false);
                    protocol.put("OFFSET", 0);
                    protocol.put("PROCESS_REPORTER", null);
                    cancel(true);
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
            VersionUpgradeActivity.filePath = "";
            mIntent(UpgradeFinishedActivity.class);
            finish();
        }

        @Override
        protected void onCancelled() {
            VersionUpgradeActivity.filePath = "";
            tetUpgradeTest.setText(getString(R.string.Cancelled));
            showPromptMessage(R.string.update_failed);
            new Handler().postDelayed(UpgradeBixinKEYActivity.this::finish, 2000);
        }
    }

    private void showPromptMessage(@StringRes int id) {
        UpgradeBixinKEYActivity.this.runOnUiThread(() -> {
            Toast.makeText(UpgradeBixinKEYActivity.this, id, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_upgrade_bixin_key;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        tag = getIntent().getIntExtra("tag", 1);
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
            if (VersionUpgradeActivity.UPDATE_PROCESS.equals(intent.getAction())) {
                int percent = intent.getIntExtra("process", 0);
                progressUpgrade.setIndeterminate(false);
                progressUpgrade.setProgress(percent);
                tetUpgradeNum.setText(String.format("%s%%", String.valueOf(percent)));
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void executeTask(ExecuteEvent executeEvent) {
        mTask.execute("nfc");
        EventBus.getDefault().removeStickyEvent(ExecuteEvent.class);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDfuException(ExceptionEvent event) {
        VersionUpgradeActivity.filePath = "";
        tetUpgradeTest.setText(getString(R.string.Cancelled));
        showPromptMessage(R.string.update_failed);
        new Handler().postDelayed(UpgradeBixinKEYActivity.this::finish, 2000);
    }
    @SingleClick
    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.img_back) {
            finish();
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (tag == 2) {
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(VersionUpgradeActivity.UPDATE_PROCESS));
            progressUpgrade.setIndeterminate(true);
        }
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (tag == 2) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        }
        mTask = null;
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }
}
