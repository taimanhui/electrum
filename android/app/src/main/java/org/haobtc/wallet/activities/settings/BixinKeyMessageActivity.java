package org.haobtc.wallet.activities.settings;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.event.FixBixinkeyNameEvent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BixinKeyMessageActivity extends BaseActivity {

    @BindView(R.id.tet_keyName)
    TextView tetKeyName;
    @BindView(R.id.tet_code)
    TextView tetCode;
    @BindView(R.id.tet_Bluetoose)
    TextView tetBluetoose;
    private String bleName;

    @Override
    public int getLayoutId() {
        return R.layout.activity_bixin_key_message;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        Intent intent = getIntent();
        String label = intent.getStringExtra("label");
        bleName = intent.getStringExtra("bleName");
        String device_id = intent.getStringExtra("device_id");
        if (!TextUtils.isEmpty(label)) {
            tetKeyName.setText(label);
        } else {
            tetKeyName.setText(String.format("%s", "BixinKEY"));
        }

        tetCode.setText(device_id);
        tetBluetoose.setText(bleName);
    }

    @Override
    public void initData() {

    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.linear_fix_key})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.linear_fix_key:
                Intent intent = new Intent(BixinKeyMessageActivity.this, FixBixinkeyNameActivity.class);
                intent.putExtra("oldBleName",bleName);
                startActivity(intent);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showReading(FixBixinkeyNameEvent event) {
        tetKeyName.setText(event.getKeyname());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
