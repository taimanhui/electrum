package org.haobtc.wallet.activities.settings;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.chaquo.python.PyObject;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector;
import org.haobtc.wallet.activities.settings.fixpin.ChangePinProcessingActivity;
import org.haobtc.wallet.activities.settings.recovery_set.RecoverySetActivity;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;
import org.haobtc.wallet.utils.NfcUtils;

import java.util.Objects;
import java.util.concurrent.FutureTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.customerUI;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.executorService;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.futureTask;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.isNFC;

public class HardwareDetailsActivity extends BaseActivity {

    public static final String TAG = HardwareDetailsActivity.class.getSimpleName();
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_keyName)
    TextView tetKeyName;
    @BindView(R.id.lin_OnckOne)
    LinearLayout linOnckOne;
    @BindView(R.id.tet_code)
    TextView tetCode;
    @BindView(R.id.lin_OnckTwo)
    LinearLayout linOnckTwo;
    @BindView(R.id.change_pin)
    LinearLayout changePin;
    @BindView(R.id.tet_noPasspay)
    TextView tetNoPasspay;
    @BindView(R.id.lin_OnckFour)
    LinearLayout linOnckFour;
    @BindView(R.id.wipe_device)
    LinearLayout wipe_device;
    @BindView(R.id.tetKeyname)
    TextView tetKeyname;
    private CommunicationModeSelector dialogFragment;
    private boolean executable = true;
    private boolean ready;
    private boolean done;
    private String pin;

    @Override
    public int getLayoutId() {
        return R.layout.activity_somemore;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String keyListItem = intent.getStringExtra("keyListItem");
        tetKeyName.setText(keyListItem);
        tetKeyname.setText(String.format("%s%s", keyListItem, getString(R.string.settings)));
    }

    @Override
    public void initData() {
        NfcUtils.nfc(this, false);
    }

    @OnClick({R.id.img_back, R.id.lin_OnckOne, R.id.lin_OnckTwo, R.id.change_pin, R.id.lin_OnckFour, R.id.wipe_device})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.lin_OnckOne:
                mIntent(BixinKeyMessageActivity.class);
                break;
            case R.id.lin_OnckTwo:
                mIntent(VersionUpgradeActivity.class);
                break;
            case R.id.change_pin:
                dialogFragment = new CommunicationModeSelector(TAG, null, "");
                dialogFragment.show(getSupportFragmentManager(), "");
                break;
            case R.id.lin_OnckFour:
                mIntent(ConfidentialPaymentSettings.class);
                break;
            case R.id.wipe_device:
                mIntent(RecoverySetActivity.class);
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction(); // get the action of the coming intent
        if (Objects.equals(action, NfcAdapter.ACTION_NDEF_DISCOVERED) // NDEF type
                || Objects.equals(action, NfcAdapter.ACTION_TECH_DISCOVERED)
                || Objects.requireNonNull(action).equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            isNFC = true;
            dealWithBusiness(intent);
        }
    }
    private void dealWithBusiness(Intent intent) {
        if (executable) {
            Tag tags = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            PyObject nfc = Global.py.getModule("trezorlib.transport.nfc");
            PyObject nfcHandler = nfc.get("NFCHandle");
            nfcHandler.put("device", tags);
            executable = false;
        }
        if (ready) {
            customerUI.put("pin", pin);
            ready = false;
        } else if (done) {
            startActivity(new Intent(this, ChangePinProcessingActivity.class));
        } else {
            futureTask = new FutureTask<>(() -> Daemon.commands.callAttr("reset_pin"));
            executorService.submit(futureTask);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CommunicationModeSelector.PIN_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) { // 激活、创建
                pin = data.getStringExtra("pin");
                int tag = data.getIntExtra("tag", 0);
                switch (tag) {
                    case CommunicationModeSelector.PIN_NEW_FIRST: // 设置新PIN
                        if (isNFC) {
                            CommunicationModeSelector.pin = pin;
                            done = true;
                        } else {
                            customerUI.put("pin", pin);
                            Intent intent = new Intent(this, ChangePinProcessingActivity.class);
                            startActivity(intent);
                        }
                        break;
                    case CommunicationModeSelector.PIN_CURRENT: // 验证现在的PIN
                        if (isNFC) {
                            ready  = true;
                        } else {
                            customerUI.put("pin", pin);
                        }
                        break;
                    default:
                }

            }
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
    }
    @Override
    protected void onRestart() {
        super.onRestart();
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
    }
}
