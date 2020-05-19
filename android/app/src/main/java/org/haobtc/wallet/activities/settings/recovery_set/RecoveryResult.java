package org.haobtc.wallet.activities.settings.recovery_set;

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

public class RecoveryResult extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.imgtongguo)
    ImageView imageView;
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
        if ("0".equals(tag)) {
            promoteMessage.setText(R.string.recovery_failed);
            imageView.setImageDrawable(getDrawable(R.drawable.shibai));
        } else {
            promoteMessage.setText(R.string.recovery_succse);
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
                finish();
                break;
        }
    }
}
