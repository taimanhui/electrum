package org.haobtc.wallet.activities.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.event.FastPayEvent;
import org.haobtc.wallet.event.HandlerEvent;

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
        EventBus.getDefault().register(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        boolean boPIN_set = preferences.getBoolean("boPIN_set", false);
        boolean noHard_set = preferences.getBoolean("noHard_set", false);
        String base_unit = preferences.getString("base_unit", "mBTC");
        payUnit.setText(base_unit);
        if (boPIN_set) {
            switchNoPin.setChecked(true);
            noPIN = "true";
        } else {
            switchNoPin.setChecked(false);
            noPIN = "false";
        }
        if (noHard_set) {
            switchNoHard.setChecked(true);
            noHard = "true";
        } else {
            switchNoHard.setChecked(false);
            noHard = "false";
        }
        TextWatcher1 textWatcher1 = new TextWatcher1();
        unclassifiedPay.addTextChangedListener(textWatcher1);
        editTimes.addTextChangedListener(textWatcher1);
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

    class TextWatcher1 implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence s, int i, int i1, int i2) {
            if (s.toString().contains(".")) {
                if (s.length() - 1 - s.toString().indexOf(".") > 7) {
                    s = s.toString().subSequence(0,
                            s.toString().indexOf(".") + 8);
                    unclassifiedPay.setText(s);
                    unclassifiedPay.setSelection(s.length());
                }
            }
            if (s.toString().trim().substring(0).equals(".")) {
                s = "0" + s;
                unclassifiedPay.setText(s);
                unclassifiedPay.setSelection(2);
            }
            if (s.toString().startsWith("0")
                    && s.toString().trim().length() > 1) {
                if (!s.toString().substring(1, 2).equals(".")) {
                    unclassifiedPay.setText(s.subSequence(0, 1));
                    unclassifiedPay.setSelection(1);
                    return;
                }
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
            if ((unclassifiedPay.length() > 0 && editTimes.length() > 0)) {
                btnSetKey.setEnabled(true);
                btnSetKey.setBackground(getDrawable(R.drawable.button_bk));
            } else {
                btnSetKey.setEnabled(false);
                btnSetKey.setBackground(getDrawable(R.drawable.button_bk_grey));
            }
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doBusiness(FastPayEvent event) {
        if (!TextUtils.isEmpty(event.getCode())) {
            if (event.getCode().equals("1")) {
                unclassifiedPay.setText("");
                editTimes.setText("");
                mToast(getString(R.string.set_success));
            }else{
                mToast(getString(R.string.set_fail));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
