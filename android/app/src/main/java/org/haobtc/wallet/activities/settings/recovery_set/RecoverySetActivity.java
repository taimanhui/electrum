package org.haobtc.wallet.activities.settings.recovery_set;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.chaquo.python.PyObject;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.ResetDeviceProcessing;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;
import org.haobtc.wallet.utils.NfcUtils;

import java.util.Objects;
import java.util.concurrent.FutureTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.futureTask;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.executorService;


public class RecoverySetActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_backups)
    TextView tetBackups;
    @BindView(R.id.reset_device)
    Button rest_device;
    @BindView(R.id.checkbox_Know)
    CheckBox checkboxKnow;
    public static final String TAG = RecoverySetActivity.class.getSimpleName();
    private CommunicationModeSelector dialogFragment;
    private boolean executable = true;

    @Override
    public int getLayoutId() {
        return R.layout.activity_recovery_set;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {
        checkboxKnow.setOnCheckedChangeListener(this);

    }

    @OnClick({R.id.img_back, R.id.tet_backups, R.id.reset_device})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_backups:
                mIntent(BackupRecoveryActivity.class);
                break;
            case R.id.reset_device:
                showPopupAddCosigner1();
                break;
        }
    }

    private void showPopupAddCosigner1() {
        dialogFragment = new CommunicationModeSelector(TAG, null, "");
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked){
            rest_device.setBackground(getDrawable(R.drawable.button_bk));
            rest_device.setEnabled(true);
        }else{
            rest_device.setBackground(getDrawable(R.drawable.button_bk_grey));
            rest_device.setEnabled(false);
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction(); // get the action of the coming intent
        if (Objects.equals(action, NfcAdapter.ACTION_NDEF_DISCOVERED) // NDEF type
                || Objects.equals(action, NfcAdapter.ACTION_TECH_DISCOVERED)
                || Objects.requireNonNull(action).equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            processingState(intent);
        }
    }

    private void processingState(Intent intent) {
        if (executable) {
            Tag tags = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            PyObject nfc = Global.py.getModule("trezorlib.transport.nfc");
            PyObject nfcHandler = nfc.get("NFCHandle");
            nfcHandler.put("device", tags);
            executable = false;
        }
        futureTask = new FutureTask<>(() -> Daemon.commands.callAttr("wipe_device"));
        executorService.submit(futureTask);
        startActivity(new Intent(this, ResetDeviceProcessing.class));
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
        finish();
    }
}
