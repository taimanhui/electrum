package org.haobtc.wallet.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.chaquo.python.PyObject;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.MainActivity;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector;
import org.haobtc.wallet.activities.transaction.CheckChainDetailWebActivity;
import org.haobtc.wallet.activities.transaction.DeatilMoreAddressActivity;
import org.haobtc.wallet.asynctask.BusinessAsyncTask;
import org.haobtc.wallet.bean.AddspeedBean;
import org.haobtc.wallet.bean.AddspeedNewtrsactionBean;
import org.haobtc.wallet.bean.GetnewcreatTrsactionListBean;
import org.haobtc.wallet.bean.HardwareFeatures;
import org.haobtc.wallet.bean.ScanCheckDetailBean;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.event.ResultEvent;
import org.haobtc.wallet.event.SecondEvent;
import org.haobtc.wallet.event.SignFailedEvent;
import org.haobtc.wallet.event.SignResultEvent;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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

public class TransactionDetailsActivity extends BaseActivity implements BusinessAsyncTask.Helper {

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
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tv_in_tb2)
    TextView tvInTb2;
    @BindView(R.id.img_share)
    ImageView imgShare;
    @BindView(R.id.tb2)
    RelativeLayout tb2;
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
    private String rowtx;
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
    private boolean executable = true;
    private String pin = "";
    public static String signedRawTx;
    private boolean isActive;
    private boolean ready;
    private CommunicationModeSelector modeSelector;
    private String signTransction;
    private boolean set_rbf;
    private List<GetnewcreatTrsactionListBean.InputAddrBean> inputAddr;
    private List<ScanCheckDetailBean.DataBean.OutputAddrBean> outputAddrScan;
    private List<ScanCheckDetailBean.DataBean.InputAddrBean> inputAddrScan;
    private String unrelatedTransaction;
    private Boolean aBoolean;
    private boolean done;

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
            publicTrsation = intent.getStringExtra("txCreatTrsaction");
            keyValue = intent.getStringExtra("keyValue");//Judge which interface to jump in from
            tx_hash = intent.getStringExtra("tx_hash");
            listType = intent.getStringExtra("listType");
            signTransction = intent.getStringExtra("signTransction");//from SignActivity
            strParse = intent.getStringExtra("strParse");
            String dataTime = intent.getStringExtra("dataTime");
            strwalletType = intent.getStringExtra("strwalletType");
            isIsmine = intent.getBooleanExtra("isIsmine", false);//isIsmine -->recevid or send
            tetTrsactionTime.setText(dataTime);
        }

        Log.i("listType", "listType--: " + listType + "   tx_hash--: " + tx_hash + "   isIsmine -- : " + isIsmine);
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
            aBoolean = get_rbf_status.toBoolean();
            if (set_rbf) {
                Log.i("setRbfStatus", "setRbfStatus: "+aBoolean);
                if (aBoolean) {
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
            PyObject get_tx_info = null;
            try {
                get_tx_info = Daemon.commands.callAttr("get_tx_info", tx_hash);
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("printStackTrace", "tr----- " + e.getMessage());
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
        Log.i("jsonDetailData", "jsonDetail==== " + jsondef_get);
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
        rowtx = getnewcreatTrsactionListBean.getTx();
        canBroadcast = getnewcreatTrsactionListBean.isCanBroadcast();
        edit.putString("signedRowtrsation", rowtx);
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
        rowtx = scanListdata.getTx();
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
                unrelatedTransaction = amount;
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
        //trsaction state
        if ("Unconfirmed".equals(tx_status)) {//Unconfirmed
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
            //trsaction hash and time
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

        } else if (tx_status.contains("Partially signed")) {//you are signer
            tetState.setText(R.string.you_are_signed);
            sigTrans.setText(R.string.forWord_orther);
            //progress
            imgProgressone.setVisibility(View.VISIBLE);
            imgProgressthree.setVisibility(View.GONE);
            imgProgressfour.setVisibility(View.GONE);
        }
    }

    private Runnable runnable = this::gotoConfirmOnHardware;

    private void gotoConfirmOnHardware() {
        String strPayAddress = tetPayAddress.getText().toString();
        Log.i("jsdhujbejnfksndml", "output_addr: " + output_addr);
        Intent intentCon = new Intent(TransactionDetailsActivity.this, ConfirmOnHardware.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("output", output_addr);
        bundle.putString("pay_address", strPayAddress);
        bundle.putString("fee", fee);
        intentCon.putExtra("outputs", bundle);
        startActivityForResult(intentCon, 1);
    }

    @OnClick({R.id.img_back, R.id.img_share, R.id.lin_getMoreaddress, R.id.tet_addSpeed, R.id.sig_trans, R.id.lin_payAddress})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.img_share:
                Intent intent = new Intent(TransactionDetailsActivity.this, ShareOtherActivity.class);
                intent.putExtra("rowTrsaction", tx_hash);
                intent.putExtra("rowTx", rowtx);
                startActivity(intent);
                break;
            case R.id.sig_trans:
                if (TextUtils.isEmpty(unrelatedTransaction)) {
                    String strBtncontent = sigTrans.getText().toString();
                    if (strBtncontent.equals(getString(R.string.forWord_orther))) {
                        Intent intent1 = new Intent(TransactionDetailsActivity.this, ShareOtherActivity.class);
                        intent1.putExtra("rowTrsaction", tx_hash);
                        intent1.putExtra("rowTx", rowtx);
                        startActivity(intent1);

                    } else if (strBtncontent.equals(getString(R.string.broadcast))) {
                        //bradcast trsaction
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
                            showCustomerDialog();
                        }
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
                intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
                intent2Pay.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent2Pay);
                break;
        }
    }

    private void showCustomerDialog() {
        List<Runnable> runnables = new ArrayList<>();
        runnables.add(runnable);
        modeSelector = new CommunicationModeSelector(TAG, runnables, rowtx);
        modeSelector.show(getSupportFragmentManager(), "");
    }

    //Radio broadcast
    private void braodcastTrsaction() {
        String signedRowTrsation = preferences.getString("signedRowtrsation", "");
        Log.i("braodcastTrsact", "braodcastTrsaction: " + signedRowTrsation);
        try {
            Daemon.commands.callAttr("broadcast_tx", signedRowTrsation);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
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
        EventBus.getDefault().post(new FirstEvent("22"));
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
                PyObject sign_tx = Daemon.commands.callAttr("sign_tx", rowtx, strPassword);
                if (sign_tx != null) {
                    Log.i("sign_txkkkkkkk", "sign_tx: " + sign_tx);
                    jsonDetailData(sign_tx.toString());
                    alertDialog.dismiss();
                    EventBus.getDefault().post(new FirstEvent("22"));

                }
            } catch (Exception e) {
                Log.i("sign_txkkkkkkk", "+++++++++++= " + e.getMessage());
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
            Log.i("strNewfee", "++++++++: " + e.getMessage());
            e.printStackTrace();
        }
        if (get_rbf_fee_info != null) {
            String strNewfee = get_rbf_fee_info.toString();
            Log.i("strNewfee", "--------: " + strNewfee);
            View viewSpeed = LayoutInflater.from(this).inflate(R.layout.add_speed, null, false);
            alertDialog = new AlertDialog.Builder(this).setView(viewSpeed).create();
            ImageView img_Cancle = viewSpeed.findViewById(R.id.cancel_select_wallet);
            TextView tetNewfee = viewSpeed.findViewById(R.id.tet_Newfee);

            Gson gson = new Gson();
            AddspeedBean addspeedBean = gson.fromJson(strNewfee, AddspeedBean.class);
            newFeerate = addspeedBean.getNewFeerate();
            tetNewfee.setText(String.format("%s  %s", getString(R.string.speed_fee), newFeerate));
            img_Cancle.setOnClickListener(v -> {
                alertDialog.dismiss();
            });
            viewSpeed.findViewById(R.id.btn_add_Speed).setOnClickListener(v -> {
                confirmedSpeed();
            });
            alertDialog.show();
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
            Log.i("create_bump_fee", "confirmedSpeed: " + create_bump_fee.toString());
            String strNewTX = create_bump_fee.toString();
            Gson gson = new Gson();
            AddspeedNewtrsactionBean addspeedNewtrsactionBean = gson.fromJson(strNewTX, AddspeedNewtrsactionBean.class);
            publicTrsation = addspeedNewtrsactionBean.getNewTx();
            edit.putString("signedRowtrsation", publicTrsation);
            edit.apply();
            mCreataSuccsesCheck();
            alertDialog.dismiss();
        }
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
            CommunicationModeSelector.customerUI.put("pin", pin);
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
                            CommunicationModeSelector.customerUI.put("pin", pin);
                            CommunicationModeSelector.handler.sendEmptyMessage(CommunicationModeSelector.SHOW_PROCESSING);

                        } else if (isActive) {
                            // nfc 激活
                            done = true;
                        }
                        break;
                    case CommunicationModeSelector.PIN_CURRENT: // 签名
                        if (!isNFC) { // ble
                            CommunicationModeSelector.customerUI.put("pin", pin);
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

