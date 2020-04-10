package org.haobtc.wallet.activities.personalwallet.hidewallet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.WalletUnActivatedActivity;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector;
import org.haobtc.wallet.asynctask.BusinessAsyncTask;
import org.haobtc.wallet.bean.HardwareFeatures;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.event.ResultEvent;
import org.haobtc.wallet.fragment.ReadingPubKeyDialogFragment;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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

public class HideWalletActivity extends BaseActivity implements BusinessAsyncTask.Helper {

    public static final String TAG = HideWalletActivity.class.getSimpleName();
    @BindView(R.id.img_backCreat)
    ImageView imgBackCreat;
    @BindView(R.id.btnNext)
    Button btnNext;
    @BindView(R.id.testCheckHidewallet)
    TextView testCheckHidewallet;
    @BindView(R.id.testCheckTips)
    TextView testCheckTips;
    private boolean executable = true;
    private boolean ready;
    private boolean isActive;
    // new version code
    public String pin = "";
    private CommunicationModeSelector dialogFragment;
    private Dialog dialogBtoms;
    private EditText edit_bixinName;
    private TextView textView;
    private boolean status;
    private String hideWalletpass;
    private SharedPreferences.Editor edit;

    @Override
    public int getLayoutId() {
        return R.layout.activity_hide_wallet;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        Intent intent = getIntent();
        String hidewallet = intent.getStringExtra("hidewallet");
        if (!TextUtils.isEmpty(hidewallet)) {
            if (hidewallet.equals("check")) {
                testCheckHidewallet.setText(getString(R.string.check_hide_wallet));
                testCheckTips.setText(getString(R.string.check_wallet_tips));
                btnNext.setText(getString(R.string.onclick_check));
            }
        }

    }

    @Override
    public void initData() {

    }


