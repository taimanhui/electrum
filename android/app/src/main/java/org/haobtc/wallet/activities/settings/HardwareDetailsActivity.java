package org.haobtc.wallet.activities.settings;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.chaquo.python.PyObject;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.ResetDeviceProcessing;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector;
import org.haobtc.wallet.activities.settings.fixpin.ChangePinProcessingActivity;
import org.haobtc.wallet.activities.settings.recovery_set.RecoverySetActivity;
import org.haobtc.wallet.asynctask.BusinessAsyncTask;
import org.haobtc.wallet.bean.HardwareFeatures;
import org.haobtc.wallet.event.ResultEvent;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;
import org.haobtc.wallet.utils.NfcUtils;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.COMMUNICATION_MODE_NFC;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.customerUI;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.executorService;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.futureTask;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.isNFC;

public class HardwareDetailsActivity extends BaseActivity implements BusinessAsyncTask.Helper {

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
    public static boolean dismiss;

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
    private boolean isInitialized() throws Exception {
        String feature;
        try {
            feature = executorService.submit(() -> Daemon.commands.callAttr("get_feature")).get().toString();
            HardwareFeatures features = new Gson().fromJson(feature, HardwareFeatures.class);
            if (features.isBootloaderMode()) {
                throw new Exception("bootloader mode");
            }
            return features.isInitialized();
        } catch (ExecutionException | InterruptedException e) {
            Toast.makeText(this, "communication error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            throw e;
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
            boolean isInit;
            try {
                isInit = isInitialized();
            } catch (Exception e) {
                e.printStackTrace();
                if ("bootloader mode".equals(e.getMessage())) {
                    Toast.makeText(this, R.string.bootloader_mode, Toast.LENGTH_LONG).show();
                }
                finish();
                return;
            }
            if (isInit) {
                new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.CHANGE_PIN, COMMUNICATION_MODE_NFC);
            } else {
                Toast.makeText(this, R.string.wallet_un_activated_pin, Toast.LENGTH_LONG).show();
                finish();
            }
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
        if (dialogFragment != null && dismiss) {
            dialogFragment.dismiss();
        }
    }

    @Override
    public void onPreExecute() {

    }

    @Override
    public void onException(Exception e) {

    }

    @Override
    public void onResult(String s) {
        EventBus.getDefault().post(new ResultEvent(s));
    }

    @Override
    public void onCancelled() {

    }
}
