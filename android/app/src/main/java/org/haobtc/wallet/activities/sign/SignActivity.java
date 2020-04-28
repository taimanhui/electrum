package org.haobtc.wallet.activities.sign;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.chaquo.python.PyObject;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.ConfirmOnHardware;
import org.haobtc.wallet.activities.TransactionDetailsActivity;
import org.haobtc.wallet.activities.WalletUnActivatedActivity;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector;
import org.haobtc.wallet.asynctask.BusinessAsyncTask;
import org.haobtc.wallet.bean.GetCodeAddressBean;
import org.haobtc.wallet.bean.HardwareFeatures;
import org.haobtc.wallet.entries.FsActivity;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.event.SignMessageEvent;
import org.haobtc.wallet.event.SignResultEvent;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dr.android.fileselector.FileSelectConstant;

import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.COMMUNICATION_MODE_NFC;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.REQUEST_ACTIVE;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.customerUI;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.executorService;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.isNFC;

public class SignActivity extends BaseActivity implements TextWatcher, RadioGroup.OnCheckedChangeListener, BusinessAsyncTask.Helper {

    public static final String TAG = SignActivity.class.getSimpleName();
    public static final String TAG1 = "SIGN_MESSAGE";
    public static final String TAG2 = "HARDWARE_SIGN_TRANSACTION";
    public static final String TAG3 = "HARDWARE_SIGN_MESSAGE";
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.radio_group)
    RadioGroup radioGroup;
    @BindView(R.id.editTrsactionTest)
    EditText editTrsactionTest;
    @BindView(R.id.btn_import_file)
    Button btnImportFile;
    @BindView(R.id.btn_sweep)
    Button btnSweep;
    @BindView(R.id.pasteSignTrsaction)
    Button pasteSignTrsaction;
    @BindView(R.id.btnConfirm)
    Button buttonConfirm;
    @BindView(R.id.textCheckSign)
    TextView textCheckSign;
    @BindView(R.id.test_sign_tips)
    TextView testSignTips;
    private RxPermissions rxPermissions;
    private static final int REQUEST_CODE = 0;
    private boolean signWhich = true;
    private String personceType;
    private String strTest;
    private boolean executable = true;
    private String pin = "";
    private boolean isActive;
    private boolean ready;
    private String strRaw;
    private String strSoftMsg;
    public static String strinputAddress;
    private String hide_phrass;
    private SharedPreferences.Editor edit;
    private String hideWalletpass;
    private boolean done;
    private CommunicationModeSelector chooseCommunicationWayDialogFragment;

    @Override
    public int getLayoutId() {
        return R.layout.activity_sign;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        Intent intent = getIntent();
        personceType = intent.getStringExtra("personceType");
        hide_phrass = intent.getStringExtra("hide_phrass");
        rxPermissions = new RxPermissions(this);
        editTrsactionTest.addTextChangedListener(this);
        radioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void initData() {
        //get sign address
        mGeneratecode();
    }

    //get sign address
    private void mGeneratecode() {
        PyObject walletAddressShowUi = null;
        try {
            walletAddressShowUi = Daemon.commands.callAttr("get_wallet_address_show_UI");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (walletAddressShowUi != null) {
            String strCode = walletAddressShowUi.toString();
            Log.i("strCode", "mGenerate--: " + strCode);
            Gson gson = new Gson();
            GetCodeAddressBean getCodeAddressBean = gson.fromJson(strCode, GetCodeAddressBean.class);
            strinputAddress = getCodeAddressBean.getAddr();

        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        strRaw = editTrsactionTest.getText().toString();
        if (!TextUtils.isEmpty(strRaw)) {
            buttonConfirm.setEnabled(true);
            buttonConfirm.setBackground(getResources().getDrawable(R.drawable.button_bk));
        } else {
            buttonConfirm.setEnabled(false);
            buttonConfirm.setBackground(getResources().getDrawable(R.drawable.button_bk_grey));
        }

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radioSignTrsaction:
                signWhich = true;
                testSignTips.setText(getString(R.string.prompt_sig));
                editTrsactionTest.setHint(getString(R.string.input_tsaction_text));
                break;
            case R.id.radioSignMsg:
                signWhich = false;
                testSignTips.setText(getString(R.string.input_message));
                editTrsactionTest.setHint(getString(R.string.input_sign_msg));
                break;
        }
    }

    @OnClick({R.id.img_back, R.id.btn_import_file, R.id.btn_sweep, R.id.pasteSignTrsaction, R.id.btnConfirm, R.id.textCheckSign})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_import_file:
                rxPermissions
                        .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                Intent intent1 = new Intent();
                                intent1.setClass(getApplicationContext(), FsActivity.class);
                                intent1.putExtra(FileSelectConstant.SELECTOR_REQUEST_CODE_KEY, FileSelectConstant.SELECTOR_MODE_FILE);
                                intent1.addCategory(Intent.CATEGORY_OPENABLE);
                                intent1.putExtra("keyFile", "1");
                                startActivityForResult(intent1, 1);

                            } else { // Oups permission denied
                                Toast.makeText(this, R.string.reservatpion_photo, Toast.LENGTH_SHORT).show();
                            }
                        }).dispose();
                break;
            case R.id.btn_sweep:
                rxPermissions
                        .request(Manifest.permission.CAMERA)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                //If you have already authorized it, you can directly jump to the QR code scanning interface
                                Intent intent2 = new Intent(this, CaptureActivity.class);
                                startActivityForResult(intent2, REQUEST_CODE);
                            } else { // Oups permission denied
                                Toast.makeText(this, R.string.photopersion, Toast.LENGTH_SHORT).show();
                            }
                        }).dispose();
                break;
            case R.id.pasteSignTrsaction:
                pasteMessage(editTrsactionTest);
                break;
            case R.id.btnConfirm:
                edit.putString("createOrcheck", "check");
                edit.apply();
                if ("standard".equals(personceType)) {//Software Wallet sign
                    strSoftMsg = editTrsactionTest.getText().toString();
                    if (TextUtils.isEmpty(strSoftMsg)) {
                        mToast(getString(R.string.raw));
                        return;
                    }
                    if (signWhich) { //sign trsaction
                        softwareSignTrsaction(strSoftMsg);
                    } else { //sign message
                        signDialog();
                    }
                } else { //Hardware wallet signature
                    strTest = editTrsactionTest.getText().toString();
                    if (signWhich) { //sign trsaction
                        if (!TextUtils.isEmpty(hide_phrass)) {
                            //hide wallet sign transaction -->set_pass_state
                            showCustomerDialog(strTest, TAG2);
                        } else {
                            showCustomerDialog(strTest, TAG);
                        }
                    } else {//sign message
                        if (!TextUtils.isEmpty(hide_phrass)) {
                            //hide wallet sign message -->set_pass_state
                            showCustomerDialog(strTest, TAG3);
                        } else {
                            showCustomerDialog(strTest, TAG1);
                        }
                    }
                }
                break;
            case R.id.textCheckSign:
                mIntent(CheckSignActivity.class);
                break;
        }
    }

    private void signDialog() {
        View view1 = LayoutInflater.from(SignActivity.this).inflate(R.layout.input_wallet_pass, null, false);
        AlertDialog alertDialog = new AlertDialog.Builder(SignActivity.this).setView(view1).create();
        EditText str_pass = view1.findViewById(R.id.edit_password);
        view1.findViewById(R.id.btn_enter_wallet).setOnClickListener(v -> {
            String strPassword = str_pass.getText().toString();
            if (TextUtils.isEmpty(strPassword)) {
                mToast(getString(R.string.please_input_pass));
                return;
            }
            PyObject sign_message = null;
            try {
                sign_message = Daemon.commands.callAttr("sign_message", strinputAddress, strSoftMsg, strPassword);
            } catch (Exception e) {
                if (e.getMessage().contains("Incorrect password")) {
                    mToast(getString(R.string.wrong_pass));
                }
                alertDialog.dismiss();
                e.printStackTrace();
                return;
            }
            if (sign_message != null) {
                String signedMessage = sign_message.toString();
                Intent intent = new Intent(SignActivity.this, CheckSignMessageActivity.class);
                intent.putExtra("signMsg", strSoftMsg);
                intent.putExtra("signAddress", strinputAddress);
                intent.putExtra("signedFinish", signedMessage);
                startActivity(intent);
                alertDialog.dismiss();
            }

        });
        view1.findViewById(R.id.cancel_select_wallet).setOnClickListener(v -> {
            alertDialog.dismiss();
        });
        alertDialog.show();

    }

    private void softwareSignTrsaction(String strTest) {
        View view1 = LayoutInflater.from(SignActivity.this).inflate(R.layout.input_wallet_pass, null, false);
        AlertDialog alertDialog = new AlertDialog.Builder(SignActivity.this).setView(view1).create();
        EditText str_pass = view1.findViewById(R.id.edit_password);
        view1.findViewById(R.id.btn_enter_wallet).setOnClickListener(v -> {
            String strPassword = str_pass.getText().toString();
            if (TextUtils.isEmpty(strPassword)) {
                mToast(getString(R.string.please_input_pass));
                return;
            }
            PyObject sign_message = null;
            try {
                sign_message = Daemon.commands.callAttr("sign_tx", strTest, strPassword);
            } catch (Exception e) {
                if (e.getMessage().contains("Incorrect password")) {
                    mToast(getString(R.string.wrong_pass));
                }
                e.printStackTrace();
                return;
            }
            if (sign_message != null) {
                String signedMessage = sign_message.toString();
                EventBus.getDefault().post(new FirstEvent("22"));
                Intent intent = new Intent(SignActivity.this, TransactionDetailsActivity.class);
                intent.putExtra("signTransction", signedMessage);
                intent.putExtra("keyValue", "Sign");
                intent.putExtra("isIsmine", true);
                startActivity(intent);
                alertDialog.dismiss();
            }

        });
        view1.findViewById(R.id.cancel_select_wallet).setOnClickListener(v -> {
            alertDialog.dismiss();
        });
        alertDialog.show();

    }

    private void showCustomerDialog(String signMsg, String tag) {
        List<Runnable> runnables = new ArrayList<>();
        runnables.add(runnable);
        chooseCommunicationWayDialogFragment = new CommunicationModeSelector(tag, runnables, signMsg);
        chooseCommunicationWayDialogFragment.show(getSupportFragmentManager(), "");
    }

    private Runnable runnable = this::gotoConfirmOnHardware;

    private void gotoConfirmOnHardware() {
        if (signWhich) {
            Intent intentCon = new Intent(SignActivity.this, ConfirmOnHardware.class);
            startActivity(intentCon);
        }
    }

    public void pasteMessage(EditText editString) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData data = clipboard.getPrimaryClip();
            if (data != null && data.getItemCount() > 0) {
                editString.setText(data.getItemAt(0).getText());
            }
        }
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
        if (ready) {
            customerUI.put("pin", pin);
            if (!TextUtils.isEmpty(hideWalletpass)) {
                customerUI.put("passphrase", hideWalletpass);
                hideWalletpass = "";
            }
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
                Toast.makeText(this, R.string.bootloader_mode, Toast.LENGTH_LONG).show();
            }
            finish();
            return;
        }
        boolean isInit = features.isInitialized();
        if (isInit) {
            //hide_phrass -->  hide wallet jump here
            if (!TextUtils.isEmpty(hide_phrass)) {
                customerUI.callAttr("set_pass_state", 1);
            }
            if (signWhich) {
                new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.SIGN_TX, strTest);
            } else {
                new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.SIGN_MESSAGE, strinputAddress, strTest);
            }
        } else {
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
        if (requestCode == 5 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                pin = data.getStringExtra("pin");
                int tag = data.getIntExtra("tag", 0);
                switch (tag) {
                    case CommunicationModeSelector.PIN_NEW_FIRST: // activation
                        // ble activation
                        if (CommunicationModeSelector.isActive) {
                            customerUI.put("pin", pin);
                            CommunicationModeSelector.handler.sendEmptyMessage(CommunicationModeSelector.SHOW_PROCESSING);

                        } else if (isActive) {
                            // nfc 激活
                            done = true;
                        }
                        break;
                    case CommunicationModeSelector.PIN_CURRENT: // sign
                        if (!isNFC) { // ble
                            customerUI.put("pin", pin);
                        } else { // nfc
                            ready = true;
                        }
                        break;
                    default:
                }
            }
        } else if (requestCode == REQUEST_ACTIVE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                isActive = data.getBooleanExtra("isActive", false);
            }
        } else if (requestCode == 0 && resultCode == RESULT_OK) {
            //scan
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                editTrsactionTest.setText(content);
            }
        } else if (requestCode == 1 && resultCode == RESULT_OK) {
            //import file
            assert data != null;
            ArrayList<String> listExtra = data.getStringArrayListExtra(FileSelectConstant.SELECTOR_BUNDLE_PATHS);
            assert listExtra != null;
            String str = listExtra.toString();
            String substring = str.substring(1);
            String strPath = substring.substring(0, substring.length() - 1);
            Log.i("listExtra", "listExtra--: " + listExtra + "   strPath ---  " + strPath);
            try {
                //read file
                PyObject read_tx_from_file = Daemon.commands.callAttr("read_tx_from_file", strPath);
                if (read_tx_from_file != null) {
                    String readFile = read_tx_from_file.toString();
                    Log.i("readFile", "tx-------: " + readFile);
                    editTrsactionTest.setText(readFile);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, getResources().getString(R.string.filestyle_wrong), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == CommunicationModeSelector.PASSPHRASS_INPUT && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                hideWalletpass = data.getStringExtra("passphrase");
                //Enter password to create hidden Wallet
                if (!isNFC) {
                    customerUI.put("passphrase", hideWalletpass);
                    gotoConfirmOnHardware();
                } else {
                    ready = true;
                }
            }
        }
    }

    @Override
    public void onPreExecute() {
        if (signWhich) {
            gotoConfirmOnHardware();
        }
    }

    @Override
    public void onException(Exception e) {
        if ("BaseException: waiting pin timeout".equals(e.getMessage())) {
            ready = false;
        } else if ("com.chaquo.python.PyException: BaseException: (7, 'PIN invalid')".equals(e.getMessage())) {
            mToast(getString(R.string.pin_wrong));
        } else if ("BaseException: Cannot sign messages with this type of address:p2ws".equals(e.getMessage())) {
            mToast(getString(R.string.single_sign_only));
        } else {
            finish();
        }
    }

    @Override
    public void onResult(String s) {
        if (signWhich) {
            EventBus.getDefault().post(new SignResultEvent(s));
        } else {
            onSignMessage(new SignMessageEvent(s));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSignMessage(SignMessageEvent event) {
        strSoftMsg = editTrsactionTest.getText().toString();
        String signedMsg = event.getSignedRaw();
        chooseCommunicationWayDialogFragment.dismiss();
        Intent intentMsg = new Intent(SignActivity.this, CheckSignMessageActivity.class);
        intentMsg.putExtra("signMsg", strSoftMsg);
        intentMsg.putExtra("signAddress", strinputAddress);
        intentMsg.putExtra("signedFinish", signedMsg);
        startActivity(intentMsg);
    }

    @Override
    public void onCancelled() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
