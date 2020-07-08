package org.haobtc.wallet.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.google.common.base.Strings;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.activities.transaction.DeatilMoreAddressActivity;
import org.haobtc.wallet.adapter.ChoosePayAddressAdapter;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.bean.AddressEvent;
import org.haobtc.wallet.bean.GetAddressBean;
import org.haobtc.wallet.bean.GetCodeAddressBean;
import org.haobtc.wallet.bean.GetnewcreatTrsactionListBean;
import org.haobtc.wallet.bean.GetsendFeenumBean;
import org.haobtc.wallet.bean.HardwareFeatures;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.event.HandlerEvent;
import org.haobtc.wallet.event.MainpageWalletEvent;
import org.haobtc.wallet.event.SecondEvent;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.IndicatorSeekBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.heaton.blelibrary.ble.Ble;


public class SendOne2ManyMainPageActivity extends BaseActivity {

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
    @BindView(R.id.linear_seek)
    LinearLayout linearSeek;
    private ArrayList<AddressEvent> dataListName;
    private Dialog dialogBtom;
    private ChoosePayAddressAdapter choosePayAddressAdapetr;
    private String mwalletName;
    private double pro;
    private String strmapBtc;
    private List addressList;
    private int intmaxFee;
    private String walletType;
    private String walletTypeToSign;
    private ArrayList<GetnewcreatTrsactionListBean.OutputAddrBean> outputAddr;
    private CommunicationModeSelector modeSelector;
    private String payAddress;
    private String strFeemontAs;
    private boolean showSeek = true;
    private String onclickName;
    private int walletNamePos;
    private SharedPreferences preferences;
    private SharedPreferences.Editor edit;
    private String fee;

    @Override
    public int getLayoutId() {
        return R.layout.send_one2many_main;
    }

