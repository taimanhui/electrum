package org.haobtc.wallet.activities.sign;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CheckSignMessageActivity extends BaseActivity {

    @BindView(R.id.testOriginal)
    TextView testOriginal;
    @BindView(R.id.testPublickey)
    TextView testPublickey;
    @BindView(R.id.testSignedMsg)
    TextView testSignedMsg;

    @Override
    public int getLayoutId() {
        return R.layout.activity_check_sign_message;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String signMsg = intent.getStringExtra("signMsg");
        String signAddress = intent.getStringExtra("signAddress");
        String signedFinish = intent.getStringExtra("signedFinish");
        testOriginal.setText(signMsg);
        testPublickey.setText(signAddress);
        testSignedMsg.setText(signedFinish);
    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_back, R.id.testCopyPublickey, R.id.testCopySignedMsg})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.testCopyPublickey:
                break;
            case R.id.testCopySignedMsg:
                break;
        }
    }
}
