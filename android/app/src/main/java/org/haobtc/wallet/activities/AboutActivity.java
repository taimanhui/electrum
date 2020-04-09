package org.haobtc.wallet.activities;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AsyncPlayer;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.listener.OnButtonClickListener;
import com.azhon.appupdate.listener.OnDownloadListener;
import com.azhon.appupdate.manager.DownloadManager;
import com.azhon.appupdate.utils.ApkUtil;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutActivity extends BaseActivity implements OnButtonClickListener, OnDownloadListener {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.attempt_update)
    RelativeLayout update;
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

    }
    private void attemptUpdate() {
        // todo: 通过服务器获取最新版本的版本信息
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
