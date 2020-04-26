package org.haobtc.wallet.activities.personalwallet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
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
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.google.gson.Gson;
import com.yzq.zxinglibrary.common.Constant;

import org.greenrobot.eventbus.EventBus;
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
import org.haobtc.wallet.utils.IndicatorSeekBar;
import org.haobtc.wallet.utils.MyDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.COMMUNICATION_MODE_NFC;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.REQUEST_ACTIVE;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.customerUI;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.executorService;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.futureTask;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.isNFC;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.xpub;

public class SingleSigWalletCreator extends BaseActivity implements BusinessAsyncTask.Helper {


    @BindView(R.id.img_backCreat)
    ImageView imgBackCreat;
    @BindView(R.id.edit_walletName_setting)
    EditText editWalletNameSetting;
    @BindView(R.id.seek_bar_num)
    IndicatorSeekBar seekBarNum;
    @BindView(R.id.bn_multi_next)
    Button bnMultiNext;
    @BindView(R.id.tv_indicator)
    TextView tvIndicator;
    private SharedPreferences.Editor edit;
    private int defaultName;
    private int walletNameNum;
    private boolean executable = true;
    public String pin = "";
    private CommunicationModeSelector dialogFragment;
    public static final String TAG = SingleSigWalletCreator.class.getSimpleName();
    private TextView textView;
    private boolean isActive;
    private String walletName;
    private boolean ready;
    private MyDialog myDialog;
    private EditText edit_bixinName;
    private Dialog dialogBtoms;
    private int pub;
    private boolean done;

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_personal_wallet;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        myDialog = MyDialog.showDialog(this);

        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        defaultName = preferences.getInt("defaultName", 0);
        init();

    }

    private void init() {
        walletNameNum = defaultName + 1;
        editWalletNameSetting.setText(String.format("钱包%s", String.valueOf(walletNameNum)));

        editWalletNameSetting.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String indication = tvIndicator.getText().toString();
                if (!TextUtils.isEmpty(s.toString())) {
                    if (Integer.parseInt(indication) != 0) {
                        bnMultiNext.setEnabled(true);
                        bnMultiNext.setBackground(getDrawable(R.drawable.button_bk));
                    } else {
                        bnMultiNext.setEnabled(false);
                        bnMultiNext.setBackground(getDrawable(R.drawable.button_bk_grey));
                    }
                } else {
                    bnMultiNext.setEnabled(false);
                    bnMultiNext.setBackground(getDrawable(R.drawable.button_bk_grey));
                }
            }
        });

    }

    @Override
    public void initData() {
        seekbarLatoutup();
    }

    private void seekbarLatoutup() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tvIndicator.getLayoutParams();
        seekBarNum.setOnSeekBarChangeListener(new IndicatorSeekBar.OnIndicatorSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, float indicatorOffset) {
                walletName = editWalletNameSetting.getText().toString();
                String indicatorText = String.valueOf(progress+1);
                tvIndicator.setText(indicatorText);
                params.leftMargin = (int) indicatorOffset;
                tvIndicator.setLayoutParams(params);
                    if (!TextUtils.isEmpty(walletName)) {
                        bnMultiNext.setEnabled(true);
                        bnMultiNext.setBackground(getDrawable(R.drawable.button_bk));
                    } else {
                        bnMultiNext.setEnabled(false);
                        bnMultiNext.setBackground(getDrawable(R.drawable.button_bk_grey));
                    }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tvIndicator.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tvIndicator.setVisibility(View.VISIBLE);
            }
        });

    }

    @OnClick({R.id.img_backCreat, R.id.bn_multi_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_backCreat:
                finish();
                break;
            case R.id.bn_multi_next:
                mCreatOnlyWallet();
                break;
        }
    }

    //creat personal wallet
    private void mCreatOnlyWallet() {
        String strWalletName = editWalletNameSetting.getText().toString();
        String indication = tvIndicator.getText().toString();
        pub = Integer.parseInt(indication);
        if (TextUtils.isEmpty(strWalletName)) {
            mToast(getString(R.string.set_wallet));
            return;
        }
        if (pub == 0) {
            mToast(getString(R.string.set_bixinkey_num));
            return;
        }

        if (pub > 1) {
            Intent intent = new Intent(SingleSigWalletCreator.this, PersonalMultiSigWalletCreator.class);
            intent.putExtra("sigNum", pub);
            intent.putExtra("walletNameNum", walletNameNum);
            intent.putExtra("walletNames", strWalletName);
            startActivity(intent);
            finish();
        } else {
            // new version code
            showPopupAddCosigner1();
        }

    }

    private Runnable runnable2 = () -> showConfirmPubDialog(this, R.layout.bixinkey_confirm, xpub);

    private void showPopupAddCosigner1() {
        List<Runnable> runnables = new ArrayList<>();
        runnables.add(null);
        runnables.add(runnable2);
        dialogFragment = new CommunicationModeSelector(TAG, runnables, "");
        dialogFragment.show(getSupportFragmentManager(), "");
    }

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
                    Toast.makeText(SingleSigWalletCreator.this, R.string.moreinput_text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            myDialog.show();
            handler.sendEmptyMessage(1);
        });

        //cancel dialog
        view.findViewById(R.id.img_cancle).setOnClickListener(v -> {
            dialogBtoms.cancel();
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
                new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.GET_EXTEND_PUBLIC_KEY_SINGLE, COMMUNICATION_MODE_NFC, "p2wpkh");
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

    @SuppressLint("HandlerLeak")
    Handler handler =new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                String strBixinname = edit_bixinName.getText().toString();
                String strSweep = textView.getText().toString();
                walletName = editWalletNameSetting.getText().toString();
                if (TextUtils.isEmpty(strBixinname)) {
                    mToast(getString(R.string.input_name));
                    return;
                }
                if (TextUtils.isEmpty(strSweep)) {
                    mToast(getString(R.string.input_public_address));
                    return;
                }
                String strXpub = "[\"" + strSweep + "\"]";
                try {
                    Daemon.commands.callAttr("import_create_hw_wallet", walletName, 1, pub, strXpub);
                } catch (Exception e) {
                    e.printStackTrace();
                    myDialog.dismiss();
                    String message = e.getMessage();
                    if ("BaseException: file already exists at path".equals(message)) {
                        mToast(getString(R.string.changewalletname));
                    }else if (message.contains("The same xpubs have create wallet")){
                        mToast(getString(R.string.xpub_have_wallet));
                    }
                    return;
                }
                Log.i("jinxioaminisheduh", "================: ");
                edit.putInt("defaultName", walletNameNum);
                edit.apply();
                myDialog.dismiss();
                EventBus.getDefault().post(new FirstEvent("11"));
                Intent intent = new Intent(SingleSigWalletCreator.this, CreatFinishPersonalActivity.class);
                intent.putExtra("walletNames", walletName);
                intent.putExtra("flagTag", "personal");
                intent.putExtra("strBixinname", strBixinname);
                startActivity(intent);
                finish();
                dialogBtoms.cancel();
                dialogFragment.dismiss();
            }

        }
    };

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
        showConfirmPubDialog(this, R.layout.bixinkey_confirm, xpub);
    }

    @Override
    public void onCancelled() {
        if (readingPubKey != null) {
            readingPubKey.dismiss();
        }
        mToast(getString(R.string.task_cancle));
    }

}