    @OnClick({R.id.img_backCreat, R.id.btnNext})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_backCreat:
                finish();
                break;
            case R.id.btnNext:
                String strBtnTest = btnNext.getText().toString();
                if (strBtnTest.equals(getString(R.string.onclick_check))) {
                    edit.putString("createOrcheck", "check");
                } else {
                    edit.putString("createOrcheck", "create");
                }
                edit.apply();
                List<Runnable> runnables = new ArrayList<>();
                runnables.add(null);
                runnables.add(runnable2);
                dialogFragment = new CommunicationModeSelector(TAG, runnables, "");
                dialogFragment.show(getSupportFragmentManager(), "");
                break;
        }
    }


    private Runnable runnable2 = () -> showConfirmPubDialog(this, R.layout.bixinkey_confirm, xpub);

    private void showConfirmPubDialog(Context context, @LayoutRes int resource, String xpub) {
        //set see view
        View view = View.inflate(context, resource, null);
        dialogBtoms = new Dialog(context, R.style.dialog);
        edit_bixinName = view.findViewById(R.id.edit_keyName);
        TextView tet_Num = view.findViewById(R.id.txt_textNum);
        textView = view.findViewById(R.id.text_public_key_cosigner_popup);
        textView.setText(xpub);
        edit_bixinName.addTextChangedListener(new TextWatcher() {
            CharSequence input;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                input = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tet_Num.setText(String.format(Locale.CHINA, "%d/20", input.length()));
                if (input.length() > 19) {
                    mToast(getString(R.string.moreinput_text));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String strBixinname = edit_bixinName.getText().toString();
            String strSweep = textView.getText().toString();
            if (TextUtils.isEmpty(strBixinname)) {
                mToast(getString(R.string.input_name));
                return;
            }
            String strXpub = "[\"" + strSweep + "\"]";
            Log.i("import_create_hw_wallet", "strBixinname: " + strBixinname);
            Log.i("import_create_hw_wallet", "strXpub: " + strXpub);
            try {
                Daemon.commands.callAttr("import_create_hw_wallet", strBixinname, 1, 1, strXpub, new Kwarg("hide_type", true));
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            dialogBtoms.cancel();
            dialogFragment.dismiss();
            Intent intent = new Intent(HideWalletActivity.this, CheckHideWalletActivity.class);
            intent.putExtra("hideWalletName", strBixinname);
            startActivity(intent);
        });

        //cancel dialog
        view.findViewById(R.id.img_cancle).setOnClickListener(v -> {
            dialogBtoms.cancel();
            dialogFragment.dismiss();
        });

        dialogBtoms.setContentView(view);
        Window window = dialogBtoms.getWindow();
        //set pop_up size
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        //set locate
        window.setGravity(Gravity.BOTTOM);
        //set animal
        window.setWindowAnimations(R.style.AnimBottom);
        dialogBtoms.show();
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
        if (ready) {//
            customerUI.put("pin", pin);
            if (!TextUtils.isEmpty(hideWalletpass)) {
                customerUI.put("passphrase", hideWalletpass);
                hideWalletpass = "";
            }
            ready = false;
            return;
        }
        HardwareFeatures features;
        try {
            features = getFeatures();
        } catch (Exception e) {
            if ("bootloader mode".equals(e.getMessage())) {
                Toast.makeText(this, R.string.bootloader_mode, Toast.LENGTH_LONG).show();
            }
            finish();
            return;
        }
        boolean isInit = features.isInitialized();

        if (isInit) {
            boolean passphrase = features.isPassphraseProtection();
            if (!passphrase) {
                dialogFragment.dismiss();
                mlToast("当前硬件状态不支持隐藏钱包");
                return;
            }
            // todo: get xpub
            if (!status) {//status -->get_xpub_from_hw Only once
                customerUI.callAttr("set_pass_state", 1);
                new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.GET_EXTEND_PUBLIC_KEY_SINGLE, COMMUNICATION_MODE_NFC, "p2wpkh");
            }

        } else {
            // todo: Initialized
            if (isActive) {
               new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.INIT_DEVICE, COMMUNICATION_MODE_NFC);
            } else {
                Intent intent1 = new Intent(this, WalletUnActivatedActivity.class);
                startActivityForResult(intent1, REQUEST_ACTIVE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CommunicationModeSelector.PIN_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) { // activation、create
                pin = data.getStringExtra("pin");
                int tag = data.getIntExtra("tag", 0);
                switch (tag) {
                    case CommunicationModeSelector.PIN_NEW_FIRST: // activation
                        // ble activation
                        if (CommunicationModeSelector.isActive) {
                            customerUI.put("pin", pin);
                            CommunicationModeSelector.handler.sendEmptyMessage(CommunicationModeSelector.SHOW_PROCESSING);

                        } else if (isActive) {
                            // nfc activation
                            CommunicationModeSelector.pin = pin;
                            CommunicationModeSelector.handler.sendEmptyMessage(CommunicationModeSelector.SHOW_PROCESSING);
                        }
                        break;
                    case CommunicationModeSelector.PIN_CURRENT: // create
                        if (!isNFC) { // ble
                            customerUI.put("pin", pin);
                        } else { // nfc
                            if (readingPubKey != null) {
                                readingPubKey.dismiss();
                            }
                            ready = true;
                            status = true;
                        }
                        break;
                    default:
                }
            }
        } else if (requestCode == REQUEST_ACTIVE && resultCode == Activity.RESULT_OK) { // nfc and ble activation
            if (data != null) {
                isActive = data.getBooleanExtra("isActive", false);
            }
        } else if (requestCode == CommunicationModeSelector.PASSPHRASS_INPUT && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                hideWalletpass = data.getStringExtra("passphrase");
                //Enter password to create hidden Wallet
                if (!isNFC) {
                    customerUI.put("passphrase", hideWalletpass);
                } else {
                    ready = true;
                    status = true;
                }

            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(FirstEvent updataHint) {
        String msgVote = updataHint.getMsg();
        if (msgVote.equals("33")) {
            dialogFragment.dismiss();
        }
    }
    private ReadingPubKeyDialogFragment readingPubKey;
    @Override
    public void onPreExecute() {
        if(!isActive) {
            readingPubKey = dialogFragment.showReadingDialog();
        }
    }

    @Override
    public void onException(Exception e) {
        readingPubKey.dismiss();
        if ("BaseException: waiting passphrase timeout".equals(e.getMessage()) || "BaseException: waiting pin timeout".equals(e.getMessage())) {
            ready = false;
            status = false;
        } else if ("com.chaquo.python.PyException: BaseException: (7, 'PIN invalid')".equals(e.getMessage())) {
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
        }        xpub = s;
        showConfirmPubDialog(this, R.layout.bixinkey_confirm, xpub);
    }

    @Override
    public void onCancelled() {
        Toast.makeText(this, "当前任务以取消", Toast.LENGTH_SHORT).show();
    }
}
