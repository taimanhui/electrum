package org.haobtc.wallet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.gyf.immersionbar.ImmersionBar;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SettingActivity extends BaseActivity {

    @BindView(R.id.tet_s1)
    TextView tetS1;
    @BindView(R.id.tet_s2)
    TextView tetS2;
    @BindView(R.id.tet_s3)
    TextView tetS3;
    @BindView(R.id.tet_s4)
    TextView tetS4;
    @BindView(R.id.tet_s5)
    TextView tetS5;
    @BindView(R.id.tet_s6)
    TextView tetS6;
    @BindView(R.id.tet_s7)
    TextView tetS7;

    @Override
    public int getLayoutId() {
        return R.layout.setting;
    }

    public void initView() {
        ButterKnife.bind(this);
        CommonUtils.enableToolBar(this, R.string.settings);
    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.tet_s1, R.id.tet_s2, R.id.tet_s3, R.id.tet_s4, R.id.tet_s5, R.id.tet_s6, R.id.tet_s7})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tet_s1:
                Intent intent1 = new Intent(this, HardwareInfoActivity.class);
                startActivity(intent1);
                break;
            case R.id.tet_s2:
                Intent intent2 = new Intent(this, MessageManagerActivity.class);
                startActivity(intent2);
                break;
            case R.id.tet_s3:
                Intent intent3 = new Intent(this, LanguageSettingActivity.class);
                startActivity(intent3);
                break;
            case R.id.tet_s4:
                Intent intent4 = new Intent(this, ServerSettingActivity.class);
                startActivity(intent4);
                break;
            case R.id.tet_s5:
                Intent intent5 = new Intent(this, TransactionsSettingActivity.class);
                startActivity(intent5);
                break;
            case R.id.tet_s6:
                    Intent intent6 = new Intent(this, ServiceOnlineActivity.class);
                    startActivity(intent6);
                break;
            case R.id.tet_s7:
                Intent intent7 = new Intent(this, AboutActivity.class);
                startActivity(intent7);
                break;
        }
    }
}
