package org.haobtc.wallet.activities.settings;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.aop.SingleClick;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.heaton.blelibrary.ble.Ble;

public class VerificationSuccessActivity extends BaseActivity {
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.imgStatus)
    ImageView imgStatus;
    @BindView(R.id.testStatus)
    TextView testStatus;
    @BindView(R.id.testContent)
    TextView testContent;
    @BindView(R.id.btn_finish)
    Button btnFinish;

    @Override
    public int getLayoutId() {
        return R.layout.activity_verification_success;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {
        int status = 0;
        switch (status) {
            case 0:
                imgStatus.setImageDrawable(getDrawable(R.drawable.tongguo));
                testStatus.setText(getString(R.string.verifying_pass));
                testContent.setText(getString(R.string.verifying_firmware));
                break;
            case 1:
                imgStatus.setImageDrawable(getDrawable(R.drawable.fail));
                testStatus.setText(getString(R.string.verifying_fail));
                testContent.setText(getString(R.string.contact_service));
                break;
            case 2:
                imgStatus.setImageDrawable(getDrawable(R.drawable.overtime));
                testStatus.setText(getString(R.string.verifying_overtime));
                testContent.setText(getString(R.string.verifying_retry));
                break;
        }
    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.btn_finish})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
            case R.id.btn_finish:
                Ble.getInstance().disconnectAll();
                finishAffinity();
                break;
        }
    }
}
