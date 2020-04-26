package org.haobtc.wallet.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chaquo.python.PyObject;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector;
import org.haobtc.wallet.activities.transaction.DeatilMoreAddressActivity;
import org.haobtc.wallet.adapter.ChoosePayAddressAdapetr;
import org.haobtc.wallet.asynctask.BusinessAsyncTask;
import org.haobtc.wallet.bean.AddressEvent;
import org.haobtc.wallet.bean.GetAddressBean;
import org.haobtc.wallet.bean.GetCodeAddressBean;
import org.haobtc.wallet.bean.GetnewcreatTrsactionListBean;
import org.haobtc.wallet.bean.GetsendFeenumBean;
import org.haobtc.wallet.bean.HardwareFeatures;
import org.haobtc.wallet.event.ResultEvent;
import org.haobtc.wallet.event.SecondEvent;
import org.haobtc.wallet.event.SignFailedEvent;
import org.haobtc.wallet.event.SignResultEvent;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;
import org.haobtc.wallet.utils.IndicatorSeekBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.COMMUNICATION_MODE_NFC;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.REQUEST_ACTIVE;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.customerUI;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.executorService;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.isNFC;

public class SendOne2ManyMainPageActivity extends BaseActivity implements BusinessAsyncTask.Helper {

    public static final String TAG = SendOne2ManyMainPageActivity.class.getSimpleName();
    @BindView(R.id.wallet_name)
    TextView walletName;
    @BindView(R.id.lin_chooseAddress)
    LinearLayout linChooseAddress;
    @BindView(R.id.address_count)
    TextView addressCount;
    @BindView(R.id.linearLayout10)
    LinearLayout linearLayout10;
    @BindView(R.id.tv_amount)
    TextView tvAmount;
    @BindView(R.id.img_feeSelect)
    ImageView feeSelect;
    @BindView(R.id.tv_fee)
    TextView tvFee;
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.create_trans_one2many)
    Button createTransOne2many;
    @BindView(R.id.seed_Bar)
    IndicatorSeekBar seedBar;
    @BindView(R.id.tv_indicator)
    TextView tvIndicator;
    @BindView(R.id.test_wallet_unit)
    TextView testWalletUnit;
    @BindView(R.id.text_blocks)
    TextView textBlocks;
    @BindView(R.id.btnRecommendFee)
    Button btnRecommendFee;
    private ArrayList<AddressEvent> dataListName;
    private Dialog dialogBtom;
    private ChoosePayAddressAdapetr choosePayAddressAdapetr;
    private String wallet_name;
    private double pro;
    private String strmapBtc;
    private List addressList;
    private int intmaxFee;
    private String waletType;
    private String wallet_type_to_sign;
    private ArrayList<GetnewcreatTrsactionListBean.OutputAddrBean> outputAddr;
    private CommunicationModeSelector modeSelector;
    private String payAddress;
    private String totalAmount;
    private boolean executable = true;
    private String pin = "";
    private boolean isActive;
    private boolean ready;
    private boolean done;
    private String rowtx;
    private String strFeemontAs;

    public int getLayoutId() {
        return R.layout.send_one2many_main;
    }

    @SuppressLint("CommitPrefEdits")
    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        //1-n wallet  --> Direct signature and broadcast
        wallet_type_to_sign = preferences.getString("wallet_type_to_sign", "");
        String base_unit = preferences.getString("base_unit", "mBTC");
        Intent intent = getIntent();
        addressList = (List) getIntent().getSerializableExtra("listdetail");
        wallet_name = intent.getStringExtra("wallet_name");
        waletType = intent.getStringExtra("wallet_type");
        int addressNum = intent.getIntExtra("addressNum", 0);
        totalAmount = intent.getStringExtra("totalAmount");

        //To many people Coin making
        strmapBtc = intent.getStringExtra("strmapBtc");
        Log.i("nihaoajinxiaomin", "initView:++ " + strmapBtc);
        walletName.setText(wallet_name);
        addressCount.setText(String.format("%s %s", String.valueOf(addressNum), getString(R.string.to_num)));
        tvAmount.setText(String.format("%s %s", totalAmount, base_unit));
        testWalletUnit.setText(base_unit);

    }

    @Override
    public void initData() {
        dataListName = new ArrayList<>();
        //getMorepayAddress
        payAddressMore();
        //fee
        getFeeamont();
        //get pay address
        mGeneratecode();

    }

    //get pay address
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
            Gson gson = new Gson();
            GetCodeAddressBean getCodeAddressBean = gson.fromJson(strCode, GetCodeAddressBean.class);
            payAddress = getCodeAddressBean.getAddr();
        }
    }

    private void getFeeamont() {
        PyObject get_default_fee_status = null;
        try {
            get_default_fee_status = Daemon.commands.callAttr("get_default_fee_status");
        } catch (Exception e) {
            e.printStackTrace();
            mToast(e.getMessage());
            return;
        }
        if (get_default_fee_status != null) {
            String strFee = get_default_fee_status.toString();
            Log.i("get_default_fee", "strFee:   " + strFee);
            if (strFee.contains("sat/byte")) {
                strFeemontAs = strFee.substring(0, strFee.indexOf("sat/byte") + 8);
                String strFeeamont = strFee.substring(0, strFee.indexOf("sat/byte"));
                String strMax = strFeeamont.replaceAll(" ", "");
                textBlocks.setText(strFeemontAs);
                intmaxFee = Integer.parseInt(strMax);
                seedBar.setMax(intmaxFee * 2);
                seedBar.setProgress(intmaxFee);

            }
            seekbarLatoutup();
            getFeerate();
        }
    }

    private void seekbarLatoutup() {
        seedBar.setOnSeekBarChangeListener(new IndicatorSeekBar.OnIndicatorSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, float indicatorOffset) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String indicatorText = String.valueOf(seekBar.getProgress());
                intmaxFee = Integer.parseInt(indicatorText);//use get fee
                textBlocks.setText(String.format("%s sat/byte", indicatorText));
                //getFeerate
                getFeerate();
            }
        });

    }


    //getMorepayAddress
    private void payAddressMore() {
        PyObject get_wallets_list_info = null;
        try {
            get_wallets_list_info = Daemon.commands.callAttr("list_wallets");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (get_wallets_list_info != null) {
            String toString = get_wallets_list_info.toString();
            JSONArray jsons = JSONObject.parseArray(toString);
            for (int i = 0; i < jsons.size(); i++) {
                Map jsonToMap = (Map) jsons.get(i);
                Set<String> keySets = jsonToMap.keySet();
                Iterator<String> ki = keySets.iterator();
                AddressEvent addressEvent = new AddressEvent();
                while (ki.hasNext()) {
                    //get key
                    String key = ki.next();
                    String value = jsonToMap.get(key).toString();
                    addressEvent.setName(key);
                    addressEvent.setType(value);
                    dataListName.add(addressEvent);
                }
            }
        }

    }


    @OnClick({R.id.lin_chooseAddress, R.id.linearLayout10, R.id.img_feeSelect, R.id.img_back, R.id.create_trans_one2many, R.id.btnRecommendFee})
    public void onViewClicked(View view) {
        PyObject mktx;
        switch (view.getId()) {
            case R.id.lin_chooseAddress:
                //check wallet
                showDialogs(SendOne2ManyMainPageActivity.this, R.layout.select_send_wallet_popwindow);
                break;
            case R.id.linearLayout10:
                Intent intent1 = new Intent(SendOne2ManyMainPageActivity.this, DeatilMoreAddressActivity.class);
                intent1.putExtra("listdetail", (Serializable) addressList);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
                break;
            case R.id.img_feeSelect:
                //Miner money
//                showSelectFeeDialogs(SendOne2ManyMainPageActivity.this, R.layout.select_fee_popwindow);
                break;
            case R.id.img_back:
                finish();
                break;
            case R.id.create_trans_one2many:
                try {
                    mktx = Daemon.commands.callAttr("mktx", "", "");
                    Log.i("CreatTransaction", "m-------: " + mktx);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getMessage().contains("Insufficient funds")) {
                        mToast(getString(R.string.wallet_insufficient));
                    } else {
                        mToast(getString(R.string.changeaddress));
                    }
                    return;
                }
                if (mktx != null) {
                    String jsonObj = mktx.toString();
                    Gson gson = new Gson();
                    GetAddressBean getAddressBean = gson.fromJson(jsonObj, GetAddressBean.class);
                    rowtx = getAddressBean.getTx();
                    if (!TextUtils.isEmpty(rowtx)) {
                        if (wallet_type_to_sign.contains("1-")) {
                            try {
                                PyObject get_tx_info_from_raw = Daemon.commands.callAttr("get_tx_info_from_raw", rowtx);
                                gson = new Gson();
                                GetnewcreatTrsactionListBean getnewcreatTrsactionListBean = gson.fromJson(get_tx_info_from_raw.toString(), GetnewcreatTrsactionListBean.class);
                                outputAddr = getnewcreatTrsactionListBean.getOutputAddr();
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }
                            //1-n wallet  --> Direct signature and broadcast
                            showCustomerDialog(rowtx);

                        } else {
                            EventBus.getDefault().post(new SecondEvent("finish"));
                            Intent intent = new Intent(SendOne2ManyMainPageActivity.this, TransactionDetailsActivity.class);
                            intent.putExtra("tx_hash", rowtx);
                            intent.putExtra("keyValue", "A");
                            intent.putExtra("isIsmine", true);
                            intent.putExtra("strwalletType", waletType);
                            intent.putExtra("txCreatTrsaction", rowtx);
                            startActivity(intent);
                        }
                    }
                }
                break;
            case R.id.btnRecommendFee:
                mToast(strFeemontAs);
                break;
        }
    }

    private void showCustomerDialog(String rowtx) {
        List<Runnable> runnables = new ArrayList<>();
        runnables.add(runnable);
        modeSelector = new CommunicationModeSelector(TAG, runnables, rowtx);
        modeSelector.show(getSupportFragmentManager(), "");
    }

    private Runnable runnable = this::gotoConfirmOnHardware;

    private void gotoConfirmOnHardware() {
        Intent intentCon = new Intent(SendOne2ManyMainPageActivity.this, ConfirmOnHardware.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("output", outputAddr);
        bundle.putString("pay_address", payAddress);
        bundle.putString("fee", totalAmount);
        intentCon.putExtra("outputs", bundle);
        startActivityForResult(intentCon, 1);
    }

    private HardwareFeatures getFeatures() throws Exception {
        String feature;
        try {
            feature = executorService.submit(() -> Daemon.commands.callAttr("get_feature", "nfc")).get().toString();
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
            gotoConfirmOnHardware();
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
            if (features.isPinCached()) {
                gotoConfirmOnHardware();
            }
            new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.SIGN_TX, rowtx);
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
                    case CommunicationModeSelector.PIN_NEW_FIRST: // 激活
                        // ble 激活
                        if (CommunicationModeSelector.isActive) {
                            customerUI.put("pin", pin);
                            CommunicationModeSelector.handler.sendEmptyMessage(CommunicationModeSelector.SHOW_PROCESSING);
                        } else if (isActive) {
                            // nfc 激活
                            done = true;
                        }
                        break;
                    case CommunicationModeSelector.PIN_CURRENT: // 签名
                        if (!isNFC) { // ble
                            customerUI.put("pin", pin);
                            gotoConfirmOnHardware();
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
        }
    }

    @Override
    public void onPreExecute() {
    }

    @Override
    public void onException(Exception e) {
        if ("BaseException: waiting pin timeout".equals(e.getMessage())) {
            ready = false;
        } else {
            EventBus.getDefault().post(new SignFailedEvent(e));
        }
    }

    @Override
    public void onResult(String s) {
        if (isActive) {
            EventBus.getDefault().post(new ResultEvent(s));
            isActive = false;
            return;
        }
        EventBus.getDefault().post(new SignResultEvent(s));
    }

    @Override
    public void onCancelled() {

    }

    private void showDialogs(Context context, @LayoutRes int resource) {
        //set see view
        View view = View.inflate(context, resource, null);
        dialogBtom = new Dialog(context, R.style.dialog);
        //cancel dialog
        view.findViewById(R.id.cancel_select_wallet).setOnClickListener(v -> {
            dialogBtom.cancel();
        });
        view.findViewById(R.id.bn_select_wallet).setOnClickListener(v -> {
            try {
                Daemon.commands.callAttr("load_wallet", wallet_name);
                Daemon.commands.callAttr("select_wallet", wallet_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
            walletName.setText(wallet_name);
            dialogBtom.cancel();
            mGeneratecode();

        });
        RecyclerView recyPayaddress = view.findViewById(R.id.recy_payAdress);
        recyPayaddress.setLayoutManager(new LinearLayoutManager(SendOne2ManyMainPageActivity.this));
        choosePayAddressAdapetr = new ChoosePayAddressAdapetr(SendOne2ManyMainPageActivity.this, dataListName);
        recyPayaddress.setAdapter(choosePayAddressAdapetr);
        recyclerviewOnclick();
        dialogBtom.setContentView(view);
        Window window = dialogBtom.getWindow();
        //set pop_up size
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        //set locate
        window.setGravity(Gravity.BOTTOM);
        //set animal
        window.setWindowAnimations(R.style.AnimBottom);
        dialogBtom.show();
    }

    private void recyclerviewOnclick() {
        choosePayAddressAdapetr.setmOnItemClickListener(new ChoosePayAddressAdapetr.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                wallet_name = dataListName.get(position).getName();
                waletType = dataListName.get(position).getType();

            }
        });
    }

    //get fee num
    private void getFeerate() {
        PyObject get_fee_by_feerate = null;
        try {
            get_fee_by_feerate = Daemon.commands.callAttr("get_fee_by_feerate", strmapBtc, "", intmaxFee);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (get_fee_by_feerate != null) {
            Log.i("get_fee_by_feerate", "getFeerate: " + get_fee_by_feerate);
            String strnewFee = get_fee_by_feerate.toString();
            Gson gson = new Gson();
            GetsendFeenumBean getsendFeenumBean = gson.fromJson(strnewFee, GetsendFeenumBean.class);
            int fee = getsendFeenumBean.getFee();
            tvFee.setText(String.format("%ssat", String.valueOf(fee)));

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(SecondEvent updataHint) {
        String msgVote = updataHint.getMsg();
        if (msgVote.equals("finish")) {
            finish();
        }
    }
}
