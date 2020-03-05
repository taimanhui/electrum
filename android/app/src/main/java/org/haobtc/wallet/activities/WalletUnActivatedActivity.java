package org.haobtc.wallet.activities;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WalletUnActivatedActivity extends BaseActivity {
    public static final String TAG = "org.wallet.activities.WalletUnActivatedActivity";
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.button_activate)
    Button buttonActivate;


    @Override
    public int getLayoutId() {
        return R.layout.activate;
    }

    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {

    }


    @OnClick({R.id.img_back, R.id.button_activate})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                Intent intent1 = new Intent();
                intent1.putExtra("isActive", false);
                setResult(Activity.RESULT_OK, intent1);
                finish();
                break;
            case R.id.button_activate:
                Intent intent = new Intent();
                intent.putExtra("isActive", true);
                setResult(Activity.RESULT_OK, intent);
                finish();
                break;
        }
    }
}
