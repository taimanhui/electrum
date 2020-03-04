package org.haobtc.wallet.activities.onlywallet.mnemonic_word;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreatePersonalHelpPassActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.edt_Pass1)
    EditText edtPass1;
    @BindView(R.id.edt_Pass2)
    EditText edtPass2;
    @BindView(R.id.btn_setPin)
    Button btnSetPin;

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_personal_help_pass;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_back, R.id.btn_setPin})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_setPin:
                mIntent(CreateInputHelpWordWalletSuccseActivity.class);
                break;
        }
    }
}
