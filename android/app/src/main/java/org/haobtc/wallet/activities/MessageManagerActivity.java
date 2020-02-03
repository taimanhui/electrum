package org.haobtc.wallet.activities;

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
    @BindView(R.id.edit_text)
    EditText editText;
    @BindView(R.id.bn_sweep)
    ImageView bnSweep;
    @BindView(R.id.bn_paste)
    TextView bnPaste;
    @BindView(R.id.btn_Recovery)
    Button btnRecovery;

    public int getLayoutId() {
        return R.layout.layout;
    }

    public void initView() {
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {

    }


    @OnClick({R.id.img_back, R.id.bn_sweep, R.id.bn_paste, R.id.btn_Recovery})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.bn_sweep:
                break;
            case R.id.bn_paste:
                break;
            case R.id.btn_Recovery:
                break;
        }
    }
}
