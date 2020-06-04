package org.haobtc.wallet.activities.personalwallet;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chaquo.python.PyObject;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.settings.BixinKEYManageActivity;
import org.haobtc.wallet.activities.settings.FixBixinkeyNameActivity;
import org.haobtc.wallet.activities.settings.HardwareDetailsActivity;
import org.haobtc.wallet.adapter.PublicPersonAdapter;
import org.haobtc.wallet.adapter.WalletAddressAdapter;
import org.haobtc.wallet.adapter.WalletDetailKeyAdapter;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.bean.HardwareFeatures;
import org.haobtc.wallet.event.AddBixinKeyEvent;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.event.WalletAddressEvent;
import org.haobtc.wallet.event.WalletDetailBixinKeyEvent;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.MyDialog;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class WalletDetailsActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_Name)
    TextView tetName;
    @BindView(R.id.tet_deleteWallet)
    Button tetDeleteWallet;
    @BindView(R.id.recl_wallet_addr)
    RecyclerView reclWalletAddr;
    @BindView(R.id.recl_publicPerson)
    RecyclerView reclPublicPerson;
    @BindView(R.id.text_fix_name)
    TextView textFixName;
    @BindView(R.id.test_no_key)
    TextView testNoKey;
    @BindView(R.id.card_three_public)
    CardView cardThreePublic;
    @BindView(R.id.test_no_address)
    TextView testNoAddress;
    private String wallet_name;
    private Dialog dialogBtom;
    private MyDialog myDialog;
    private ArrayList<WalletDetailBixinKeyEvent> addEventsDatas;
    private ArrayList<WalletAddressEvent> walletAddressList;
    private List<HardwareFeatures> deviceValue;

    @Override
    public int getLayoutId() {
        return R.layout.activity_check_wallet_detail;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        myDialog = MyDialog.showDialog(WalletDetailsActivity.this);
        Intent intent = getIntent();
        wallet_name = intent.getStringExtra("wallet_name");
        String wallet_type = intent.getStringExtra("wallet_type");
        if ("standard".equals(wallet_type)) {
            cardThreePublic.setVisibility(View.GONE);
        } else {
            addEventsDatas = new ArrayList<>();
            getBixinKeyList();
        }
        tetName.setText(wallet_name);

    }

    @Override
    public void initData() {
        walletAddressList = new ArrayList<>();
        getAllFundedAddress();
    }

    private void getBixinKeyList() {
        deviceValue = new ArrayList<>();
        SharedPreferences devices = getSharedPreferences("devices", MODE_PRIVATE);
        Map<String, ?> devicesAll = devices.getAll();
        //key
        for (Map.Entry<String, ?> entry : devicesAll.entrySet()) {
            String mapValue = (String) entry.getValue();
            HardwareFeatures hardwareFeatures = new Gson().fromJson(mapValue, HardwareFeatures.class);
            deviceValue.add(hardwareFeatures);
        }
        if (deviceValue != null) {
            try {
                PyObject get_device_info = Daemon.commands.callAttr("get_device_info");
                Log.i("TAGget_device_info", "getBixinKeyList:==== " + get_device_info);
                String str_deviceId = get_device_info.toString();
                if (!TextUtils.isEmpty(str_deviceId)) {
                    for (HardwareFeatures entity : deviceValue) {
                        if (str_deviceId.contains(entity.getDeviceId())) {
                            WalletDetailBixinKeyEvent addBixinKeyEvent = new WalletDetailBixinKeyEvent();
                            if (!TextUtils.isEmpty(entity.getLabel())) {
                                addBixinKeyEvent.setLabel(entity.getLabel());
                            } else {
                                addBixinKeyEvent.setLabel("BixinKey");
                            }
                            addBixinKeyEvent.setBleName(entity.getBleName());
                            addBixinKeyEvent.setDeviceId(entity.getDeviceId());
                            addBixinKeyEvent.setMajorVersion(entity.getMajorVersion());
                            addBixinKeyEvent.setMinorVersion(entity.getMinorVersion());
                            addBixinKeyEvent.setPatchVersion(entity.getPatchVersion());
                            addEventsDatas.add(addBixinKeyEvent);
                        }
                    }
                    WalletDetailKeyAdapter publicPersonAdapter = new WalletDetailKeyAdapter(addEventsDatas);
                    reclPublicPerson.setAdapter(publicPersonAdapter);
                    publicPersonAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                            String firmwareVersion = "V" + addEventsDatas.get(position).getMajorVersion() + "." + addEventsDatas.get(position).getMinorVersion() + "." + addEventsDatas.get(position).getPatchVersion();
                            Intent intent = new Intent(WalletDetailsActivity.this, HardwareDetailsActivity.class);
                            intent.putExtra("label", addEventsDatas.get(position).getLabel());
                            intent.putExtra("bleName", addEventsDatas.get(position).getBleName());
                            intent.putExtra("firmwareVersion", firmwareVersion);
                            intent.putExtra("bleVerson", "V" + addEventsDatas.get(position).getMajorVersion() + "." + addEventsDatas.get(position).getPatchVersion());
                            intent.putExtra("device_id", addEventsDatas.get(position).getDeviceId());
                            startActivity(intent);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                reclPublicPerson.setVisibility(View.GONE);
                testNoKey.setVisibility(View.VISIBLE);
            }
        } else {
            reclPublicPerson.setVisibility(View.GONE);
            testNoKey.setVisibility(View.VISIBLE);
        }
    }

    private void getAllFundedAddress() {
        PyObject get_all_funded_address = null;
        try {
            get_all_funded_address = Daemon.commands.callAttr("get_all_funded_address");
            JSONArray jsonArray = new JSONArray(get_all_funded_address.toString());
            if (jsonArray.length() != 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    WalletAddressEvent walletAddressEvent = new WalletAddressEvent();
                    walletAddressEvent.setAddress(jsonObject.getString("address"));
                    walletAddressEvent.setBalance(jsonObject.getString("balance"));
                    walletAddressList.add(walletAddressEvent);
                }
                WalletAddressAdapter walletAddressAdapter = new WalletAddressAdapter(walletAddressList);
                reclWalletAddr.setAdapter(walletAddressAdapter);
            } else {
                reclWalletAddr.setVisibility(View.GONE);
                testNoAddress.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            reclWalletAddr.setVisibility(View.GONE);
            testNoAddress.setVisibility(View.VISIBLE);
        }
    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.tet_deleteWallet, R.id.text_fix_name})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_deleteWallet:
                //delete wallet dialog
                showDialogs(WalletDetailsActivity.this, R.layout.delete_wallet);
                break;
            case R.id.text_fix_name:
                mIntent(FixBixinkeyNameActivity.class);
                break;
        }
    }

    private void showDialogs(Context context, @LayoutRes int resource) {
        //set see view
        View view = View.inflate(context, resource, null);
        dialogBtom = new Dialog(context, R.style.dialog);
        //cancel dialog
        view.findViewById(R.id.tet_ConfirmDelete).setOnClickListener(v -> {
            myDialog.show();
            Log.i("wallet_name", "showDialogs: " + wallet_name);
            try {
                Daemon.commands.callAttr("delete_wallet", wallet_name);
                EventBus.getDefault().post(new FirstEvent("11"));
                mToast(getString(R.string.delete_succse));
                finish();
            } catch (Exception e) {
                Log.i("delete_wallet", "===========: " + e.getMessage());
                e.printStackTrace();
            }
            myDialog.dismiss();
            dialogBtom.cancel();
        });
        //cancel dialog
        view.findViewById(R.id.tet_cancle).setOnClickListener(v -> {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
