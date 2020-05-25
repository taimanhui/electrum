package org.haobtc.wallet.activities.personalwallet;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.PyObject;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.settings.FixBixinkeyNameActivity;
import org.haobtc.wallet.adapter.WalletAddressAdapter;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.event.WalletAddressEvent;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.MyDialog;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

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
    private String wallet_name;
    private Dialog dialogBtom;
    private MyDialog myDialog;
    private ArrayList<WalletAddressEvent> walletAddressList;

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
        tetName.setText(wallet_name);

    }

    @Override
    public void initData() {
        walletAddressList = new ArrayList<>();
        getAllFundedAddress();
    }

    private void getAllFundedAddress() {
        PyObject get_all_funded_address = null;
        try {
            get_all_funded_address = Daemon.commands.callAttr("get_all_funded_address");
            Log.i("WalletDetailsActivity", "getAllFundedAddress: " + get_all_funded_address.toString());
            JSONArray jsonArray = new JSONArray(get_all_funded_address.toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                WalletAddressEvent walletAddressEvent = new WalletAddressEvent();
                walletAddressEvent.setAddress(jsonObject.getString("address"));
                walletAddressEvent.setBalance(jsonObject.getString("balance"));
                walletAddressList.add(walletAddressEvent);

            }
            WalletAddressAdapter walletAddressAdapter = new WalletAddressAdapter(walletAddressList);
            reclWalletAddr.setAdapter(walletAddressAdapter);

        } catch (Exception e) {
            e.printStackTrace();
            return;
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

}
