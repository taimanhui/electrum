package org.haobtc.wallet.activities;

import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.listener.OnButtonClickListener;
import com.azhon.appupdate.listener.OnDownloadListener;
import com.azhon.appupdate.manager.DownloadManager;
import com.azhon.appupdate.utils.ApkUtil;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.aop.SingleClick;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutActivity extends BaseActivity implements OnButtonClickListener, OnDownloadListener {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.attempt_update)
    RelativeLayout update;
    @BindView(R.id.version_code)
    TextView versionCodetext;
    @BindView(R.id.update_version)
    TextView updateVersion;
    private DownloadManager manager;

    @Override
    public int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {
        String versionName = ApkUtil.getVersionName(this);
        versionCodetext.setText(String.format("V%s", versionName));
        updateVersion.setText(String.format("V%s", versionName));
    }

    private void attemptUpdate() {
        // todo: Get the latest version information from the server
        int versionCode = ApkUtil.getVersionCode(this);
        String url = "https://key.bixin.com/bixinkey.apk";
        UpdateConfiguration configuration = new UpdateConfiguration()
                .setEnableLog(true)
                //.setHttpManager()
                .setJumpInstallPage(true)
                .setDialogButtonTextColor(Color.WHITE)
                .setDialogButtonColor(getColor(R.color.button_bk))
                .setDialogImage(R.drawable.update)
                .setShowNotification(true)
                .setShowBgdToast(true)
                .setForcedUpgrade(false)
                .setButtonClickListener(this)
                .setOnDownloadListener(this);

        manager = DownloadManager.getInstance(this);
        manager.setApkName("BixinKEY.apk")
                .setApkUrl(url)
                .setSmallIcon(R.drawable.app_icon)
                .setShowNewerToast(true)
                .setConfiguration(configuration)
                .setApkVersionCode(versionCode + 1)
                .setApkVersionName("2.1.8")
                .setApkSize("20.4")
                .setApkDescription(getString(R.string.introduce_detials))
//                .setApkMD5("DC501F04BBAA458C9DC33008EFED5E7F")
                .download();
    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.attempt_update})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.attempt_update:
                attemptUpdate();

        }
    }

    @Override
    public void onButtonClick(int id) {

    }

    @Override
    public void start() {

    }

    @Override
    public void downloading(int max, int progress) {

    }

    @Override
    public void done(File apk) {
        manager.release();
    }

    @Override
    public void cancel() {

    }

    @Override
    public void error(Exception e) {

    }

}
