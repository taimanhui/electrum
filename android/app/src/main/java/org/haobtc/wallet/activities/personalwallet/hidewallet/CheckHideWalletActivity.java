package org.haobtc.wallet.activities.personalwallet.hidewallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chaquo.python.PyObject;
import com.google.gson.Gson;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.ReceivedPageActivity;
import org.haobtc.wallet.activities.SendOne2OneMainPageActivity;
import org.haobtc.wallet.activities.TransactionDetailsActivity;
import org.haobtc.wallet.activities.TransactionRecordsActivity;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.personalwallet.WalletDetailsActivity;
import org.haobtc.wallet.activities.sign.SignActivity;
import org.haobtc.wallet.adapter.MaindowndatalistAdapetr;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.bean.MainNewWalletBean;
import org.haobtc.wallet.bean.MaintrsactionlistEvent;
import org.haobtc.wallet.event.SecondEvent;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.MyDialog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CheckHideWalletActivity extends BaseActivity implements OnRefreshListener {

    @BindView(R.id.wallet_card_tv4)
    TextView walletCardTv4;
    @BindView(R.id.tet_fiat)
    TextView tetFiat;
    @BindView(R.id.tet_Cny)
    TextView tetCny;
    @BindView(R.id.recy_data)
    RecyclerView recy_data;
    @BindView(R.id.smart_RefreshLayout)
    SmartRefreshLayout refreshLayout;
    @BindView(R.id.tet_None)
    TextView tetNone;
    @BindView(R.id.wallet_card_tv3)
    TextView walletCard;
    private PyObject select_wallet;
    private MaindowndatalistAdapetr trsactionlistAdapter;
    private ArrayList<MaintrsactionlistEvent> maintrsactionlistEvents;
    private MyDialog myDialog;
    private JSONArray jsonArray;
    private boolean is_mine;
    private String date;
    private String substring;
    private String strCNY;
    private SharedPreferences.Editor edit;
    private SharedPreferences preferences;

    @Override
    public int getLayoutId() {
        return R.layout.activity_check_hide_wallet;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void initView() {
        ButterKnife.bind(this);
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        //Eventbus register
        EventBus.getDefault().register(this);
        myDialog = MyDialog.showDialog(CheckHideWalletActivity.this);
        Intent intent = getIntent();
        inits();
    }

    private void inits() {
        //get wallet unit
        String base_unit = preferences.getString("base_unit", "");
        walletCard.setText(String.format("%s（%s）", getString(R.string.balance), base_unit));
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setEnableRefresh(true);
        refreshLayout.setOnRefreshListener(this);
    }

    @Override
    public void initData() {
        maintrsactionlistEvents = new ArrayList<>();
        //Binder Adapter
        trsactionlistAdapter = new MaindowndatalistAdapetr(maintrsactionlistEvents);
        recy_data.setAdapter(trsactionlistAdapter);
        getWalletMsg();
        //trsaction list data
        downMainListdata();
    }

    //get wallet message
    public void getWalletMsg() {
        try {
            select_wallet = Daemon.commands.callAttr("select_wallet", "隐藏钱包");
        } catch (Exception e) {
            Log.i("select_wallet", "--------- " + e.getMessage());
            e.printStackTrace();
        }

        if (select_wallet != null) {
            String toString = select_wallet.toString();
            Log.i("CheckHideWalletActivity", "select_wallet+++: " + toString);
            Gson gson = new Gson();
            MainNewWalletBean mainWheelBean = gson.fromJson(toString, MainNewWalletBean.class);
            String balanceC = mainWheelBean.getBalance();
            if (!TextUtils.isEmpty(balanceC)) {
                if (balanceC.contains("(")) {
                    substring = balanceC.substring(0, balanceC.indexOf("("));
                    Log.e("substring", "substring: " + substring);
                    Log.e("substring", "balanceC: " + balanceC);
                    walletCardTv4.setText(substring);
                    strCNY = balanceC.substring(balanceC.indexOf("(") + 1, balanceC.indexOf(")"));
                    if (!TextUtils.isEmpty(strCNY)) {
                        tetCny.setText(String.format("≈ %s", strCNY));
                    }
                } else {
                    walletCardTv4.setText(balanceC);
                }
            }
        }
    }

    private void downMainListdata() {
        maintrsactionlistEvents.clear();
        trsactionlistAdapter.notifyDataSetChanged();
        PyObject get_history_tx = null;
        try {
            //get transaction json
            get_history_tx = Daemon.commands.callAttr("get_all_tx_list");
        } catch (Exception e) {
            e.printStackTrace();
            myDialog.dismiss();
            refreshLayout.finishRefresh();
            tetNone.setText(getString(R.string.no_records));
            tetNone.setVisibility(View.VISIBLE);
            recy_data.setVisibility(View.GONE);
            Log.i("downMainListdata", "downMaina===: " + e.getMessage());
            return;
        }
        //get transaction list
        if (get_history_tx != null) {
            tetNone.setVisibility(View.GONE);
            recy_data.setVisibility(View.VISIBLE);
            String strHistory = get_history_tx.toString();
            //Log.i("strHistory", "onPage----: " + strHistory);
            refreshLayout.finishRefresh();
            if (strHistory.length() == 2) {
                myDialog.dismiss();
                tetNone.setText(getString(R.string.no_records));
                tetNone.setVisibility(View.VISIBLE);
                recy_data.setVisibility(View.GONE);
            } else {
                //show trsaction list
                showTrsactionlist(strHistory);
            }

        } else {
            myDialog.dismiss();
            refreshLayout.finishRefresh();
            tetNone.setText(getString(R.string.no_records));
            tetNone.setVisibility(View.VISIBLE);
            recy_data.setVisibility(View.GONE);
        }

    }

    //show trsaction list
    private void showTrsactionlist(String strHistory) {
        maintrsactionlistEvents.clear();
        try {
            jsonArray = new JSONArray(strHistory);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                MaintrsactionlistEvent maintrsactionlistEvent = new MaintrsactionlistEvent();
                String type = jsonObject.getString("type");
                String tx_hash = jsonObject.getString("tx_hash");
                String amount = jsonObject.getString("amount");
                //false ->get   true ->push
                is_mine = jsonObject.getBoolean("is_mine");
                date = jsonObject.getString("date");
                String tx_status = jsonObject.getString("tx_status");
                if (type.equals("history")) {
                    String confirmations = jsonObject.getString("confirmations");
                    //add attribute
                    maintrsactionlistEvent.setTx_hash(tx_hash);
                    maintrsactionlistEvent.setDate(date);
                    maintrsactionlistEvent.setAmount(amount);
                    maintrsactionlistEvent.setIs_mine(is_mine);
                    maintrsactionlistEvent.setConfirmations(confirmations);
                    maintrsactionlistEvent.setType(type);
                    maintrsactionlistEvent.setTx_status(tx_status);
                    maintrsactionlistEvents.add(maintrsactionlistEvent);
                } else {

                    String txCreatTrsaction = jsonObject.getString("tx");
                    String invoice_id = jsonObject.getString("invoice_id");//delete use
                    //add attribute
                    maintrsactionlistEvent.setTx_hash(tx_hash);
                    maintrsactionlistEvent.setDate(date);
                    maintrsactionlistEvent.setAmount(amount);
                    maintrsactionlistEvent.setIs_mine(is_mine);
                    maintrsactionlistEvent.setType(type);
                    maintrsactionlistEvent.setTx_status(tx_status);
                    maintrsactionlistEvent.setInvoice_id(invoice_id);
                    maintrsactionlistEvents.add(maintrsactionlistEvent);
                }
            }
            myDialog.dismiss();
            trsactionlistAdapter.notifyDataSetChanged();
            trsactionlistAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                private String tx_hash1;
                private boolean status;

                @Override
                public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                    String typeDele = maintrsactionlistEvents.get(position).getType();
                    switch (view.getId()) {
                        case R.id.lin_Item:
                            try {
                                JSONObject jsonObject = jsonArray.getJSONObject(position);
                                tx_hash1 = jsonObject.getString("tx_hash");
                                is_mine = jsonObject.getBoolean("is_mine");
                                date = jsonObject.getString("date");
                                Intent intent = new Intent(CheckHideWalletActivity.this, TransactionDetailsActivity.class);
                                intent.putExtra("hideWallet", "hideWallet");
                                intent.putExtra("keyValue", "B");
                                intent.putExtra("dataTime", date);
                                intent.putExtra("isIsmine", is_mine);
                                intent.putExtra("tx_hash", tx_hash1);
                                intent.putExtra("strwalletType", "1-1");
                                intent.putExtra("listType", typeDele);
                                if ("tx".equals(typeDele)) {
                                    String tx_Onclick = jsonObject.getString("tx");
                                    intent.putExtra("txCreatTrsaction", tx_Onclick);
                                    startActivity(intent);
                                } else {
                                    startActivity(intent);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case R.id.txt_delete:
                            try {
                                JSONObject jsonObject = jsonArray.getJSONObject(position);
                                tx_hash1 = jsonObject.getString("tx_hash");
                                PyObject get_remove_flag = Daemon.commands.callAttr("get_remove_flag", tx_hash1);
                                status = get_remove_flag.toBoolean();
                                Log.i("onItemChildClick", "onItemCh==== " + status);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (status) {
//                                String invoice_id = maintrsactionlistEvents.get(position).getInvoice_id();
                                try {
                                    Daemon.commands.callAttr("remove_local_tx", tx_hash1);
                                    maintrsactionlistEvents.remove(position);
                                    trsactionlistAdapter.notifyItemChanged(position);
                                    trsactionlistAdapter.notifyDataSetChanged();
                                    downMainListdata();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                mToast(getString(R.string.delete_unBroad));
                            }

                            break;
                    }
                }
            });

        } catch (JSONException e) {
            Log.e("sndkjnskjn", "type++++: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.wallet_card_bn1, R.id.wallet_card_bn2, R.id.wallet_card_bn3, R.id.conlay_back, R.id.textView_more})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.wallet_card_bn1:
                edit.putString("wallet_type_to_sign", "1-1");
                edit.apply();
                Intent intent1 = new Intent(CheckHideWalletActivity.this, SendOne2OneMainPageActivity.class);
                intent1.putExtra("wallet_name", "隐藏钱包");
                intent1.putExtra("wallet_type", "1-1");
                intent1.putExtra("strNowBtc", walletCardTv4.getText().toString());
                intent1.putExtra("strNowCny", tetCny.getText().toString());
                intent1.putExtra("hideRefresh", "hideRefresh");
                startActivity(intent1);
                break;
            case R.id.wallet_card_bn2:
                Intent intent2 = new Intent(CheckHideWalletActivity.this, ReceivedPageActivity.class);
                startActivity(intent2);
                break;
            case R.id.wallet_card_bn3:
                Intent intent3 = new Intent(CheckHideWalletActivity.this, SignActivity.class);
                intent3.putExtra("hide_phrass", "hideWallet");
                intent3.putExtra("personceType", "1-1");
                startActivity(intent3);
                break;
            case R.id.conlay_back:
                Intent intent = new Intent(CheckHideWalletActivity.this, WalletDetailsActivity.class);
                intent.putExtra("wallet_name", "隐藏钱包");
                startActivity(intent);
                break;
            case R.id.textView_more:
                Intent intent4 = new Intent(this, TransactionRecordsActivity.class);
                startActivity(intent4);
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(SecondEvent updataHint) {
        String msgVote = updataHint.getMsg();
        Log.i("JXMmsgVote", "event: " + msgVote);
        if (!TextUtils.isEmpty(msgVote) || msgVote.length() != 2 && !"finish".equals(msgVote)) {
            //Rolling Wallet
            try {
                JSONObject jsonObject = new JSONObject(msgVote);
                if (msgVote.contains("balance")) {
                    String balance = jsonObject.getString("balance");
                    walletCardTv4.setText(balance);
                }
                if (msgVote.contains("fiat")) {
                    String fiat = jsonObject.getString("fiat");
                    tetCny.setText(fiat);
                }
                if (msgVote.contains("unconfirmed")) {
                    String unconfirmed = jsonObject.getString("unconfirmed");
                    tetFiat.setText(String.format("%s%s", unconfirmed, getString(R.string.unconfirm)));
                } else {
                    tetFiat.setText("");
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
            //trsaction list data
            downMainListdata();

        }
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        maintrsactionlistEvents.clear();
        //trsaction list data
        downMainListdata();
        if (trsactionlistAdapter != null) {
            trsactionlistAdapter.notifyDataSetChanged();
        }
    }

}
