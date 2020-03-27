package org.haobtc.wallet.activities.personalwallet.hidewallet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;

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

    @Override
    public int getLayoutId() {
        return R.layout.activity_hide_wallet_set_pass;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_back, R.id.bn_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.bn_next:
                String strNewpass = editNewPass.getText().toString();
                String strNextpass = editOldPass.getText().toString();
                if (TextUtils.isEmpty(strNewpass)){
                    mToast(getString(R.string.please_input_pass));
                    return;
                }
                if (TextUtils.isEmpty(strNextpass)){
                    mToast(getString(R.string.please_next_input_pass));
                    return;
                }
                if (!strNewpass.equals(strNextpass)){
                    mToast(getString(R.string.two_different_pass));
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("passphrase", editNewPass.getText().toString());
                setResult(Activity.RESULT_OK, intent);
                finish();
                break;
        }
    }
}
