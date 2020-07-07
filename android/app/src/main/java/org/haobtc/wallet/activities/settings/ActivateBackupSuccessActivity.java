package org.haobtc.wallet.activities.settings;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.BackupRecoveryLiteActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.aop.SingleClick;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActivateBackupSuccessActivity extends BaseActivity {

    @Override
    public int getLayoutId() {
        return R.layout.activity_activite_backup_success;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {

    }
    @SingleClick
    @OnClick({R.id.img_back, R.id.btn_finish, R.id.backup_to_new})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_finish:
                finish();
                break;
            case R.id.backup_to_new:
                Intent intent = new Intent(this, BackupRecoveryLiteActivity.class);
                intent.putExtra("extras", getIntent().getStringExtra("message"));
                intent.setAction("recovery");
                startActivity(intent);
            default:
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }
}
