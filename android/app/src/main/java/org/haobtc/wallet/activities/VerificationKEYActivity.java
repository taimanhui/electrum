package org.haobtc.wallet.activities;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector;
import org.haobtc.wallet.activities.settings.VerificationSuccessActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VerificationKEYActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;

    @Override
    public int getLayoutId() {
        return CommunicationModeSelector.isNFC ? R.layout.processing_nfc : R.layout.processing_ble;

    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        // 设置沉浸式状态栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        Intent intent = getIntent();
        String strVerification = intent.getStringExtra("strVerification");
        Log.i("strVerification", "initView: "+strVerification);
    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
//                Intent intent = new Intent(VerificationKEYActivity.this, VerificationSuccessActivity.class);
//                startActivity(intent);
                break;

        }
    }

}
