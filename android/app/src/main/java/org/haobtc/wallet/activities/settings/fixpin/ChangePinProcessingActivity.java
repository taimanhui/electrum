package org.haobtc.wallet.activities.settings.fixpin;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.event.ResultEvent;
import org.haobtc.wallet.utils.NfcUtils;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChangePinProcessingActivity extends AppCompatActivity {

    private static final String TAG = ChangePinProcessingActivity.class.getSimpleName();
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.verify_pin_state)
    TextView verify;
    @BindView(R.id.pin_setting_state)
    TextView pinSettingState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_pin_processing);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        NfcUtils.nfc(this, false);
        EventBus.getDefault().register(this);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changePinResult(ResultEvent resultEvent) {
        switch (resultEvent.getResult()) {
            case "1":
                Drawable drawableStart = getDrawable(R.drawable.chenggong);
                Objects.requireNonNull(drawableStart).setBounds(0, 0, drawableStart.getMinimumWidth(), drawableStart.getMinimumHeight());
                pinSettingState.setCompoundDrawables(drawableStart, null, null, null);
                startActivity(new Intent(this, ConfirmPincodeActivity.class));
                finish();
                break;
            case "0":
                Toast.makeText(this, "PIN码重置失败", Toast.LENGTH_LONG).show();
                finish();
        }

    }
    @OnClick(R.id.img_back)
    public void onViewClicked(View view) {
        if (view.getId() == R.id.img_back) {
            finish();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        NfcUtils.mNfcAdapter = null;
        EventBus.getDefault().unregister(this);
    }
}
