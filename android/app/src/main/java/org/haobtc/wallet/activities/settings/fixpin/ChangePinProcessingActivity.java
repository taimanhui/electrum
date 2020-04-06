package org.haobtc.wallet.activities.settings.fixpin;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector;
import org.haobtc.wallet.event.ResultEvent;
import org.haobtc.wallet.utils.NfcUtils;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.heaton.blelibrary.ble.Ble;

import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.futureTask;

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
    @Subscribe
    public void onEventMainThread(ResultEvent resultEvent) {
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
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction(); // get the action of the coming intent
        if (Objects.equals(action, NfcAdapter.ACTION_NDEF_DISCOVERED) // NDEF type
                || Objects.equals(action, NfcAdapter.ACTION_TECH_DISCOVERED)
                || Objects.requireNonNull(action).equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            CommunicationModeSelector.customerUI.put("pin", CommunicationModeSelector.pin);
        }
    }
    @OnClick(R.id.img_back)
    public void onViewClicked(View view) {
        if (view.getId() == R.id.img_back) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // enable nfc discovery for the app
            System.out.println("为本App启用NFC感应");
            NfcUtils.mNfcAdapter.enableForegroundDispatch(this, NfcUtils.mPendingIntent, NfcUtils.mIntentFilter, NfcUtils.mTechList);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // disable nfc discovery for the app
            NfcUtils.mNfcAdapter.disableForegroundDispatch(this);
            System.out.println("禁用本App的NFC感应");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NfcUtils.mNfcAdapter = null;
        EventBus.getDefault().unregister(this);
    }
}
