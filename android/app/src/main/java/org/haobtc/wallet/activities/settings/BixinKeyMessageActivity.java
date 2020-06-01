package org.haobtc.wallet.activities.settings;

import android.content.Intent;
import android.content.SharedPreferences;
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
import org.haobtc.wallet.event.SetShutdownTimeEvent;

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
    @BindView(R.id.test_shutdown_time)
    TextView testShutdownTime;
    private String bleName;
    private Intent intent;

    @Override
    public int getLayoutId() {
        return R.layout.activity_bixin_key_message;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        intent = getIntent();
        inits();
    }

    private void inits() {
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        String shutdownTime = preferences.getString("shutdownTime", "");
        String label = intent.getStringExtra("label");
        bleName = intent.getStringExtra("bleName");
        String device_id = intent.getStringExtra("device_id");
        if (!TextUtils.isEmpty(label)) {
            tetKeyName.setText(label);
        } else {
            tetKeyName.setText(String.format("%s", "BixinKEY"));
        }
        testShutdownTime.setText(String.format("%s%s", shutdownTime, getString(R.string.second)));
        tetCode.setText(device_id);
        tetBluetoose.setText(bleName);
    }

    @Override
    public void initData() {

    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.linear_fix_key, R.id.linear_shutdown_time})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.linear_fix_key:
                mIntent(FixBixinkeyNameActivity.class);
                break;
            case R.id.linear_shutdown_time:
                mIntent(SetShutdownTimeActivity.class);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showReading(FixBixinkeyNameEvent event) {
        tetKeyName.setText(event.getKeyname());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showtime(SetShutdownTimeEvent event) {
        testShutdownTime.setText(String.format("%s%s", event.getTime(), getString(R.string.second)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
