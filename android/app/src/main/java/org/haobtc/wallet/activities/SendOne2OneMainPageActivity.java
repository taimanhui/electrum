package org.haobtc.wallet.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.PyObject;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.ChoosePayAddressAdapetr;
import org.haobtc.wallet.bean.AddressEvent;
import org.haobtc.wallet.bean.GetAddressBean;
import org.haobtc.wallet.bean.MainNewWalletBean;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.IndicatorSeekBar;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SendOne2OneMainPageActivity extends BaseActivity implements View.OnClickListener, TextWatcher {
    @BindView(R.id.edit_changeMoney)
    TextView editChangeMoney;
    @BindView(R.id.seek_bar)
    IndicatorSeekBar seekBar;
    @BindView(R.id.tv_indicator)
    TextView tvIndicator;
    @BindView(R.id.lin_btcAddress)
    LinearLayout linBtcAddress;
    private LinearLayout selectSend;
    private ImageView selectSigNum, buttonSweep;
    private EditText editTextComments, editAddress;
    private TextView bytesCount, buttonPaste;
    private Button buttonCreate;
    private Dialog dialogBtom;
    private String strContent = "";
    private EditText tetMoneye;
    private RxPermissions rxPermissions;
    private static final int REQUEST_CODE = 0;
    private EditText tetamount;
    private RecyclerView recyPayaddress;
    private List<AddressEvent> dataListName;
    private ChoosePayAddressAdapetr choosePayAddressAdapetr;
    private String straddress;
    private String strAmount;
    private TextView tetWalletname;
    private String wallet_name;
    private TextView textView;
    private double pro;
    private Gson gson;
    private SharedPreferences preferences;
    private SharedPreferences.Editor edit;
    private String wallet_amount;
    private PyObject get_wallets_list;
    private PyObject mktx;
    private ImageView imgBack;
    private int walletmoney = 0;
    private int catorText;
    private PyObject pyObject;
    private String chooseAmount;
    private String chooseName;
    private PyObject select_wallet;
    private int intmaxFee;

    @Override
    public int getLayoutId() {
        return R.layout.send_one2one_main_page;
    }

    public void initView() {
        ButterKnife.bind(this);
        preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        edit = preferences.edit();
        selectSend = findViewById(R.id.llt_select_wallet);
        tetMoneye = findViewById(R.id.tet_Money);
        tetamount = findViewById(R.id.amount);
        selectSigNum = findViewById(R.id.fee_select);
        tetWalletname = findViewById(R.id.tet_WalletName);
        textView = findViewById(R.id.tv_send2many);
        editTextComments = findViewById(R.id.comment_edit);
        editAddress = findViewById(R.id.edit_address_one2one);
        bytesCount = findViewById(R.id.byte_count);
        buttonCreate = findViewById(R.id.create_trans_one2one);
        buttonSweep = findViewById(R.id.bn_sweep_one2noe);
        buttonPaste = findViewById(R.id.bn_paste_one2one);
        imgBack = findViewById(R.id.img_back);
        imgBack.setOnClickListener(this);
        tetamount.addTextChangedListener(this);
        init();
        //edittext focus change
        focusChange();
    }

    //edittext focus change
    private void focusChange() {
        linBtcAddress.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                linBtcAddress.getWindowVisibleDisplayFrame(r);
                int screenHeight = linBtcAddress.getRootView()
                        .getHeight();
                int heightDifference = screenHeight - (r.bottom);
                if (heightDifference > 200) {
                    //Soft keyboard display
                    mToast("mmmmmm");
                } else {
                    //Soft keyboard hidden
                    mToast("cccc");
                }
            }
        });
        editAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mToast("dddd");
                } else {
                    mToast("cccccccc");
                }
            }
        });

    }

    @SuppressLint("DefaultLocale")
    private void init() {
        rxPermissions = new RxPermissions(this);
        selectSend.setOnClickListener(this);
        selectSigNum.setOnClickListener(this);
        textView.setOnClickListener(this);
        buttonCreate.setOnClickListener(this);
        buttonSweep.setOnClickListener(this);
        buttonPaste.setOnClickListener(this);
        dataListName = new ArrayList<>();
        Intent intent = getIntent();
        wallet_name = intent.getStringExtra("wallet_name");
        wallet_amount = intent.getStringExtra("wallet_balance");
        String sendAdress = intent.getStringExtra("sendAdress");
        String sendmessage = intent.getStringExtra("sendmessage");
        tetWalletname.setText(wallet_name);
        int sendamount = intent.getIntExtra("sendamount", 0);
        editAddress.setText(sendAdress);
        if (sendamount != 0) {
            tetamount.setText(String.format("%d", sendamount));
        }
        if (!TextUtils.isEmpty(sendmessage)) {
            editTextComments.setText(String.format("%s", sendmessage));
        } else {
            editTextComments.setText(String.format("%s", ""));
        }

        tetMoneychange();
        //fee
        getFeeamont();
        //InputMaxTextNum
        setEditTextComments();
        payAddressMore();

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
                String strFeeamont = strFee.substring(0, strFee.indexOf("sat/byte"));
                String strMax = strFeeamont.replaceAll(" ", "");
                tetMoneye.setText(strFeeamont);
                intmaxFee = Integer.parseInt(strMax);
                seekBar.setMax(intmaxFee);
                seekBar.setProgress(intmaxFee);
            }
            seekbarLatoutup();
        }
    }

    private void seekbarLatoutup() {
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tvIndicator.getLayoutParams();
        seekBar.setOnSeekBarChangeListener(new IndicatorSeekBar.OnIndicatorSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, float indicatorOffset) {
                String indicatorText = String.valueOf(progress);
                catorText = Integer.parseInt(indicatorText);// use get fee
                //changed fee
                intmaxFee = catorText;
//                tvIndicator.setText(indicatorText);
//                tetMoneye.setText(indicatorText);
//                params.leftMargin = (int) indicatorOffset;
//                tvIndicator.setLayoutParams(params);
                straddress = editAddress.getText().toString();
                strAmount = tetamount.getText().toString();
                if (!TextUtils.isEmpty(straddress) && !TextUtils.isEmpty(strAmount)) {
                    //TODO:

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

    private void tetMoneychange() {
        tetMoneye.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > 7) {
                        s = s.toString().subSequence(0,
                                s.toString().indexOf(".") + 8);
                        tetMoneye.setText(s);
                        tetMoneye.setSelection(s.length());
                    }
                }
                if (s.toString().trim().substring(0).equals(".")) {
                    s = "0" + s;
                    tetMoneye.setText(s);
                    tetMoneye.setSelection(2);
                }
                if (s.toString().startsWith("0")
                        && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        tetMoneye.setText(s.subSequence(0, 1));
                        tetMoneye.setSelection(1);
                        return;
                    }
                }

            }

            @Override
            public void afterTextChanged(Editable s) {
                String strMoney = tetMoneye.getText().toString();
                if (TextUtils.isEmpty(strMoney)) {
                    tetMoneye.setText("0");
                }
            }
        });
    }

    @Override
    public void initData() {


    }

    private void setEditTextComments() {
        editTextComments.addTextChangedListener(new TextWatcher() {
            CharSequence input;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                input = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                bytesCount.setText(String.format(Locale.CHINA, "%d/20", input.length()));
                if (input.length() > 19) {
                    Toast.makeText(SendOne2OneMainPageActivity.this, R.string.moreinput_text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void showPopupSelectFee() {
        //Miner money
        showSelectFeeDialogs(SendOne2OneMainPageActivity.this, R.layout.select_fee_popwindow);

    }

    private void showSelectFeeDialogs(Context context, @LayoutRes int resource) {
        //set see view
        View view = View.inflate(context, resource, null);
        dialogBtom = new Dialog(context, R.style.dialog);
        //Here you can set properties for each control as required
        AppCompatSeekBar seekBar = view.findViewById(R.id.seek_bar_fee);
        TextView textViewFee = view.findViewById(R.id.fee);
        //SelectFee
        seekBar.setOnSeekBarChangeListener(new AppCompatSeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                pro = (double) progress / 10000;
                strContent = String.valueOf(pro);
                textViewFee.setText(strContent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
//                Toast.makeText(SendOne2OneMainPageActivity.this, "触碰SeekBar", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                Toast.makeText(SendOne2OneMainPageActivity.this, "放开SeekBar", Toast.LENGTH_SHORT).show();

            }
        });
        //cancel dialog
        view.findViewById(R.id.cancel_select_fee).setOnClickListener(v -> {
            dialogBtom.cancel();
        });
        view.findViewById(R.id.bn_fee).setOnClickListener(v -> {
            tetMoneye.setText(String.valueOf(pro));
            dialogBtom.cancel();
        });

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

    private void showPopupSelectWallet() {
        //check address
        showDialogs(SendOne2OneMainPageActivity.this, R.layout.select_send_wallet_popwindow);

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
            wallet_name = chooseName;
            try {
                Daemon.commands.callAttr("load_wallet", wallet_name);
                select_wallet = Daemon.commands.callAttr("select_wallet", wallet_name);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (select_wallet != null) {
                String toString = select_wallet.toString();
                Gson gson = new Gson();
                MainNewWalletBean mainWheelBean = gson.fromJson(toString, MainNewWalletBean.class);
                wallet_amount = mainWheelBean.getBalance();
            }

            tetWalletname.setText(wallet_name);
            dialogBtom.cancel();
        });
        recyPayaddress = view.findViewById(R.id.recy_payAdress);

//        recyPayaddress.setLayoutManager(new LinearLayoutManager(SendOne2OneMainPageActivity.this));
        choosePayAddressAdapetr = new ChoosePayAddressAdapetr(SendOne2OneMainPageActivity.this, dataListName);
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

    //getMorepayAddress
    private void payAddressMore() {
        try {
            get_wallets_list = Daemon.commands.callAttr("list_wallets");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (get_wallets_list != null) {
            List<PyObject> pyObjects = get_wallets_list.asList();
            for (int i = 0; i < pyObjects.size(); i++) {
                String walletName = pyObjects.get(i).toString();
                AddressEvent addressEvent = new AddressEvent();
                addressEvent.setName(walletName);
                dataListName.add(addressEvent);
            }
        }

    }

    private void recyclerviewOnclick() {
        choosePayAddressAdapetr.setmOnItemClickListener(new ChoosePayAddressAdapetr.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                chooseName = dataListName.get(position).getName();
                chooseAmount = dataListName.get(position).getAmount();

            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llt_select_wallet:
                //check address
                showPopupSelectWallet();
                break;
            case R.id.fee_select:
                //Miner money
                showPopupSelectFee();
                break;
            case R.id.tv_send2many:
                Intent intent = new Intent(this, Send2ManyActivity.class);
                intent.putExtra("wallet_name", wallet_name);
                startActivity(intent);
                break;
            case R.id.create_trans_one2one:
                straddress = editAddress.getText().toString();
                strAmount = tetamount.getText().toString();

                if (TextUtils.isEmpty(straddress)) {
                    Toast.makeText(this, R.string.input_address, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(strAmount)) {
                    Toast.makeText(this, R.string.inoutnum, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (walletmoney == 1) {
                    mToast(getResources().getString(R.string.wallet_insufficient));
                    return;
                }
                //creatTrnsaction
                mCreatTransaction();
                break;
            case R.id.bn_sweep_one2noe:
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
            case R.id.bn_paste_one2one:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData data = clipboard.getPrimaryClip();
                    if (data != null && data.getItemCount() > 0) {
                        editAddress.setText(data.getItemAt(0).getText());
                    }
                }
                break;
            case R.id.img_back:
                finish();
                break;
        }
    }

    //creat transaction
    private void mCreatTransaction() {
        straddress = editAddress.getText().toString();
        strAmount = tetamount.getText().toString();
        ArrayList<Map<String, String>> arrayList = new ArrayList<>();
        Map<String, String> pramas = new HashMap<>();
        pramas.put(straddress, strAmount);
        arrayList.add(pramas);
        String strMinerFee = tetMoneye.getText().toString();
        String strComment = editTextComments.getText().toString();
        String strPramas = new Gson().toJson(arrayList);

        Log.i("CreatTransaction", "strPramas: " + strPramas);

        try {
            mktx = Daemon.commands.callAttr("mktx", strPramas, strComment);

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("CreatTransaction", "mCrea-----  " + e.getMessage());
            if (e.getMessage().contains("Insufficient funds")) {
//                mToast(getResources().getString(R.string.insufficient));
                mToast(getResources().getString(R.string.fee_toohigh));
            } else if (e.getMessage().contains("invalid bitcoin address")) {
                mToast(getResources().getString(R.string.changeaddress));
            }

            return;
        }
        Log.i("CreatTransaction", "mCreatTransaction: " + mktx);
        if (mktx != null) {
            String jsonObj = mktx.toString();
            gson = new Gson();
            GetAddressBean getAddressBean = gson.fromJson(jsonObj, GetAddressBean.class);
            String beanTx = getAddressBean.getTx();
            if (beanTx != null) {
                EventBus.getDefault().post(new FirstEvent("22"));
                Intent intent = new Intent(SendOne2OneMainPageActivity.this, TransactionDetailsActivity.class);
                intent.putExtra("tx_hash", beanTx);
                intent.putExtra("keyValue", "A");
                intent.putExtra("txCreatTrsaction", beanTx);
                startActivity(intent);
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Scan QR code / barcode return
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                Log.i("content", "on------: " + content);
                if (!TextUtils.isEmpty(content)) {
                    if (content.contains("bitcoin:")) {
                        String replace = content.replaceAll("bitcoin:", "");
                        editAddress.setText(replace);
                    } else {
                        editAddress.setText(content);
                    }
                }
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().contains(".")) {
            if (s.length() - 1 - s.toString().indexOf(".") > 7) {
                s = s.toString().subSequence(0,
                        s.toString().indexOf(".") + 8);
                tetamount.setText(s);
                tetamount.setSelection(s.length());
            }
        }
        if (s.toString().trim().substring(0).equals(".")) {
            s = "0" + s;
            tetamount.setText(s);
            tetamount.setSelection(2);
        }
        if (s.toString().startsWith("0")
                && s.toString().trim().length() > 1) {
            if (!s.toString().substring(1, 2).equals(".")) {
                tetamount.setText(s.subSequence(0, 1));
                tetamount.setSelection(1);
                return;
            }
        }

    }

    @Override
    public void afterTextChanged(Editable s) {
        strAmount = tetamount.getText().toString();
        if (!TextUtils.isEmpty(strAmount)) {
            BigDecimal bigmBTC = new BigDecimal(strAmount);
            try {
                pyObject = Daemon.commands.callAttr("get_exchange_currency", "base", bigmBTC);

            } catch (Exception e) {
                e.printStackTrace();
                mToast(e.getMessage());
            }
            if (pyObject != null) {
                editChangeMoney.setText(pyObject.toString());
            }
            //compare
            BigDecimal bignum1 = new BigDecimal(strAmount);
            Log.i("wallet_amount", "afterTex+++++ " + wallet_amount);
            if (!TextUtils.isEmpty(wallet_amount)) {
                String strBtc = wallet_amount.substring(0, wallet_amount.indexOf(" mBTC"));
                BigDecimal bignum2 = new BigDecimal(strBtc);
                int math = bignum1.compareTo(bignum2);
                //if math = 1 -> bignum2
                BigDecimal bigDecimal = new BigDecimal(21000000);
                int mathMax = bignum1.compareTo(bigDecimal);
                if (mathMax == 1) {
                    mToast(getResources().getString(R.string.sendMore));
                } else {
                    if (math == 1) {
                        mToast(getResources().getString(R.string.wallet_insufficient));
                        //walletmoney == 1  ->  Sorry, your credit is running low
                        walletmoney = 1;
                    } else if (math == -1 || math == 0) {
                        walletmoney = 0;
                    }
                }
            }

        } else {
            editChangeMoney.setText("");
        }
    }

}



