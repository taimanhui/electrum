package org.haobtc.wallet.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chaquo.python.PyObject;
import com.google.gson.Gson;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.transaction.DeatilMoreAddressActivity;
import org.haobtc.wallet.adapter.ChoosePayAddressAdapetr;
import org.haobtc.wallet.bean.AddressEvent;
import org.haobtc.wallet.bean.GetAddressBean;
import org.haobtc.wallet.bean.GetsendFeenumBean;
import org.haobtc.wallet.bean.MainWheelBean;
import org.haobtc.wallet.event.SendMoreAddressEvent;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.IndicatorSeekBar;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SendOne2ManyMainPageActivity extends BaseActivity {


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
    @BindView(R.id.edit_Remarks)
    EditText editRemarks;
    @BindView(R.id.tet_textNum)
    TextView tetTextNum;
    @BindView(R.id.tv_fee)
    EditText tvFee;
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.create_trans_one2many)
    Button createTransOne2many;
    @BindView(R.id.seed_Bar)
    IndicatorSeekBar seedBar;
    @BindView(R.id.tv_indicator)
    TextView tvIndicator;
    private ArrayList<AddressEvent> dataListName;
    private Dialog dialogBtom;
    private RecyclerView recyPayaddress;
    private ChoosePayAddressAdapetr choosePayAddressAdapetr;
    private String wallet_name;
    private double pro;
    private String strmapBtc;
    private PyObject mktx;
    private SharedPreferences.Editor edit;
    private List addressList;
    private int intmaxFee;
    private String waletType;

    public int getLayoutId() {
        return R.layout.send_one2many_main;
    }

    @SuppressLint("CommitPrefEdits")
    public void initView() {
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        edit = preferences.edit();
        Intent intent = getIntent();
        addressList = (List) getIntent().getSerializableExtra("listdetail");
        wallet_name = intent.getStringExtra("wallet_name");
        waletType = intent.getStringExtra("wallet_type");
        int addressNum = intent.getIntExtra("addressNum", 0);
        String totalAmount = intent.getStringExtra("totalAmount");

        //To many people Coin making
        strmapBtc = intent.getStringExtra("strmapBtc");
        walletName.setText(wallet_name);
        addressCount.setText(String.format("%s %s", String.valueOf(addressNum), getResources().getString(R.string.to_num)));
        tvAmount.setText(String.format("%s BTC", totalAmount));
        //InputMaxTextNum
        setEditTextComments();
        getFeerate();

    }

    @Override
    public void initData() {
        dataListName = new ArrayList<>();
        //getMorepayAddress
        payAddressMore();
        //fee
        getFeeamont();

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
                String strFeemontAs = strFee.substring(0, strFee.indexOf("sat/byte") + 8);
                String strFeeamont = strFee.substring(0, strFee.indexOf("sat/byte"));
                String strMax = strFeeamont.replaceAll(" ", "");
                tvFee.setText(strFeemontAs);
                intmaxFee = Integer.parseInt(strMax);
                seedBar.setMax(intmaxFee);
                seedBar.setProgress(intmaxFee);
            }
            seekbarLatoutup();
        }
    }

    private void seekbarLatoutup() {
        seedBar.setOnSeekBarChangeListener(new IndicatorSeekBar.OnIndicatorSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, float indicatorOffset) {
                String indicatorText = String.valueOf(progress);
                int catorText = Integer.parseInt(indicatorText);// use get fee
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

    private void setEditTextComments() {
        editRemarks.addTextChangedListener(new TextWatcher() {
            CharSequence input;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                input = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tetTextNum.setText(String.format(Locale.CHINA, "%d/20", input.length()));
                if (input.length() > 19) {
                    Toast.makeText(SendOne2ManyMainPageActivity.this, R.string.moreinput_text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
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


    @OnClick({R.id.lin_chooseAddress, R.id.linearLayout10, R.id.img_feeSelect, R.id.img_back, R.id.create_trans_one2many})
    public void onViewClicked(View view) {
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
                String strRemarks = editRemarks.getText().toString();
                String strFee = tvFee.getText().toString();
                try {
                    mktx = Daemon.commands.callAttr("mktx", strmapBtc, strRemarks);
                    Log.i("CreatTransaction", "m-------: " + mktx);
                } catch (Exception e) {
                    e.printStackTrace();
                    mToast(getResources().getString(R.string.changeaddress));
                    return;
                }
                if (mktx != null) {
                    String jsonObj = mktx.toString();
                    Gson gson = new Gson();
                    GetAddressBean getAddressBean = gson.fromJson(jsonObj, GetAddressBean.class);
                    String beanTx = getAddressBean.getTx();

                    if (beanTx != null) {
                        Intent intent = new Intent(SendOne2ManyMainPageActivity.this, TransactionDetailsActivity.class);
                        intent.putExtra("tx_hash", beanTx);
                        intent.putExtra("keyValue", "A");
                        intent.putExtra("strwalletType", waletType);
                        intent.putExtra("txCreatTrsaction", beanTx);
                        startActivity(intent);
                    }
                }

                break;
        }
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
                String strContent = String.valueOf(pro);
                textViewFee.setText(strContent);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //cancel dialog
        view.findViewById(R.id.cancel_select_fee).setOnClickListener(v -> {
            dialogBtom.cancel();
        });
        view.findViewById(R.id.bn_fee).setOnClickListener(v -> {
            tvFee.setText(String.valueOf(pro));
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
        });
        recyPayaddress = view.findViewById(R.id.recy_payAdress);
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
        ArrayList<Map<String, String>> arrayList = new ArrayList<>();
        Map<String, String> pramas = new HashMap<>();
        if (addressList!=null){
            for (int i = 0; i < addressList.size(); i++) {
                String straddress = ((SendMoreAddressEvent) (addressList.get(0))).getInputAddress();
                String strAmount = ((SendMoreAddressEvent) (addressList.get(0))).getInputAmount();
                pramas.put(straddress, strAmount);
                arrayList.add(pramas);
            }

            String strPramas = new Gson().toJson(arrayList);
            PyObject get_fee_by_feerate = null;
            try {
                get_fee_by_feerate = Daemon.commands.callAttr("get_fee_by_feerate", strPramas, "", intmaxFee);
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
    }

}
