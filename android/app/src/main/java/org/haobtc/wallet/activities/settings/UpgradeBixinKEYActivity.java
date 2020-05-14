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
import com.google.common.base.Strings;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.base.MyApplication;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
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
import static org.haobtc.wallet.activities.service.CommunicationModeSelector.isDfu;


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
    private String newNrfVersion;
    private String newLoaderVersion;
    private boolean isNew;


    public static final String TAG = UpgradeBixinKEYActivity.class.getSimpleName();

    private HardwareFeatures getFeatures(String path) throws Exception {
        String feature;
        try {
            futureTask = new FutureTask<>(() -> Daemon.commands.callAttr("get_feature", path));
            executorService.submit(futureTask);
            feature = futureTask.get(5, TimeUnit.SECONDS).toString();
            return new Gson().fromJson(feature, HardwareFeatures.class);
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
         //   Toast.makeText(this, getString(R.string.no_message), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            throw e;
        }
    }

    public class MyTask extends AsyncTask<String, Object, Void> {
        @Override
        protected void onPreExecute() {
            progressUpgrade.setIndeterminate(true);
            switch (tag) {
                case 1:
                    tetUpgradeTest.setText("v" + getIntent().getExtras().getString("stm32_version"));
                    break;
                case 2:
                    tetUpgradeTest.setText("v" + getIntent().getExtras().getString("nrf_version"));
            }

        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                if (TextUtils.isEmpty(VersionUpgradeActivity.filePath)) {
                    HardwareFeatures features = getFeatures(params[0]);
                    String nrfVersion = features.getBleVer();
                    String loaderVersion = String.format("%s.%s.%s", features.getMajorVersion(), features.getMinorVersion(), features.getPatchVersion());
                    switch (tag) {
                        case 1:
                            assert newLoaderVersion != null;
                            if (newLoaderVersion.compareTo(loaderVersion) <= 0) {
                                isNew = true;
                                cancel(true);
                            } else {
                                runOnUiThread(() -> tetUpgradeTest.setText("正在下载升级文件"));
                                updateFiles(getIntent().getExtras().getString("stm32_url"));
                                doUpdate(params[0]);
                            }
                            break;
                        case 2:
                            assert newNrfVersion != null;
                            if (newNrfVersion.compareTo(nrfVersion) <= 0) {
                                isNew = true;
                                cancel(true);
                            } else {
                                runOnUiThread(() -> tetUpgradeTest.setText("正在下载升级文件"));
                                updateFiles(getIntent().getExtras().getString("nrf_url"));
                                doUpdate(params[0]);
                            }
                    }
                } else {
                    doUpdate(params[0]);
                }
            } catch (Exception e) {
                e.printStackTrace();
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

        private void doUpdate(String path) {
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
                        Daemon.commands.callAttr("firmware_update", TextUtils.isEmpty(VersionUpgradeActivity.filePath) ? String.format("%s/bixin.bin", getExternalCacheDir().getPath()) : VersionUpgradeActivity.filePath, path);
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
                        Daemon.commands.callAttr("firmware_update", TextUtils.isEmpty(VersionUpgradeActivity.filePath) ? String.format("%s/bixin.zip", getExternalCacheDir().getPath()) : VersionUpgradeActivity.filePath, path, "ble_ota");
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
            if (isNew) {
                isNew = false;
                showPromptMessage(R.string.is_new);
            } else {
                showPromptMessage(R.string.update_failed);
            }
            new Handler().postDelayed(UpgradeBixinKEYActivity.this::finish, 2000);
        }

    }

    private void updateFiles(String url) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        String name = "";
        if (tag == 1) {
            name = "bixin.bin";
        } else {
            name = "bixin.zip";
        }
        File file = new File(String.format("%s/%s", getExternalCacheDir().getPath(), name));
        byte[] buf = new byte[2048];
        int len = 0;
        try (Response response = call.execute(); InputStream is = response.body().byteStream(); FileOutputStream fos = new FileOutputStream(file)) {
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        runOnUiThread(() -> tetUpgradeTest.setText("正在升级至 v" + newLoaderVersion));
        if (isDfu) {
            dfu();
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
        newNrfVersion = getIntent().getExtras().getString("nrf_version");
        newLoaderVersion = getIntent().getExtras().getString("stm32_version");
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
            if (Strings.isNullOrEmpty(VersionUpgradeActivity.filePath)) {
                SharedPreferences sharedPreferences = getSharedPreferences("devices", MODE_PRIVATE);
                String device = sharedPreferences.getString(BleDeviceRecyclerViewAdapter.mBleDevice.getBleName(), "");
                if (Strings.isNullOrEmpty(device)) {
                    showPromptMessage(R.string.un_bonded);
                    return;
                }
                HardwareFeatures features = new Gson().fromJson(device, HardwareFeatures.class);
                String nrfVersion = features.getBleVer();
                if (newNrfVersion.compareTo(nrfVersion) <= 0) {
                    showPromptMessage(R.string.is_new);
                    return;
                } else {
                    runOnUiThread(() -> tetUpgradeTest.setText("正在下载升级文件"));
                    executorService.execute(() -> updateFiles(getIntent().getExtras().getString("nrf_url")));
                }
            } else {
                dfu();
            }
            EventBus.getDefault().removeStickyEvent(DfuEvent.class);
        }
    }
    private void dfu() {
        BleDevice device = BleDeviceRecyclerViewAdapter.mBleDevice;
            final DfuServiceInitiator starter = new DfuServiceInitiator(device.getBleAddress());
            starter.setDeviceName(device.getBleName());
            starter.setKeepBond(false);
        /*
           Call this method to put Nordic nrf52832 into bootloader mode
        */
            starter.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true);
            if (TextUtils.isEmpty(VersionUpgradeActivity.filePath)) {
                File file = new File(String.format("%s/bixin.zip", getExternalCacheDir().getPath()));
                if (!file.exists()) {
                    Toast.makeText(this, R.string.update_file_not_exist, Toast.LENGTH_LONG).show();
//                    EventBus.getDefault().post(new ExecuteEvent());
                    finish();
                    return;
                }
            } else if (!VersionUpgradeActivity.filePath.endsWith(".zip")) {
                Toast.makeText(this, R.string.update_file_format_error, Toast.LENGTH_LONG).show();
//                EventBus.getDefault().post(new ExecuteEvent());
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
