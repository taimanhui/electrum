package org.haobtc.wallet.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.activities.transaction.CheckChainDetailWebActivity;
import org.haobtc.wallet.activities.transaction.DeatilMoreAddressActivity;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.bean.AddspeedNewtrsactionBean;
import org.haobtc.wallet.bean.GetnewcreatTrsactionListBean;
import org.haobtc.wallet.bean.ScanCheckDetailBean;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.event.SecondEvent;
import org.haobtc.wallet.utils.Daemon;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TransactionDetailsActivity extends BaseActivity {

    public static final String TAG_HIDE_WALLET = "TAG_HIDE_WALLET_TRANSACTION";
    @BindView(R.id.img_progressone)
    ImageView imgProgressone;
    @BindView(R.id.img_progressthree)
    ImageView imgProgressthree;
    @BindView(R.id.img_progressfour)
    ImageView imgProgressfour;
    @BindView(R.id.tet_Trone)
    TextView tetTrone;
    @BindView(R.id.tet_Trthree)
    TextView tetTrthree;
    @BindView(R.id.tet_Trfore)
    TextView tetTrfore;
    @BindView(R.id.tv_in_tb2)
    TextView tvInTb2;
    @BindView(R.id.tet_getMoneyaddress)
    TextView tetGetMoneyaddress;
    @BindView(R.id.tet_payAddress)
    TextView tetPayAddress;
    @BindView(R.id.textView14)
    TextView textView14;
    @BindView(R.id.textView15)
    TextView textView15;
    @BindView(R.id.textView18)
    TextView textView18;
    @BindView(R.id.textView20)
    TextView textView20;
    @BindView(R.id.sig_trans)
    Button sigTrans;
    public static final String TAG = "com.bixin.wallet.activities.TransactionDetailsActivity";
    @BindView(R.id.tet_content)
    TextView tetContent;
    @BindView(R.id.tet_state)
    TextView tetState;
    @BindView(R.id.tet_trsactionHash)
    TextView tetTrsactionHash;
    @BindView(R.id.tet_trsactionTime)
    TextView tetTrsactionTime;
    @BindView(R.id.lin_getMoreaddress)
    LinearLayout linGetMoreaddress;
    @BindView(R.id.tet_addressNum)
    TextView tetAddressNum;
    @BindView(R.id.tet_confirm)
    TextView tetConfirm;
    @BindView(R.id.lin_tractionHash)
    LinearLayout linTractionHash;
    @BindView(R.id.lin_tractionTime)
    LinearLayout linTractionTime;
    @BindView(R.id.tet_grive)
    TextView tetGrive;
    @BindView(R.id.tet_addSpeed)
    TextView tetAddSpeed;
    @BindView(R.id.lin_fee)
    LinearLayout linFee;
    @BindView(R.id.lin_payAddress)
    LinearLayout linPayAddress;
    @BindView(R.id.linearSignStatus)
    LinearLayout linearSignStatus;
    @BindView(R.id.tet_payAddressNum)
    TextView tetPayAddressNum;
    private String keyValue;
    private String tx_hash;
    private String listType;
    private String publicTrsation;
    private String jsondef_get;
    private String rawtx;
    private String strParse;
    private String language;
    private boolean isIsmine;
    private PyObject get_rbf_status;
    private String strwalletType;
    private SharedPreferences preferences;
    private String txid;
    private boolean canBroadcast;
    private SharedPreferences.Editor edit;
    private String newFeerate;
    private AlertDialog alertDialog;
    private String amount, fee;
    ArrayList<GetnewcreatTrsactionListBean.OutputAddrBean> output_addr;
    public static String signedRawTx;
    private String signTransction;
    private boolean set_rbf;
    private List<GetnewcreatTrsactionListBean.InputAddrBean> inputAddr;
    private List<ScanCheckDetailBean.DataBean.OutputAddrBean> outputAddrScan;
    private List<ScanCheckDetailBean.DataBean.InputAddrBean> inputAddrScan;
    private String unConfirmStatus;
    private String hideWallet;


    @Override
    public int getLayoutId() {
        return R.layout.trans_details;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        language = preferences.getString("language", "");
        set_rbf = preferences.getBoolean("set_rbf", false);//rbf transaction
        Intent intent = getIntent();
        if (!TextUtils.isEmpty(intent.getStringExtra("signed_raw_tx"))) {
            signedRawTx = intent.getStringExtra("signed_raw_tx");
        } else {
            hideWallet = intent.getStringExtra("hideWallet");//hide wallet transaction
            publicTrsation = intent.getStringExtra("txCreatTrsaction");
            keyValue = intent.getStringExtra("keyValue");//Judge which interface to jump in from
            tx_hash = intent.getStringExtra("tx_hash");
            listType = intent.getStringExtra("listType");
            unConfirmStatus = intent.getStringExtra("unConfirmStatus");//Manually change to the status to be confirmed
            signTransction = intent.getStringExtra("signTransction");//from SignActivity
            strParse = intent.getStringExtra("strParse");
            String dataTime = intent.getStringExtra("dataTime");
            strwalletType = intent.getStringExtra("strwalletType");
            isIsmine = intent.getBooleanExtra("isIsmine", false);//isIsmine -->recevid or send
            tetTrsactionTime.setText(dataTime);
        }
    }

    @Override
    public void initData() {
        if (!TextUtils.isEmpty(signedRawTx)) {
            jsonDetailData(signedRawTx);
            signedRawTx = "";
            return;
        }
        //isIsmine -->recevid or send
        if (isIsmine) {
            setRbfStatus();//Show RBF button or not
            tvInTb2.setText(R.string.sendetail);
            linFee.setVisibility(View.VISIBLE);
        } else {
            linFee.setVisibility(View.GONE);
            linearSignStatus.setVisibility(View.GONE);
            tvInTb2.setText(R.string.recevid);
        }
        if (!TextUtils.isEmpty(keyValue)) {
            switch (keyValue) {
                case "A":
                    //creat succses
                    mCreataSuccsesCheck();
                    break;
                case "B":
                    if ("history".equals(listType)) {
                        //isIsmine -->recevid or send
                        if (isIsmine) {
                            tvInTb2.setText(R.string.sendetail);
                        } else {
                            linearSignStatus.setVisibility(View.GONE);
                            tvInTb2.setText(R.string.recevid);
                        }
                        //histry trsaction detail
                        trsactionDetail();

                    } else if ("scan".equals(listType)) {
                        linearSignStatus.setVisibility(View.GONE);
                        tvInTb2.setText(R.string.recevid);
                        scanDataDetailMessage();

                    } else {
                        tvInTb2.setText(R.string.sendetail);
                        //creat succses
                        mCreataSuccsesCheck();

                    }
                    break;
                case "Sign":
                    jsonDetailData(signTransction);
                    break;
            }
        }
    }

    //Show RBF button or not
    private void setRbfStatus() {
        try {
            get_rbf_status = Daemon.commands.callAttr("get_rbf_status", tx_hash);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (get_rbf_status != null) {
            boolean rbfEnable = get_rbf_status.toBoolean();
            if (set_rbf) {
                if (rbfEnable) {
                    tetAddSpeed.setVisibility(View.VISIBLE);//rbf speed Whether to display
                } else {
                    tetAddSpeed.setVisibility(View.GONE);
                }
            }
        }
    }

    //trsaction detail
    private void trsactionDetail() {
        if (!TextUtils.isEmpty(tx_hash)) {
            PyObject get_tx_info;
            try {
                get_tx_info = Daemon.commands.callAttr("get_tx_info", tx_hash);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            if (get_tx_info != null) {
                jsondef_get = get_tx_info.toString();
                jsonDetailData(jsondef_get);
            }

        }
    }

    //creat succses check
    private void mCreataSuccsesCheck() {
        //get trsaction list content
        if (!TextUtils.isEmpty(publicTrsation)) {
            PyObject def_get_tx_info_from_raw = null;
            try {
                def_get_tx_info_from_raw = Daemon.commands.callAttr("get_tx_info_from_raw", publicTrsation);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (def_get_tx_info_from_raw != null) {
                jsondef_get = def_get_tx_info_from_raw.toString();
                jsonDetailData(jsondef_get);
            }
        }
    }

    //intent ->histry or create
    @SuppressLint("DefaultLocale")
    private void jsonDetailData(String jsondef_get) {
        // Log.d("jsonDetailData", "transactionDetail==== " + jsondef_get);
        GetnewcreatTrsactionListBean getnewcreatTrsactionListBean;
        try {
            Gson gson = new Gson();
            getnewcreatTrsactionListBean = gson.fromJson(jsondef_get, GetnewcreatTrsactionListBean.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            return;
        }
        amount = getnewcreatTrsactionListBean.getAmount();
        fee = getnewcreatTrsactionListBean.getFee();
        String description = getnewcreatTrsactionListBean.getDescription();
        String tx_status = getnewcreatTrsactionListBean.getTxStatus();
        output_addr = getnewcreatTrsactionListBean.getOutputAddr();
        List<Integer> signStatus = getnewcreatTrsactionListBean.getSignStatus();
        inputAddr = getnewcreatTrsactionListBean.getInputAddr();
        txid = getnewcreatTrsactionListBean.getTxid();
        tx_hash = txid;
        rawtx = getnewcreatTrsactionListBean.getTx();
        canBroadcast = getnewcreatTrsactionListBean.isCanBroadcast();
        edit.putString("signedRowtrsation", rawtx);
        edit.apply();
        if (inputAddr.size() != 0) {
            String addrInput = inputAddr.get(0).getAddr();
            tetPayAddress.setText(addrInput);
        }
        //trsaction hash
        tetTrsactionHash.setText(txid);
        //input address num
        int size = output_addr.size();
        if (language.equals("English")) {
            tetAddressNum.setText(String.format("%s%d", getString(R.string.wait), size));
            tetPayAddressNum.setText(String.format("%s%d", getString(R.string.wait), inputAddr.size()));
        } else {
            tetAddressNum.setText(String.format("%s%d%s", getString(R.string.wait), size, getString(R.string.ge)));
            tetPayAddressNum.setText(String.format("%s%d%s", getString(R.string.wait), inputAddr.size(), getString(R.string.ge)));
        }
        if (size != 0) {
            //output_address
            String addr = output_addr.get(0).getAddr();
            tetGetMoneyaddress.setText(addr);
        }

        if (signStatus != null) {
            Integer integer = signStatus.get(0);
            Integer integer1 = signStatus.get(1);
            String strNum = integer + "/" + integer1;
            textView20.setText(strNum);
        }
        //Transfer accounts num
        if (!TextUtils.isEmpty(amount)) {
            if (amount.contains("-")) {
                String replaceAmont = amount.replace("-", "");
                textView14.setText(String.format("-%s", replaceAmont));
            } else {
                if (isIsmine) {
                    textView14.setText(String.format("-%s", amount));
                } else {

                    textView14.setText(String.format("+%s", amount));
                }
            }
        }
        //Miner's fee
        if (!TextUtils.isEmpty(fee)) {
            textView15.setText(fee);
        }
        //Remarks
        tetContent.setText(description);
        if (!TextUtils.isEmpty(tx_status)) {
            //judge state
            judgeState(tx_status);
        }

    }

    //scan get
    @SuppressLint("DefaultLocale")
    private void scanDataDetailMessage() {
        Gson gson = new Gson();
        ScanCheckDetailBean scanCheckDetailBean = gson.fromJson(strParse, ScanCheckDetailBean.class);
        ScanCheckDetailBean.DataBean scanListdata = scanCheckDetailBean.getData();
        String amount = scanListdata.getAmount();
        String fee = scanListdata.getFee();
        String description = scanListdata.getDescription();
        String tx_status = scanListdata.getTxStatus();
        inputAddrScan = scanListdata.getInputAddr();
        outputAddrScan = scanListdata.getOutputAddr();

        List<Integer> signStatusMes = scanListdata.getSignStatus();
        String txid = scanListdata.getTxid();
        rawtx = scanListdata.getTx();
        //trsaction hash
        tetTrsactionHash.setText(txid);
        //input address num
        int size = outputAddrScan.size();
        if (language.equals("English")) {
            tetAddressNum.setText(String.format("%s%d", getString(R.string.wait), size));
            tetPayAddressNum.setText(String.format("%s%d", getString(R.string.wait), inputAddrScan.size()));
        } else {
            tetAddressNum.setText(String.format("%s%d%s", getString(R.string.wait), size, getString(R.string.ge)));
            tetPayAddressNum.setText(String.format("%s%d%s", getString(R.string.wait), inputAddrScan.size(), getString(R.string.ge)));
        }
        //output_address
        String addr = outputAddrScan.get(0).getAddr();
        tetGetMoneyaddress.setText(addr);

        if (signStatusMes != null) {
            Integer integer = signStatusMes.get(0);
            Integer integer1 = signStatusMes.get(1);
            String strNum = integer + "/" + integer1;
            textView20.setText(strNum);
        }
        //Transfer accounts num
        if (!TextUtils.isEmpty(amount)) {
            if (!"Transaction unrelated to your wallet".equals(amount)) {
                if (amount.contains("-")) {
                    String replaceAmont = amount.replace("-", "");
                    textView14.setText(String.format("-%s", replaceAmont));
                } else {
                    textView14.setText(amount);
                }
            } else {
                textView14.setText(amount);
            }

        }
        //Miner's fee
        if (!TextUtils.isEmpty(fee)) {
            textView15.setText(fee);
        }
        //Remarks
        tetContent.setText(description);
        if (!TextUtils.isEmpty(tx_status)) {
            //judge state
            judgeState(tx_status);
        }

    }

    //judge state
    private void judgeState(String tx_status) {
        if ("broadcast_complete".equals(unConfirmStatus)) {
            broadcastStatus();//sendOne2OnePageActivity  1-n wallet -->sign and broadcast ，Modify status manually
        } else {
            //transaction state
            if ("Unconfirmed".equals(tx_status)) {//Unconfirmed
                tetState.setText(R.string.waitchoose);
                sigTrans.setText(R.string.check_trsaction);
                imgProgressone.setVisibility(View.GONE);
                imgProgressthree.setVisibility(View.GONE);
                imgProgressfour.setVisibility(View.VISIBLE);
                //text color
                tetTrthree.setTextColor(getColor(R.color.button_bk_disableok));
                tetTrfore.setTextColor(getColor(R.color.button_bk_disableok));
                //transaction hash and time
                linTractionHash.setVisibility(View.VISIBLE);
                linTractionTime.setVisibility(View.GONE);
            } else if (tx_status.contains("confirmations")) {//Confirmed
                tetState.setText(R.string.completed);
                sigTrans.setText(R.string.check_trsaction);
                imgProgressone.setVisibility(View.GONE);
                imgProgressthree.setVisibility(View.GONE);
                imgProgressfour.setVisibility(View.VISIBLE);
                //Number of judgment confirmation
                String strConfirl = tx_status.replaceAll(" confirmations", "");
                BigDecimal bignum1 = new BigDecimal(strConfirl);
                BigDecimal bigDecimal = new BigDecimal(100);
                int mathMax = bignum1.compareTo(bigDecimal);
                if (mathMax > 0) {
                    tetConfirm.setText(String.format("%s%s", getString(R.string.confirmnum), ">100"));
                } else {
                    tetConfirm.setText(String.format("%s%s", getString(R.string.confirmnum), strConfirl));
                }

                tetTrthree.setTextColor(getColor(R.color.button_bk_disableok));
                tetTrfore.setTextColor(getColor(R.color.button_bk_disableok));
                //transaction hash and time
                linTractionHash.setVisibility(View.VISIBLE);
                linTractionTime.setVisibility(View.VISIBLE);
            } else if (tx_status.contains("Unsigned")) {//unsigned
                linPayAddress.setVisibility(View.VISIBLE);
                tetState.setText(R.string.unsigned);
                sigTrans.setText(R.string.signature_trans);
            } else if (tx_status.contains("Signed") || tx_status.contains("Local") || canBroadcast) {//signed
                tetState.setText(R.string.wait_broadcast);
                sigTrans.setText(R.string.broadcast);
                tetGrive.setVisibility(View.VISIBLE);
                linPayAddress.setVisibility(View.GONE);
                //progress
                imgProgressone.setVisibility(View.GONE);
                imgProgressthree.setVisibility(View.VISIBLE);
                imgProgressfour.setVisibility(View.GONE);
                tetTrthree.setTextColor(getColor(R.color.button_bk_disableok));

            } else if (tx_status.contains("Partially signed")) {//
                tetState.setText(R.string.transaction_waitting);
                sigTrans.setText(R.string.signature_trans);
                //progress
                imgProgressone.setVisibility(View.VISIBLE);
                imgProgressthree.setVisibility(View.GONE);
                imgProgressfour.setVisibility(View.GONE);
            }
        }
    }

    private Runnable runnable = this::gotoConfirmOnHardware;

    private void gotoConfirmOnHardware() {
        String strPayAddress = tetPayAddress.getText().toString();
        Intent intentCon = new Intent(TransactionDetailsActivity.this, ConfirmOnHardware.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("output", output_addr);
        bundle.putString("pay_address", strPayAddress);
        bundle.putString("fee", fee);
        intentCon.putExtra("outputs", bundle);
        startActivity(intentCon);
    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.img_share, R.id.lin_getMoreaddress, R.id.tet_addSpeed, R.id.sig_trans, R.id.lin_payAddress})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.img_share:
                Intent intent = new Intent(TransactionDetailsActivity.this, ShareOtherActivity.class);
                intent.putExtra("rowTrsaction", tx_hash);
                intent.putExtra("rowTx", rawtx);
                startActivity(intent);
                break;
            case R.id.sig_trans:
                String strBtncontent = sigTrans.getText().toString();
                if (strBtncontent.equals(getString(R.string.broadcast))) {
                    //broadcast transaction
                    braodcastTrsaction();

                } else if (strBtncontent.equals(getString(R.string.check_trsaction))) {
                    Intent intent1 = new Intent(TransactionDetailsActivity.this, CheckChainDetailWebActivity.class);
                    intent1.putExtra("checkTxid", txid);
                    startActivity(intent1);
                } else if (strBtncontent.equals(getString(R.string.signature_trans))) {
                    if ("standard".equals(strwalletType)) {
                        //sign input pass
                        signInputpassDialog();
                    } else {
                        CommunicationModeSelector.runnables.clear();
                        CommunicationModeSelector.runnables.add(runnable);
                        Intent intent1 = new Intent(this, CommunicationModeSelector.class);
                        if (!TextUtils.isEmpty(hideWallet)) {
                            intent1.putExtra("tag", TAG_HIDE_WALLET);
                        } else {
                            intent1.putExtra("tag", TAG);
                        }
                        intent1.putExtra("extras", rawtx);
                        startActivity(intent1);
                    }
                } else {
                    mToast(getString(R.string.unrelated_transaction));
                }

                break;
            case R.id.lin_getMoreaddress:
                //transaction detail or create success
                Intent intent1 = new Intent(TransactionDetailsActivity.this, DeatilMoreAddressActivity.class);
                if (output_addr != null) {
                    intent1.putExtra("jsondef_get", (Serializable) output_addr);
                } else {
                    intent1.putExtra("jsondef_getScan", (Serializable) outputAddrScan);
                }
                startActivity(intent1);
                break;
            case R.id.tet_addSpeed:
                ifHaveRbf();
                break;
            case R.id.lin_payAddress:
                Intent intent2Pay = new Intent(TransactionDetailsActivity.this, DeatilMoreAddressActivity.class);
                if (inputAddr != null) {
                    intent2Pay.putExtra("payAddress", (Serializable) inputAddr);
                } else {
                    intent2Pay.putExtra("payAddressScan", (Serializable) inputAddrScan);
                }
                intent2Pay.putExtra("addressType", "pay");
                intent2Pay.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent2Pay);
                break;
        }
    }


    //Radio broadcast
    private void braodcastTrsaction() {
        String signedRowTrsation = preferences.getString("signedRowtrsation", "");
        try {
            Daemon.commands.callAttr("broadcast_tx", signedRowTrsation);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        EventBus.getDefault().post(new FirstEvent("22"));
        broadcastStatus();
    }

    private void broadcastStatus() {
        tetState.setText(R.string.waitchoose);
        sigTrans.setText(R.string.check_trsaction);
        imgProgressone.setVisibility(View.GONE);
        imgProgressthree.setVisibility(View.GONE);
        imgProgressfour.setVisibility(View.VISIBLE);
        //text color
        tetTrthree.setTextColor(getColor(R.color.button_bk_disableok));
        tetTrfore.setTextColor(getColor(R.color.button_bk_disableok));
        //trsaction hash and time
        linTractionHash.setVisibility(View.VISIBLE);
        mToast(getString(R.string.broadcast_success));

    }

    private void signInputpassDialog() {
        View view1 = LayoutInflater.from(TransactionDetailsActivity.this).inflate(R.layout.input_wallet_pass, null, false);
        AlertDialog alertDialog = new AlertDialog.Builder(TransactionDetailsActivity.this).setView(view1).create();
        EditText str_pass = view1.findViewById(R.id.edit_password);
        view1.findViewById(R.id.btn_enter_wallet).setOnClickListener(v -> {
            String strPassword = str_pass.getText().toString();
            if (TextUtils.isEmpty(strPassword)) {
                mToast(getString(R.string.please_input_pass));
                return;
            }
            try {
                PyObject sign_tx = Daemon.commands.callAttr("sign_tx", rawtx, new Kwarg("password", strPassword));
                if (sign_tx != null) {
                    jsonDetailData(sign_tx.toString());
                    alertDialog.dismiss();
                    EventBus.getDefault().post(new FirstEvent("22"));

                }
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

    private void ifHaveRbf() {
        PyObject get_rbf_fee_info = null;
        try {
            get_rbf_fee_info = Daemon.commands.callAttr("get_rbf_fee_info", tx_hash);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (get_rbf_fee_info != null) {
            String strNewfee = get_rbf_fee_info.toString();
            View viewSpeed = LayoutInflater.from(this).inflate(R.layout.add_speed, null, false);
            alertDialog = new AlertDialog.Builder(this).setView(viewSpeed).create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ImageView img_Cancle = viewSpeed.findViewById(R.id.cancel_select_wallet);
            TextView tetNewfee = viewSpeed.findViewById(R.id.tet_Newfee);
            try {
                JSONObject jsonObject = new JSONObject(strNewfee);
                newFeerate = jsonObject.getString("new_feerate");
                tetNewfee.setText(String.format("%s  %s sat/byte", getString(R.string.speed_fee), newFeerate));
                img_Cancle.setOnClickListener(v -> {
                    alertDialog.dismiss();
                });
                viewSpeed.findViewById(R.id.btn_add_Speed).setOnClickListener(v -> {
                    confirmedSpeed();
                });
                alertDialog.show();
                WindowManager.LayoutParams lp = alertDialog.getWindow().getAttributes();
                lp.width = 950;
                lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                alertDialog.getWindow().setAttributes(lp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    //confirm speed
    private void confirmedSpeed() {
        PyObject create_bump_fee = null;
        try {
            create_bump_fee = Daemon.commands.callAttr("create_bump_fee", tx_hash, newFeerate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (create_bump_fee != null) {
            String strNewTX = create_bump_fee.toString();
            Gson gson = new Gson();
            AddspeedNewtrsactionBean addspeedNewtrsactionBean = gson.fromJson(strNewTX, AddspeedNewtrsactionBean.class);
            publicTrsation = addspeedNewtrsactionBean.getNewTx();
            mToast(getString(R.string.addspeed_success));
            EventBus.getDefault().post(new FirstEvent("22"));
            edit.putString("signedRowtrsation", publicTrsation);
            edit.apply();
            mCreataSuccsesCheck();
            alertDialog.dismiss();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(SecondEvent updataHint) {
        String msgVote = updataHint.getMsg();
        if (msgVote.equals("finish")) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}

