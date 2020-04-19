package org.haobtc.wallet.activities.personalwallet.hidewallet;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.utils.Global;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class HideWalletSetPassActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.editNewPass)
    EditText editNewPass;
    @BindView(R.id.editOldPass)
    EditText editOldPass;
    @BindView(R.id.bn_next)
    Button bnNext;
    @BindView(R.id.linearNextInputPass)
    LinearLayout linearNextInputPass;
    private String createOrcheck;

    @Override
    public int getLayoutId() {
        return R.layout.activity_hide_wallet_set_pass;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        createOrcheck = preferences.getString("createOrcheck", "");//createOrcheck -->  judge create hide wallet or check hide wallet
        if (createOrcheck.equals("check")) {//check -->  is check hide wallet
            linearNextInputPass.setVisibility(View.GONE);
        }
    }

    @Override
    public void initData() {

    }
    @SingleClick
    @OnClick({R.id.img_back, R.id.bn_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                Global.py.getModule("trezorlib.customer_ui").get("CustomerUI").put("user_cancel", 1);
                finish();
                break;
            case R.id.bn_next:
                String strNewpass = editNewPass.getText().toString();
                String strNextpass = editOldPass.getText().toString();
                if (createOrcheck.equals("check")) {
                    if (TextUtils.isEmpty(strNewpass)) {
                        mToast(getString(R.string.please_input_pass));
                        return;
                    }
                    Intent intent = new Intent();
                    intent.putExtra("passphrase", strNewpass);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                } else {
                    if (TextUtils.isEmpty(strNewpass)) {
                        mToast(getString(R.string.please_input_pass));
                        return;
                    }
                    if (TextUtils.isEmpty(strNextpass)) {
                        mToast(getString(R.string.please_next_input_pass));
                        return;
                    }
                    if (!strNewpass.equals(strNextpass)) {
                        mToast(getString(R.string.two_different_pass));
                        return;
                    }
                    Intent intent = new Intent();
                    intent.putExtra("passphrase", strNewpass);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }

                break;
        }
    }

}
