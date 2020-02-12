package org.haobtc.wallet.activities.onlywallet;

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

public class CreatAppWalletActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_setWalletName)
    EditText tetSetWalletName;
    @BindView(R.id.btn_setPin)
    Button btnSetPin;

    @Override
    public int getLayoutId() {
        return R.layout.activity_creat_app_wallet;
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
                String strWalletName = tetSetWalletName.getText().toString();
                if (TextUtils.isEmpty(strWalletName)){
                    mToast(getResources().getString(R.string.set_wallet));
                    return;
                }
                Intent intent = new Intent(CreatAppWalletActivity.this, AppWalletSetPassActivity.class);
                intent.putExtra("strName",strWalletName);
                startActivity(intent);
                break;
        }
    }
}
