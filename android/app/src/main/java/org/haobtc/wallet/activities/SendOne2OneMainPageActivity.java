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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
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

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import org.haobtc.wallet.bean.GetsendFeenumBean;
import org.haobtc.wallet.bean.MainNewWalletBean;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.IndicatorSeekBar;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
    @BindView(R.id.tet_Table)
    TextView tetTable;
    @BindView(R.id.tet_WalletTable)
    TextView tetWalletTable;
    @BindView(R.id.tet_strunit)
    TextView tetStrunit;
    @BindView(R.id.btnRecommendFee)
    Button btnRecommendFee;
    @BindView(R.id.testNowCanUse)
    TextView testNowCanUse;
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
    private List<AddressEvent> dataListName;
    private ChoosePayAddressAdapetr choosePayAddressAdapetr;
    private String straddress;
    private String strAmount;
    private TextView tetWalletname;
    private String wallet_name;
    private TextView textView;
    private double pro;
    private String wallet_amount;
    private PyObject get_wallets_list;
    private int walletmoney = 0;
    private int catorText;
    private PyObject pyObject;
    private PyObject select_wallet;
    private int intmaxFee;
    private String strComment = "";
    private String waletType;
    private ViewTreeObserver.OnGlobalLayoutListener mLayoutChangeListener;
    private boolean mIsSoftKeyboardShowing;
    private int screenHeight;
    private String base_unit;
    private String strUnit;
    private String strFeemontAs;

    @Override
    public int getLayoutId() {
        return R.layout.send_one2one_main_page;
    }

    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        base_unit = preferences.getString("base_unit", "mBTC");
        strUnit = preferences.getString("cny_strunit", "CNY");
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
        ImageView imgBack = findViewById(R.id.img_back);
        Button btnRecommend = findViewById(R.id.btnRecommendFee);
        btnRecommend.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        tetamount.addTextChangedListener(this);
        init();

    }

    //edittext focus change
    private void focusChange() {
        registerKeyBoard();

        editAddress.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //getFeerate
                    getFeerate();
                    //button to gray or blue
                    changeButton();
                }
            }
        });
        tetamount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    //getFeerate
                    getFeerate();
                    //button to gray or blue
                    changeButton();
                }
            }
        });

    }

    private void registerKeyBoard() {
        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        screenHeight = metric.heightPixels;
        mIsSoftKeyboardShowing = false;
        mLayoutChangeListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //Determine the size of window visible area
                Rect r = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
                //If the difference between screen height and window visible area height is greater than 1 / 3 of the whole screen height, it means that the soft keyboard is in display, otherwise, the soft keyboard is hidden.
                int heightDifference = screenHeight - (r.bottom - r.top);
                boolean isKeyboardShowing = heightDifference > screenHeight / 3;

                //If the status of the soft keyboard was previously displayed, it is now closed, or it was previously closed, it is now displayed, it means that the status of the soft keyboard has changed
                if ((mIsSoftKeyboardShowing && !isKeyboardShowing) || (!mIsSoftKeyboardShowing && isKeyboardShowing)) {
                    mIsSoftKeyboardShowing = isKeyboardShowing;
                    if (!mIsSoftKeyboardShowing) {
                        //getFeerate
                        getFeerate();
                        //button to gray or blue
                        changeButton();
                    }
                }
            }
        };
        //Register layout change monitoring
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(mLayoutChangeListener);
    }

    //button to gray or blue
    private void changeButton() {
        if (!TextUtils.isEmpty(editAddress.getText().toString()) && !TextUtils.isEmpty(tetamount.getText().toString())) {
            buttonCreate.setEnabled(true);
            buttonCreate.setBackground(getDrawable(R.drawable.button_bk));
        } else {
            buttonCreate.setEnabled(false);
            buttonCreate.setBackground(getDrawable(R.drawable.button_bk_grey));
        }
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
        tetTable.setText(base_unit);
        tetWalletTable.setText(base_unit);
        tetStrunit.setText(strUnit);
        Intent intent = getIntent();
        wallet_name = intent.getStringExtra("wallet_name");
        wallet_amount = intent.getStringExtra("wallet_balance");
        waletType = intent.getStringExtra("wallet_type");
        String sendAdress = intent.getStringExtra("sendAdress");
        String sendmessage = intent.getStringExtra("sendmessage");
        String strNowBtc = intent.getStringExtra("strNowBtc");
        String strNowCny = intent.getStringExtra("strNowCny");
        if (!TextUtils.isEmpty(strNowCny)){
            if (strNowCny.contains("≈")){
                testNowCanUse.setText(String.format("%s%s%s", getString(R.string.usable), strNowBtc, strNowCny));
            }else{
                testNowCanUse.setText(String.format("%s%s≈ %s", getString(R.string.usable), strNowBtc, strNowCny));
            }
        }

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
        //edittext focus change
        focusChange();

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
//                tetMoneye.setText(strFeemontAs);
                intmaxFee = Integer.parseInt(strMax);//fee
                seekBar.setMax(intmaxFee);
                seekBar.setProgress(intmaxFee);
            }
            seekbarLatoutup();
        }
    }

    private void seekbarLatoutup() {
        seekBar.setOnSeekBarChangeListener(new IndicatorSeekBar.OnIndicatorSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, float indicatorOffset) {
                String indicatorText = String.valueOf(progress);
                catorText = Integer.parseInt(indicatorText);// use get fee
                //changed fee
                intmaxFee = catorText;

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //getFeerate
                getFeerate();
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
        RecyclerView recyPayaddress = view.findViewById(R.id.recy_payAdress);

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
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    get_wallets_list = Daemon.commands.callAttr("list_wallets");
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                if (get_wallets_list != null) {
                    String toString = get_wallets_list.toString();
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

            }
        }, 200);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llt_select_wallet:
                //check address
                showPopupSelectWallet();
                break;
            case R.id.fee_select:
                //Miner money
