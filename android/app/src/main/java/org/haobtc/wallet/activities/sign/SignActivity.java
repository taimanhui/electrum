package org.haobtc.wallet.activities.sign;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.radio_group)
    RadioGroup radioGroup;
    @BindView(R.id.editTrsactionTest)
    EditText editTrsactionTest;
    @BindView(R.id.btn_import_file)
    Button btnImportFile;
    @BindView(R.id.btn_sweep)
    Button btnSweep;
    @BindView(R.id.pasteSignTrsaction)
    Button pasteSignTrsaction;
    @BindView(R.id.editSignMsg)
    EditText editSignMsg;
    @BindView(R.id.pasteSignMsg)
    Button pasteSignMsg;
    @BindView(R.id.btn_toUpgrade)
    Button btnToUpgrade;

    @Override
    public int getLayoutId() {
        return R.layout.activity_sign;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_back, R.id.btn_import_file, R.id.btn_sweep, R.id.pasteSignTrsaction, R.id.pasteSignMsg, R.id.btn_toUpgrade})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_import_file:
                break;
            case R.id.btn_sweep:
                break;
            case R.id.pasteSignTrsaction:
                break;
            case R.id.pasteSignMsg:
                break;
            case R.id.btn_toUpgrade:
                break;
        }
    }
}
