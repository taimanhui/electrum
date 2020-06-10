package org.haobtc.wallet.activities.settings.recovery_set;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.event.ButtonRequestEvent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.features;

public class ResetDeviceActivity extends BaseActivity{
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_backups)
    TextView tetBackups;
    @BindView(R.id.reset_device)
    Button rest_device;
    @BindView(R.id.checkbox_Know)
    LinearLayout checkboxKnow;
    public static final String TAG = "org.haobtc.wallet.activities.settings.recovery_set.RecoverySetActivity";
    @BindView(R.id.img_choose)
    ImageView imgChoose;
    private int img = 1;

    @Override
    public int getLayoutId() {
        return R.layout.activity_recovery_set;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
    }

    @Override
    public void initData() {

    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.tet_backups, R.id.reset_device, R.id.checkbox_Know})
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
                intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("tag", TAG);
                startActivity(intent);
                break;
            case R.id.checkbox_Know:
                if (img == 1) {
                    img = 2;
                    imgChoose.setImageDrawable(getDrawable(R.drawable.chenggong));
                    rest_device.setBackground(getDrawable(R.drawable.button_bk));
                    rest_device.setEnabled(true);
                } else {
                    img = 1;
                    imgChoose.setImageDrawable(getDrawable(R.drawable.circle_empty));
                    rest_device.setBackground(getDrawable(R.drawable.button_bk_grey));
                    rest_device.setEnabled(false);
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onButtonRequest(ButtonRequestEvent event) {
        if (!features.isPinProtection()) {
            startActivity(new Intent(this, ResetDeviceSuccessActivity.class));
            finish();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }
}
