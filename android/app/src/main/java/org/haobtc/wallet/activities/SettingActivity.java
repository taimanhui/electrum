package org.haobtc.wallet.activities;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.set.BixinKEYMenageActivity;
import org.haobtc.wallet.activities.set.CurrencyActivity;
import org.haobtc.wallet.activities.set.recovery_set.Backup_recoveryActivity;

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
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_bixinKey)
    TextView tetBixinKey;
    @BindView(R.id.tet_Faru)
    TextView tetFaru;

    @Override
    public int getLayoutId() {
        return R.layout.setting;
    }

    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.tet_s1, R.id.tet_s2, R.id.tet_s3, R.id.tet_s4, R.id.tet_s5, R.id.tet_s6, R.id.tet_s7, R.id.img_back, R.id.tet_bixinKey, R.id.tet_Faru})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tet_bixinKey:
                mIntent(BixinKEYMenageActivity.class);
                break;
            case R.id.tet_s1:
                mIntent(HardwareInfoActivity.class);
                break;
            case R.id.tet_s2:
                mIntent(Backup_recoveryActivity.class);
                break;
            case R.id.tet_s3:
                mIntent(LanguageSettingActivity.class);
                break;
            case R.id.tet_s4:
                mIntent(ServerSettingActivity.class);
                break;
            case R.id.tet_s5:
                mIntent(TransactionsSettingActivity.class);
                break;
            case R.id.tet_s6:
               mIntent(ServiceOnlineActivity.class);
                break;
            case R.id.tet_s7:
                mIntent(AboutActivity.class);
                break;
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_Faru:
                mIntent(CurrencyActivity.class);
                break;
        }
    }

}
