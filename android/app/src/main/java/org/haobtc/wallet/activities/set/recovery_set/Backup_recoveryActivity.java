package org.haobtc.wallet.activities.set.recovery_set;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.MessageManagerActivity;
import org.haobtc.wallet.activities.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Backup_recoveryActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_keyName)
    TextView tetKeyName;
    @BindView(R.id.lin_OnckTwo)
    LinearLayout linOnckTwo;

    @Override
    public int getLayoutId() {
        return R.layout.activity_backup_recovery;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_back, R.id.tet_keyName, R.id.lin_OnckTwo})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_keyName:
                mIntent(BackupMessageActivity.class);
                break;
            case R.id.lin_OnckTwo:
                mIntent(MessageManagerActivity.class);
                break;
        }
    }
}
