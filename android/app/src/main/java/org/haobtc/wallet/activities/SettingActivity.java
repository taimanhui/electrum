package org.haobtc.wallet.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.azhon.appupdate.utils.ApkUtil;
import com.chaquo.python.PyObject;
import com.google.gson.Gson;
import com.yzq.zxinglibrary.common.Constant;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector;
import org.haobtc.wallet.activities.settings.BixinKEYManageActivity;
import org.haobtc.wallet.activities.settings.BlueToothStatusActivity;
import org.haobtc.wallet.activities.settings.CurrencyActivity;
import org.haobtc.wallet.activities.settings.recovery_set.BackupRecoveryActivity;
import org.haobtc.wallet.activities.sign.SignActivity;
import org.haobtc.wallet.asynctask.BusinessAsyncTask;
import org.haobtc.wallet.bean.HardwareFeatures;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.event.ResultEvent;
import org.haobtc.wallet.event.SecondEvent;
import org.haobtc.wallet.fragment.ReadingPubKeyDialogFragment;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.COMMUNICATION_MODE_NFC;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.REQUEST_ACTIVE;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.customerUI;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.executorService;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.isNFC;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.xpub;

public class SettingActivity extends BaseActivity implements BusinessAsyncTask.Helper {

    public static final String TAG = SettingActivity.class.getSimpleName();
    @BindView(R.id.tetBuckup)
    TextView tetBuckup;
    @BindView(R.id.tet_language)
    TextView tet_language;
    @BindView(R.id.tetSeverSet)
    TextView tetSeverSet;
    @BindView(R.id.tetTrsactionSet)
    TextView tetTrsactionSet;
    @BindView(R.id.tetVerification)
    TextView tetVerification;
    @BindView(R.id.tetAbout)
    TextView tetAbout;
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_bixinKey)
    TextView tetBixinKey;
    @BindView(R.id.tet_Faru)
    TextView tetFaru;
    @BindView(R.id.tet_verson)
    TextView tetVerson;
    @BindView(R.id.bluetooth_status)
    TextView bluetoothStatusText;
    private boolean bluetoothStatus;
    private SharedPreferences preferences;
    private boolean executable = true;
    public String pin = "";
    private boolean ready;
    private boolean done;
    private boolean isActive;
    private CommunicationModeSelector dialogFragment;

    @Override
    public int getLayoutId() {
        return R.layout.setting;
    }

    @SuppressLint("CommitPrefEdits")
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        preferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        bluetoothStatus = preferences.getBoolean("bluetoothStatus", false);
        if (!bluetoothStatus) {
            bluetoothStatusText.setText(getString(R.string.close));
        } else {
            bluetoothStatusText.setText(getString(R.string.open));
        }
    }

    @Override
    public void initData() {
        String versionName = ApkUtil.getVersionName(this);
        tetVerson.setText(String.format("V%s", versionName));

    }

    @OnClick({R.id.tetBuckup, R.id.tet_language, R.id.tetSeverSet, R.id.tetTrsactionSet, R.id.tetVerification, R.id.tetAbout, R.id.img_back, R.id.tet_bixinKey, R.id.tet_Faru, R.id.bluetooth_set})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tet_bixinKey:
                mIntent(BixinKEYManageActivity.class);
                break;
            case R.id.tetBuckup:
                mIntent(BackupRecoveryActivity.class);
                break;
            case R.id.tet_language:
                mIntent(LanguageSettingActivity.class);
                break;
            case R.id.tetSeverSet:
                mIntent(ServerSettingActivity.class);
                break;
            case R.id.tetTrsactionSet:
                mIntent(TransactionsSettingActivity.class);
                break;
            case R.id.tetVerification:
