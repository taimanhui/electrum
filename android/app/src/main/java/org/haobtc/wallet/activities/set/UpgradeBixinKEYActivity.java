package org.haobtc.wallet.activities.set;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.chaquo.python.PyObject;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;

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
    private boolean ifOnclick = false;
    private int tag;

    private class MyTask extends AsyncTask<String, Object, String> {
        @Override
        protected void onPreExecute() {
            tetUpgradeTest.setText(getString(R.string.upgradeing));

        }

        @SuppressLint("SdCardPath")
        @Override
        protected String doInBackground(String... params) {
            PyObject progress = Global.py.getModule("trezorlib.firmware");
            progress.put("PROCESS_REPORTER", this);
            switch (tag) {
                case 1: // 固件
                    try {
                        Log.i("PROCESS_REPORTER", "升级固件");
                        Daemon.commands.callAttr("firmware_update","/sdcard/Android/data/org.haobtc.wallet/files/trezor.bin", params[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("PROCESS_REPORTER", "固件"+e.getMessage());
//                        showPromptMessage();
                    }
                case 2: // 蓝牙
                    try {
                        Log.i("PROCESS_REPORTER", "升级蓝牙");
                        Daemon.commands.call("firmware_update", "/sdcard/ble.bin",params[0]);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("PROCESS_REPORTER", "蓝牙"+e.getMessage());
//                        showPromptMessage();
                    }
            }

            return null;
        }

        private void showPromptMessage() {
            UpgradeBixinKEYActivity.this.runOnUiThread(() -> {
                mToast( getString(R.string.confirm_NFC_pattern));
            });
        }

        @Override
        protected void onProgressUpdate(Object... progresses) {
            progressUpgrade.setProgress(Integer.parseInt(((Long)progresses[0]).toString()));
            tetUpgradeNum.setText(String.format("%s%%", String.valueOf(progresses[0])));

        }

        @Override
        protected void onPostExecute(String result) {
//            mIntent(UpgradeFinishedActivity.class);
//            finish();
        }

        @Override
        protected void onCancelled() {
            tetUpgradeTest.setText(getString(R.string.cancled));
            progressUpgrade.setProgress(0);

        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_upgrade_bixin_k_e_y;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        //judge to upgrade firmware or Bluetooth
        tag = getIntent().getIntExtra("tag", 0);

    }

    @Override
    public void initData() {
        if ("nfc".equals(getIntent().getStringExtra("way"))) {
            new MyTask().execute("nfc");
        }
    }

    @OnClick({R.id.img_back, R.id.imgdhksjks, R.id.tet_test})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.imgdhksjks:
//                if (!ifOnclick){
//                    new MyTask().execute();
//                    ifOnclick = true;
//                }
//                break;
//            case R.id.tet_test:
//                if (ifOnclick){
//                    new MyTask().cancel(true);
//                    ifOnclick = false;
//                }
//                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new MyTask().cancel(true);
    }
}
