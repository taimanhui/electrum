package org.haobtc.wallet.activities;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MessageManagerActivity extends BaseActivity {


    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.edit_text1)
    EditText editText;
    @BindView(R.id.bn_sweep1)
    ImageView bnSweep;
    @BindView(R.id.bn_paste1)
    TextView bnPaste;
    @BindView(R.id.btn_Recovery1)
    Button btnRecovery1;

    public int getLayoutId() {
        return R.layout.layout;
    }

    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {

    }


    @OnClick({R.id.img_back, R.id.bn_sweep1, R.id.bn_paste1, R.id.btn_Recovery1})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.bn_sweep1:
                break;
            case R.id.bn_paste1:
                break;
            case R.id.btn_Recovery1:
                String strBackuptext = editText.getText().toString();
                if (TextUtils.isEmpty(strBackuptext)){
                    mToast(getString(R.string.please_clode_text));
                    return;
                }
                break;
        }
    }

}