//                mIntent(VerificationKEYActivity.class);
                showCustomerDialog();
                break;
            case R.id.tetAbout:
                mIntent(AboutActivity.class);
                break;
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_Faru:
                mIntent(CurrencyActivity.class);
                break;
            case R.id.bluetooth_set:
                mIntent(BlueToothStatusActivity.class);
                break;
        }
    }

    private void showCustomerDialog() {
        List<Runnable> runnables = new ArrayList<>();
        runnables.add(runnable);
        dialogFragment = new CommunicationModeSelector(TAG, runnables, "");
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    private Runnable runnable = this::gotoConfirmVerification;

    private void gotoConfirmVerification() {
        Intent intentCon = new Intent(SettingActivity.this, VerificationKEYActivity.class);
        intentCon.putExtra("strVerification", xpub);
        startActivity(intentCon);

    }

    private HardwareFeatures getFeatures() throws Exception {
        String feature;
        try {
            feature = executorService.submit(() -> Daemon.commands.callAttr("get_feature")).get().toString();
            HardwareFeatures features = new Gson().fromJson(feature, HardwareFeatures.class);
            if (features.isBootloaderMode()) {
                throw new Exception("bootloader mode");
            }
            return features;

        } catch (ExecutionException | InterruptedException e) {
            Toast.makeText(this, "communication error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            throw e;
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
            if (executable) {
                Tag tags = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                PyObject nfc = Global.py.getModule("trezorlib.transport.nfc");
                PyObject nfcHandler = nfc.get("NFCHandle");
                nfcHandler.put("device", tags);
                executable = false;
            }
            if (ready) {
                CommunicationModeSelector.customerUI.put("pin", pin);
                ready = false;
                return;
            } else if (done) {
                customerUI.put("pin", pin);
                done = false;
                CommunicationModeSelector.handler.sendEmptyMessage(CommunicationModeSelector.SHOW_PROCESSING);
                return;
            }
            HardwareFeatures features;
            try {
                features = getFeatures();
            } catch (Exception e) {
                if ("bootloader mode".equals(e.getMessage())) {
                    mlToast(getString(R.string.bootloader_mode));
                }
                finish();
                return;
            }
            boolean isInit = features.isInitialized();
            if (isInit) {
                String strRandom = UUID.randomUUID().toString().replaceAll("-", "");
                //Anti counterfeiting verification
                new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.COUNTER_VERIFICATION, strRandom);
            } else {
                if (isActive) {
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.INIT_DEVICE, COMMUNICATION_MODE_NFC);
                } else {
                    Intent intent1 = new Intent(this, WalletUnActivatedActivity.class);
                    startActivityForResult(intent1, REQUEST_ACTIVE);
                }
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CommunicationModeSelector.PIN_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                pin = data.getStringExtra("pin");
                int tag = data.getIntExtra("tag", 0);
                switch (tag) {
                    case CommunicationModeSelector.PIN_NEW_FIRST: // 激活
                        // ble 激活
                        if (CommunicationModeSelector.isActive) {
                            CommunicationModeSelector.customerUI.put("pin", pin);
                            CommunicationModeSelector.handler.sendEmptyMessage(CommunicationModeSelector.SHOW_PROCESSING);

                        } else if (isActive) {
                            // nfc 激活
                            done = true;
                        }
                        break;
                    case CommunicationModeSelector.PIN_CURRENT: // 创建
                        if (!isNFC) { // ble
                            CommunicationModeSelector.customerUI.put("pin", pin);
                        } else { // nfc
                            if (readingPubKey != null) {
                                readingPubKey.dismiss();
                            }
                            ready = true;
                        }
                        break;
                    default:
                }
            }
        } else if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                Log.i("CODED_CONTENT", "content=----: " + content);
            }
        } else if (requestCode == REQUEST_ACTIVE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                isActive = data.getBooleanExtra("isActive", false);
            }
        }
    }

    private ReadingPubKeyDialogFragment readingPubKey;

    @Override
    public void onPreExecute() {
        if (!isActive) {
            readingPubKey = dialogFragment.showReadingDialog();
        }
    }

    @Override
    public void onException(Exception e) {
        readingPubKey.dismiss();
        if ("BaseException: waiting pin timeout".equals(e.getMessage())) {
            ready = false;
        } else if ("BaseException: (7, 'PIN invalid')".equals(e.getMessage())) {
            dialogFragment.showReadingFailedDialog(R.string.pin_wrong);
        } else {
            dialogFragment.showReadingFailedDialog(R.string.read_pk_failed);
        }
    }

    @Override
    public void onResult(String s) {
        if (isActive) {
            EventBus.getDefault().post(new ResultEvent(s));
            isActive = false;
            return;
        }
        if (readingPubKey != null) {
            readingPubKey.dismiss();
        }
        xpub = s;
        gotoConfirmVerification();
    }

    @Override
    public void onCancelled() {
        if (readingPubKey != null) {
            readingPubKey.dismiss();
        }
        mToast(getString(R.string.task_cancle));
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(SecondEvent updataHint) {
        String msgVote = updataHint.getMsg();
        if (msgVote.equals("bluetooth_status")) {
            bluetoothStatus = preferences.getBoolean("bluetoothStatus", false);
            if (!bluetoothStatus) {
                bluetoothStatusText.setText(getString(R.string.close));
            } else {
                bluetoothStatusText.setText(getString(R.string.open));
            }
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
