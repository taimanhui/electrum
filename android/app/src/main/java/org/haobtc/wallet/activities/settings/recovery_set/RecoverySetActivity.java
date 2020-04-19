package org.haobtc.wallet.activities.settings.recovery_set;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.asynctask.BusinessAsyncTask;
import org.haobtc.wallet.event.ResultEvent;
import org.haobtc.wallet.utils.NfcUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;



public class RecoverySetActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener, BusinessAsyncTask.Helper {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_backups)
    TextView tetBackups;
    @BindView(R.id.reset_device)
    Button rest_device;
    @BindView(R.id.checkbox_Know)
    CheckBox checkboxKnow;
    public static final String TAG = "org.haobtc.wallet.activities.settings.recovery_set.RecoverySetActivity";

    @Override
    public int getLayoutId() {
        return R.layout.activity_recovery_set;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {
        checkboxKnow.setOnCheckedChangeListener(this);

    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.tet_backups, R.id.reset_device})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_backups:
                mIntent(BackupRecoveryActivity.class);
                break;
            case R.id.reset_device:
                Intent intent = new Intent(this, CommunicationModeSelector.class);
                intent.putExtra("tag", TAG);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked){
            rest_device.setBackground(getDrawable(R.drawable.button_bk));
            rest_device.setEnabled(true);
        }else{
            rest_device.setBackground(getDrawable(R.drawable.button_bk_grey));
            rest_device.setEnabled(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NfcUtils.mNfcAdapter = null;
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }

    @Override
    public void onPreExecute() {

    }

    @Override
    public void onException(Exception e) {
    }

    @Override
    public void onResult(String s) {
        EventBus.getDefault().post(new ResultEvent(s));
    }

    @Override
    public void onCancelled() {

    }
}