//                showPopupSelectFee();
                break;
            case R.id.tv_send2many:
                Intent intent = new Intent(this, Send2ManyActivity.class);
                intent.putExtra("wallet_name", wallet_name);
                intent.putExtra("wallet_type", waletType);
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
                    mToast(getString(R.string.wallet_insufficient));
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
                        //getFeerate
                        getFeerate();
                        //button to gray or blue
                        changeButton();
                    }
                }
                break;
            case R.id.img_back:
                finish();
                break;
            case R.id.btnRecommendFee:
                mToast(strFeemontAs);
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
        strComment = editTextComments.getText().toString();
        String strPramas = new Gson().toJson(arrayList);

        Log.i("CreatTransaction", "strPramas: " + strPramas);

        PyObject mktx;
        try {
            mktx = Daemon.commands.callAttr("mktx", strPramas, strComment);

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("CreatTransaction", "mCrea-----  " + e.getMessage());
            if (e.getMessage().contains("Insufficient funds")) {
//                mToast(getString(R.string.insufficient));
                mToast(getString(R.string.fee_toohigh));
            } else if (e.getMessage().contains("invalid bitcoin address")) {
                mToast(getString(R.string.changeaddress));
            }

            return;
        }
        Log.i("CreatTransaction", "mCreatTransaction: " + mktx);
        if (mktx != null) {
            String jsonObj = mktx.toString();
            Gson gson = new Gson();
            GetAddressBean getAddressBean = gson.fromJson(jsonObj, GetAddressBean.class);
            String beanTx = getAddressBean.getTx();
            if (beanTx != null) {
                EventBus.getDefault().post(new FirstEvent("22"));
                Intent intent = new Intent(SendOne2OneMainPageActivity.this, TransactionDetailsActivity.class);
                intent.putExtra("tx_hash", beanTx);
                intent.putExtra("keyValue", "A");
                intent.putExtra("isIsmine", true);
                intent.putExtra("strwalletType", waletType);
                intent.putExtra("txCreatTrsaction", beanTx);
                startActivity(intent);
            }
        }

    }

    @SuppressLint("ObsoleteSdkInt")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().getViewTreeObserver().removeOnGlobalLayoutListener(mLayoutChangeListener);
        } else {
            getWindow().getDecorView().getViewTreeObserver().removeGlobalOnLayoutListener(mLayoutChangeListener);
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
                    //getFeerate
                    getFeerate();
                    //button to gray or blue
                    changeButton();

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

                Log.i("pyObjectcommands", "---------: " + pyObject);
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
                String strBtc = "";
                if (wallet_amount.contains("mBTC")) {
                    strBtc = wallet_amount.substring(0, wallet_amount.indexOf(" mBTC"));
                } else if (wallet_amount.contains("sat")) {
                    strBtc = wallet_amount.substring(0, wallet_amount.indexOf(" sat"));
                } else if (wallet_amount.contains("bits")) {
                    strBtc = wallet_amount.substring(0, wallet_amount.indexOf(" bits"));
                } else {
                    strBtc = wallet_amount.substring(0, wallet_amount.indexOf(" BTC"));
                }

                BigDecimal bignum2 = new BigDecimal(strBtc);
                int math = bignum1.compareTo(bignum2);
                //if math = 1 -> bignum2
                BigDecimal bigDecimal = new BigDecimal(21000000);
                int mathMax = bignum1.compareTo(bigDecimal);
                if (mathMax > 0) {
                    mToast(getString(R.string.sendMore));
                } else {
                    if (math > 0) {
                        mToast(getString(R.string.wallet_insufficient));
                        //walletmoney == 1  ->  Sorry, your credit is running low
                        walletmoney = 1;
                    } else {
                        walletmoney = 0;
                    }
                }
            }

        } else {
            editChangeMoney.setText("");
        }
    }

    //get fee num
    private void getFeerate() {
        straddress = editAddress.getText().toString();
        strAmount = tetamount.getText().toString();
        strComment = editTextComments.getText().toString();
        if (!TextUtils.isEmpty(straddress) && !TextUtils.isEmpty(strAmount)) {
            ArrayList<Map<String, String>> arrayList = new ArrayList<>();
            Map<String, String> pramas = new HashMap<>();
            pramas.put(straddress, strAmount);
            arrayList.add(pramas);
            String strPramas = new Gson().toJson(arrayList);
            PyObject get_fee_by_feerate = null;
            try {
                get_fee_by_feerate = Daemon.commands.callAttr("get_fee_by_feerate", strPramas, strComment, intmaxFee);
            } catch (Exception e) {
                e.printStackTrace();
                if (e.getMessage().contains("invalid bitcoin address")) {
                    Toast.makeText(this, getString(R.string.changeaddress), Toast.LENGTH_LONG).show();
                }
                return;
            }
            Log.i("get_fee_by_feerate", "getFeerate: " + get_fee_by_feerate);
            if (get_fee_by_feerate != null) {
                String strnewFee = get_fee_by_feerate.toString();
                Gson gson = new Gson();
                GetsendFeenumBean getsendFeenumBean = gson.fromJson(strnewFee, GetsendFeenumBean.class);
                int fee = getsendFeenumBean.getFee();

                tetMoneye.setText(String.format("%ssat", String.valueOf(fee)));

            }
        }
    }

}



