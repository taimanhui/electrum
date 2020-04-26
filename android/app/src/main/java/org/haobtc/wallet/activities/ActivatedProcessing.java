package org.haobtc.wallet.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.event.ResultEvent;
import org.haobtc.wallet.utils.NfcUtils;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActivatedProcessing extends BaseActivity {
    @BindView(R.id.img_back)
    ImageView imgBack;
    private TextView textViewProcess;

    public int getLayoutId() {
        return R.layout.activing_process;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        textViewProcess = findViewById(R.id.activate_state);
        EventBus.getDefault().register(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    @Override
    public void initData() {
        NfcUtils.nfc(this, false);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NfcUtils.mNfcAdapter = null;
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onEventMainThread(ResultEvent resultEvent) {
        switch (resultEvent.getResult()) {
            case "1":
                Drawable drawableStart = getDrawable(R.drawable.chenggong);
                Objects.requireNonNull(drawableStart).setBounds(0, 0, drawableStart.getMinimumWidth(), drawableStart.getMinimumHeight());
                textViewProcess.setCompoundDrawables(drawableStart, null, null, null);
                startActivity(new Intent(this, ActivateSuccessActivity.class));
                finish();
                break;
            case "0":
                Toast.makeText(this, "设备激活失败", Toast.LENGTH_LONG).show();
                finish();
        }
    }
    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.img_back) {
            finish();
        }
    }
}
