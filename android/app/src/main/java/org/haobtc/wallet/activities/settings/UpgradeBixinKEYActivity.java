package org.haobtc.wallet.activities.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

import com.chaquo.python.PyObject;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.bean.HardwareFeatures;
import org.haobtc.wallet.bean.UpdateInfo;
import org.haobtc.wallet.dfu.service.DfuService;
import org.haobtc.wallet.event.DfuEvent;
import org.haobtc.wallet.event.ExceptionEvent;
import org.haobtc.wallet.event.ExecuteEvent;
import org.haobtc.wallet.fragment.BleDeviceRecyclerViewAdapter;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.heaton.blelibrary.ble.model.BleDevice;
import no.nordicsemi.android.dfu.DfuBaseService;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.executorService;
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.futureTask;


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
    String nrfVersion;
    String loaderVersion;

    public static final String TAG = UpgradeBixinKEYActivity.class.getSimpleName();

    private HardwareFeatures getFeatures(String path) throws Exception {
        String feature;
        try {
            futureTask = new FutureTask<>(() -> Daemon.commands.callAttr("get_feature", path));
            executorService.submit(futureTask);
            feature = futureTask.get(5, TimeUnit.SECONDS).toString();
            return new Gson().fromJson(feature, HardwareFeatures.class);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            Toast.makeText(this, getString(R.string.no_message), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            throw e;
        }
    }

    public class MyTask extends AsyncTask<String, Object, Void> {
        @Override
        protected void onPreExecute() {
            progressUpgrade.setIndeterminate(true);
            tetUpgradeTest.setText(getString(R.string.upgradeing));

        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                HardwareFeatures features = getFeatures(params[0]);
                 nrfVersion = features.getBleVer();
                 loaderVersion = String.format("%s.%s.%s", features.getMajorVersion(), features.getMinorVersion(), features.getPatchVersion());
            } catch (Exception e) {
                e.printStackTrace();
                cancel(true);
            }
            getUpdateInfo();
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
            finishAffinity();
        }

        @Override
        protected void onCancelled() {
            VersionUpgradeActivity.filePath = "";
            tetUpgradeTest.setText(getString(R.string.Cancelled));
            showPromptMessage(R.string.update_failed);
            new Handler().postDelayed(UpgradeBixinKEYActivity.this::finish, 2000);
        }
        private void getUpdateInfo() {
            // version_testnet.json version_regtest.json
            String url = "https://key.bixin.com/version.json";
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder().url(url).build();
            Call call = okHttpClient.newCall(request);
            runOnUiThread(() -> Toast.makeText(UpgradeBixinKEYActivity.this, "正在检查更新信息", Toast.LENGTH_LONG).show());
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("获取更新信息失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    assert response.body() != null;
                    SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
                    String locate = preferences.getString("language", "");
                    String info = response.body().string();
                    UpdateInfo updateInfo = UpdateInfo.objectFromData(info);
                    String urlNrf = updateInfo.getNrf().getUrl();
                    String versionNrf = updateInfo.getNrf().getVersion();
                    String versionStm32 = updateInfo.getStm32().getBootloaderVersion().toString();
                    int i = versionNrf.compareTo(nrfVersion);
                    int n = versionStm32.compareTo(loaderVersion);

                    String descriptionNrf = "English".equals(locate) ? updateInfo.getNrf().getChangelogEn() : updateInfo.getNrf().getChangelogCn();
                    String urlStm32 = updateInfo.getStm32().getUrl();
                    String descriptionStm32 = "English".equals(locate) ? updateInfo.getStm32().getChangelogEn() : updateInfo.getNrf().getChangelogCn();
                    if (tag == 1) {
                        if (n > 0) {
                            updateFiles(String.format("https://key.bixin.com/%s", urlStm32));
                        } else {
                            runOnUiThread(() -> Toast.makeText(UpgradeBixinKEYActivity.this, "已是最新版本，无需升级", Toast.LENGTH_LONG).show());
                        }
                    } else {
                        if (i > 0) {
                            updateFiles(String.format("https://key.bixin.com/%s", urlNrf));
                        } else {
                            runOnUiThread(() -> Toast.makeText(UpgradeBixinKEYActivity.this, "已是最新版本，无需升级", Toast.LENGTH_LONG).show());
                        }
                    }
                }
            });
        }
        private void updateFiles(String url) {
            OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
            Request request = new Request.Builder().url(url).build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    System.out.println("文件下载失败");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String name = "";
                    if (tag == 1) {
                        name = "bixin.bin";
                    } else {
                        name = "bixin.zip";
                    }
                    File file = new File(String.format("%s/%s", getExternalCacheDir().getPath(), name));
                    byte[] buf = new byte[2048];
                    int len = 0;
                    assert response.body() != null;
                    try (InputStream is = response.body().byteStream(); FileOutputStream fos = new FileOutputStream(file)) {
                        while ((len = is.read(buf)) != -1) {
                            fos.write(buf, 0, len);
                        }
                        fos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
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
        EventBus.getDefault().register(this);
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
        tetUpgradeTest.setText(getString(R.string.Cancelled));
        showPromptMessage(R.string.update_failed);
        new Handler().postDelayed(UpgradeBixinKEYActivity.this::finish, 2000);
    }
    @SingleClick
    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.img_back) {
            finish();
            final Intent pauseAction = new Intent(DfuBaseService.BROADCAST_ACTION);
            pauseAction.putExtra(DfuBaseService.EXTRA_ACTION, DfuBaseService.ACTION_ABORT);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pauseAction);
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onDfu(DfuEvent event) {
        if (event.getType() == DfuEvent.DFU_SHOW_PROCESS) {
            dfu();
            EventBus.getDefault().removeStickyEvent(DfuEvent.class);
        }
    }
    private void dfu() {
        BleDevice device = BleDeviceRecyclerViewAdapter.mBleDevice;
            final DfuServiceInitiator starter = new DfuServiceInitiator(device.getBleAddress());
            starter.setDeviceName(device.getBleName());
            starter.setKeepBond(true);
        /*
           Call this method to put Nordic nrf52832 into bootloader mode
        */
            starter.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true);
            if (TextUtils.isEmpty(VersionUpgradeActivity.filePath)) {
                File file = new File(String.format("%s/bixin.zip", getExternalCacheDir().getPath()));
                if (!file.exists()) {
                    Toast.makeText(this, R.string.update_file_not_exist, Toast.LENGTH_LONG).show();

                    EventBus.getDefault().post(new ExecuteEvent());
                    finish();
                    return;
                }
            } else if (!VersionUpgradeActivity.filePath.endsWith(".zip")) {
                Toast.makeText(this, R.string.update_file_format_error, Toast.LENGTH_LONG).show();
                EventBus.getDefault().post(new ExecuteEvent());
                finish();
                return;
            }
            starter.setZip(null, TextUtils.isEmpty(VersionUpgradeActivity.filePath) ? String.format("%s/bixin.zip", getExternalCacheDir().getPath()) : VersionUpgradeActivity.filePath);
            DfuServiceInitiator.createDfuNotificationChannel(this);
            starter.start(this, DfuService.class);

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (tag == 2) {
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(VersionUpgradeActivity.UPDATE_PROCESS));
            progressUpgrade.setIndeterminate(true);
        }
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
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }
}