    @Override
    @SuppressLint("CommitPrefEdits")
    public void initView() {
        ButterKnife.bind(this);
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        //1-n wallet  --> Direct signature and broadcast
        walletTypeToSign = preferences.getString("wallet_type_to_sign", "");
        String baseUnit = preferences.getString("base_unit", "mBTC");
        Intent intent = getIntent();
        String hideRefresh = getIntent().getStringExtra("hideRefresh");
        addressList = (ArrayList) getIntent().getSerializableExtra("listdetail");
        mwalletName = intent.getStringExtra("wallet_name");
        walletType = intent.getStringExtra("wallet_type");
        int addressNum = intent.getIntExtra("addressNum", 0);
        String totalAmount = intent.getStringExtra("totalAmount");

        //To many people Coin making
        strmapBtc = intent.getStringExtra("strmapBtc");
        Log.i("nihaoajinxiaomin", "initView:++ " + strmapBtc);
        walletName.setText(mwalletName);
        onclickName = mwalletName;
        addressCount.setText(String.format("%s %s", String.valueOf(addressNum), getString(R.string.to_num)));
        tvAmount.setText(String.format("%s %s", totalAmount, baseUnit));
        testWalletUnit.setText(baseUnit);

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
        PyObject getDefaultFeeStatuses = null;
        try {
            getDefaultFeeStatuses = Daemon.commands.callAttr("get_default_fee_status");
        } catch (Exception e) {
            e.printStackTrace();
            mToast(e.getMessage());
            return;
        }
        if (getDefaultFeeStatuses != null) {
            String strFee = getDefaultFeeStatuses.toString();
            Log.i("get_default_fee", "strFee:   " + strFee);
            if (strFee.contains("sat/byte")) {
                strFeemontAs = strFee.substring(0, strFee.indexOf("sat/byte") + 8);
                String strFeeamont = strFee.substring(0, strFee.indexOf("sat/byte"));
                String strMax = strFeeamont.replaceAll(" ", "").split("\\.", 2)[0];
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
        PyObject getWalletsListInfo = null;
        try {
            getWalletsListInfo = Daemon.commands.callAttr("list_wallets");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (getWalletsListInfo != null) {
            String toString = getWalletsListInfo.toString();
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


    @SingleClick
    @OnClick({R.id.lin_chooseAddress, R.id.linearLayout10, R.id.linear_feeSelect, R.id.img_back, R.id.create_trans_one2many})
    public void onViewClicked(View view) {
        PyObject mktx;
        switch (view.getId()) {
            case R.id.lin_chooseAddress:
                Log.i("wallet_type_to_sign", "onItemClick: " + walletTypeToSign);
                //check wallet
                showDialogs(SendOne2ManyMainPageActivity.this, R.layout.select_send_wallet_popwindow);
                break;
            case R.id.linearLayout10:
                Intent intent1 = new Intent(SendOne2ManyMainPageActivity.this, DeatilMoreAddressActivity.class);
                intent1.putExtra("listdetail", (Serializable) addressList);
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent1);
                break;
            case R.id.linear_feeSelect:
                if (showSeek) {
                    feeSelect.setImageDrawable(getDrawable(R.drawable.jiantou_up));
                    linearSeek.setVisibility(View.VISIBLE);
                    showSeek = false;
                } else {
                    feeSelect.setImageDrawable(getDrawable(R.drawable.jiantou));
                    linearSeek.setVisibility(View.GONE);
                    showSeek = true;
                }
                break;
            case R.id.img_back:
                finish();
                break;
            case R.id.create_trans_one2many:
                try {
                    mktx = Daemon.commands.callAttr("mktx", "", "");
                } catch (Exception e) {
                    e.printStackTrace();
                    if (e.getMessage().contains("Insufficient funds")) {
                        mToast(getString(R.string.wallet_insufficient));
                    } else if (e.getMessage().contains("Transaction is unrelated to this wallet")) {
                        mToast(getString(R.string.error_wallet_transaction));
                    } else {
                        mToast(getString(R.string.changeaddress));
                    }
                    return;
                }
                if (mktx != null) {
                    String jsonObj = mktx.toString();
                    Gson gson = new Gson();
                    GetAddressBean getAddressBean = gson.fromJson(jsonObj, GetAddressBean.class);
                    String rowtx = getAddressBean.getTx();
                    if (!TextUtils.isEmpty(rowtx)) {
                        if (walletTypeToSign.contains("1-")) {
                            try {
                                PyObject txInfoFromRaw = Daemon.commands.callAttr("get_tx_info_from_raw", rowtx);
                                gson = new Gson();
                                GetnewcreatTrsactionListBean getnewcreatTrsactionListBean = gson.fromJson(txInfoFromRaw.toString(), GetnewcreatTrsactionListBean.class);
                                outputAddr = getnewcreatTrsactionListBean.getOutputAddr();
                                fee = getnewcreatTrsactionListBean.getFee();
                            } catch (Exception e) {
                                e.printStackTrace();
                                return;
                            }
                            if (onclickName.equals(mwalletName)) {
                                EventBus.getDefault().post(new FirstEvent("22"));
                            } else {
                                EventBus.getDefault().post(new MainpageWalletEvent("22", walletNamePos));
                            }
                            //1-n wallet  --> Direct signature and broadcast
                            if ("1-1".equals(walletTypeToSign) && Ble.getInstance().getConnetedDevices().size() != 0) {
                                String deviceId = Daemon.commands.callAttr("get_device_info").toString().replaceAll("\"", "");
                                SharedPreferences devices = getSharedPreferences("devices", MODE_PRIVATE);
                                String feature = devices.getString(deviceId, "");
                                if (!Strings.isNullOrEmpty(feature)) {
                                    HardwareFeatures features = HardwareFeatures.objectFromData(feature);
                                    if (Ble.getInstance().getConnetedDevices().get(0).getBleName().equals(features.getBleName())) {
                                        EventBus.getDefault().postSticky(new HandlerEvent());
                                    }
                                }

                            }
                            CommunicationModeSelector.runnables.clear();
                            CommunicationModeSelector.runnables.add(runnable);
                            Intent intent2 = new Intent(this, CommunicationModeSelector.class);
                            intent2.putExtra("tag", TAG);
                            intent2.putExtra("extras", rowtx);
                            startActivity(intent2);
                        } else {
                            EventBus.getDefault().post(new SecondEvent("finish"));
//                            if (!TextUtils.isEmpty(hideRefresh)) {
//                                EventBus.getDefault().post(new SecondEvent("update_hide_transaction"));
//                            } else {
                            if (onclickName.equals(mwalletName)) {
                                EventBus.getDefault().post(new FirstEvent("22"));
                            } else {
                                EventBus.getDefault().post(new MainpageWalletEvent("22", walletNamePos));
                            }
//                            }
                            Intent intent = new Intent(SendOne2ManyMainPageActivity.this, TransactionDetailsActivity.class);
                            intent.putExtra("tx_hash", rowtx);
                            intent.putExtra("keyValue", "A");
                            intent.putExtra("is_mine", true);
                            intent.putExtra("strwalletType", walletType);
                            intent.putExtra("txCreatTrsaction", rowtx);
                            startActivity(intent);
                        }
                    }
                }
                break;
            case R.id.btnRecommendFee:
                mToast(strFeemontAs);
                break;
            default:
        }
    }


    private Runnable runnable = this::gotoConfirmOnHardware;

    private void gotoConfirmOnHardware() {
        EventBus.getDefault().post(new SecondEvent("finish"));
        Intent intentCon = new Intent(SendOne2ManyMainPageActivity.this, ConfirmOnHardware.class);
        intentCon.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        Bundle bundle = new Bundle();
        bundle.putSerializable("output", outputAddr);
        bundle.putString("pay_address", payAddress);
        bundle.putString("fee", fee);
        intentCon.putExtra("outputs", bundle);
        startActivityForResult(intentCon, 1);
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
            String strScrollPass = preferences.getString(mwalletName, "");
            boolean haveCreateNopass = preferences.getBoolean("haveCreateNopass", false);
            if (!TextUtils.isEmpty(strScrollPass)) {
                try {
                    Daemon.commands.callAttr("load_wallet", mwalletName);
                    Daemon.commands.callAttr("select_wallet", mwalletName);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                walletName.setText(mwalletName);
                dialogBtom.cancel();
                mGeneratecode();
            } else {
                if (haveCreateNopass) {
                    try {
                        Daemon.commands.callAttr("load_wallet", mwalletName);
                        Daemon.commands.callAttr("select_wallet", mwalletName);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    walletName.setText(mwalletName);
                    dialogBtom.cancel();
                    mGeneratecode();
                } else {
                    //When no password has been entered for switching Wallet
                    inputWalletPass();
                }
            }

        });
        RecyclerView recyPayaddress = view.findViewById(R.id.recy_payAdress);
        recyPayaddress.setLayoutManager(new LinearLayoutManager(SendOne2ManyMainPageActivity.this));
        choosePayAddressAdapetr = new ChoosePayAddressAdapter(SendOne2ManyMainPageActivity.this, dataListName);
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

    private void inputWalletPass() {
        //input password
        View view1 = LayoutInflater.from(this).inflate(R.layout.input_wallet_pass, null, false);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view1).create();
        EditText strPass = view1.findViewById(R.id.edit_password);
        view1.findViewById(R.id.btn_enter_wallet).setOnClickListener(v -> {
            String strPassword = strPass.getText().toString();
            if (TextUtils.isEmpty(strPassword)) {
                mToast(getString(R.string.please_input_pass));
                return;
            }
            try {
                Daemon.commands.callAttr("load_wallet", mwalletName, new Kwarg("password", strPassword));
                Daemon.commands.callAttr("select_wallet", mwalletName);
                walletName.setText(mwalletName);
                dialogBtom.cancel();
                //get pay address
                mGeneratecode();
                edit.putString(mwalletName, strPassword);
                edit.apply();
                alertDialog.dismiss();

            } catch (Exception e) {
                if (e.getMessage().contains("Incorrect password")) {
                    mToast(getString(R.string.wrong_pass));
                }
                e.printStackTrace();
            }

        });
        view1.findViewById(R.id.cancel_select_wallet).setOnClickListener(v -> {
            alertDialog.dismiss();
        });
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    private void recyclerviewOnclick() {
        choosePayAddressAdapetr.setmOnItemClickListener(new ChoosePayAddressAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                walletNamePos = position;
                mwalletName = dataListName.get(position).getName();
                walletType = dataListName.get(position).getType();
                walletTypeToSign = walletType;
            }
        });
    }

    //get fee num
    private void getFeerate() {
        PyObject getFeeByFeeRate = null;
        try {
            getFeeByFeeRate = Daemon.commands.callAttr("get_fee_by_feerate", strmapBtc, "", intmaxFee);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (getFeeByFeeRate != null) {
            Log.i("get_fee_by_feerate", "getFeerate: " + getFeeByFeeRate);
            String strnewFee = getFeeByFeeRate.toString();
            Gson gson = new Gson();
            GetsendFeenumBean getsendFeenumBean = gson.fromJson(strnewFee, GetsendFeenumBean.class);
            int fee = getsendFeenumBean.getFee();
            tvFee.setText(String.format("%ssat", String.valueOf(fee)));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(SecondEvent updataHint) {
        String msgVote = updataHint.getMsg();
        if ("finish".equals(msgVote)) {
            finish();
        }
    }
}
