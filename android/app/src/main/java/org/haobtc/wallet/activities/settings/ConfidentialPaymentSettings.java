package org.haobtc.wallet.activities.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.aop.SingleClick;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ConfidentialPaymentSettings extends BaseActivity {

    public static final String TAG = ConfidentialPaymentSettings.class.getSimpleName();
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.switch_noPin)
    Switch switchNoPin;
    @BindView(R.id.switch_noHard)
    Switch switchNoHard;
    @BindView(R.id.unclassified_pay)
    EditText unclassifiedPay;
    @BindView(R.id.pay_unit)
    TextView payUnit;
    @BindView(R.id.edit_times)
    EditText editTimes;
    @BindView(R.id.btn_set_key)
    Button btnSetKey;
    private String noPIN = "false";
    private String noHard = "false";

    @Override
    public int getLayoutId() {
        return R.layout.activity_no_secret_payment;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        boolean boPIN_set = preferences.getBoolean("boPIN_set", false);
        boolean noHard_set = preferences.getBoolean("noHard_set", false);
        String base_unit = preferences.getString("base_unit", "mBTC");
        payUnit.setText(base_unit);
        if (boPIN_set) {
            switchNoPin.setChecked(true);
        } else {
            switchNoPin.setChecked(false);
        }
        if (noHard_set) {
            switchNoHard.setChecked(true);
        } else {
            switchNoHard.setChecked(false);
        }

    }

    @Override
    public void initData() {
        switchNoPinStatus();
    }

    private void switchNoPinStatus() {
        switchNoPin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    noPIN = "true";
                } else {
                    noPIN = "false";
                }
            }
        });
        switchNoHard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    noHard = "true";
                } else {
                    noHard = "false";
                }
            }
        });
    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.btn_set_key})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_set_key:
                Intent intent = new Intent(this, CommunicationModeSelector.class);
                intent.putExtra("tag", TAG);
                intent.putExtra("limit", unclassifiedPay.getText().toString());
                intent.putExtra("times", editTimes.getText().toString());
                intent.putExtra("noPIN", noPIN);
                intent.putExtra("noHard", noHard);
                startActivity(intent);
                break;

        }
    }

}
