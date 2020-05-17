package org.haobtc.wallet.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.settings.recovery_set.RecoveryActivity;
import org.haobtc.wallet.aop.SingleClick;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WalletUnActivatedActivity extends BaseActivity {
    public static final String TAG = "org.wallet.activities.WalletUnActivatedActivity";
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.button_activate)
    Button buttonActivate;
    @BindView(R.id.button_recover)
    Button buttonRecover;
    @BindView(R.id.use_se)
    CheckBox useSe;
    private String tag_xpub = "";
    private SharedPreferences.Editor edit;
    private boolean active_set_pin;


    @Override
    public int getLayoutId() {
        return R.layout.activate;
    }

    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        active_set_pin = preferences.getBoolean("Active_set_PIN", false);
        Intent intent = getIntent();
        tag_xpub = intent.getStringExtra("tag_Xpub");
    }

    @Override
    public void initData() {

    }


    @SingleClick
    @OnClick({R.id.img_back, R.id.button_activate, R.id.button_recover})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.button_activate:
                if (!active_set_pin) {
                    edit.putBoolean("Active_set_PIN", true);
                    edit.apply();
                }
                Intent intent = new Intent(this, ActivatedProcessing.class);
                intent.putExtra("tag_xpub", tag_xpub);
                intent.putExtra("use_se", useSe.isChecked());
                startActivity(intent);
                finish();
                break;
            case R.id.button_recover:
                startActivity(new Intent(this, RecoveryActivity.class));
                finish();
        }
    }
}
