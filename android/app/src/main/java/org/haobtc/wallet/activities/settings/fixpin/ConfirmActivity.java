package org.haobtc.wallet.activities.settings.fixpin;

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

public class ConfirmActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.btn_finish)
    Button btnFinish;
    @BindView(R.id.promote_message)
    TextView promoteMessage;

    @Override
    public int getLayoutId() {
        return R.layout.active_successful;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        String tag = getIntent().getStringExtra("tag");
        if (ChangePinProcessingActivity.TAG.equals(tag)) {
            promoteMessage.setText(R.string.pin_change_confirm);
        } else if ("set_pin".equals(tag)) {
            promoteMessage.setText(R.string.set_pin_confirm);
        }
    }

    @Override
    public void initData() {

    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.btn_finish})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
            case R.id.btn_finish:
                finishAffinity();
                break;
        }
    }
}
