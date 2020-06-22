package org.haobtc.wallet.activities;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chaquo.python.PyObject;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.MaindowndatalistAdapetr;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.bean.MaintrsactionlistEvent;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.utils.Daemon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TransactionRecordsActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener, OnRefreshListener {
    @BindView(R.id.recy_jylist)
    RecyclerView recyJylist;
    @BindView(R.id.tet_None)
    TextView tetNone;
    @BindView(R.id.img_backTrsa)
    ImageView imgBackTrsa;
    @BindView(R.id.radio_one)
    RadioButton radioOne;
    @BindView(R.id.radio_two)
    RadioButton radioTwo;
    @BindView(R.id.radio_group)
    RadioGroup radioGroup;
    @BindView(R.id.smart_RefreshLayout)
    SmartRefreshLayout refreshLayout;
    private ArrayList<MaintrsactionlistEvent> maintrsactionlistEvents;
    private String strChoose = "send";
    private String date;
    private boolean is_mine;
    private MaindowndatalistAdapetr trsactionlistAdapter;
    private String strwalletType;


    public int getLayoutId() {
        return R.layout.transaction_records;
    }

    public void initView() {
        ButterKnife.bind(this);
        Intent intent = getIntent();
        strwalletType = intent.getStringExtra("strwalletType");
        radioGroup.setOnCheckedChangeListener(this);
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setEnableRefresh(true);
        refreshLayout.setOnRefreshListener(this);
    }

    public void initData() {
        maintrsactionlistEvents = new ArrayList<>();
        //Binder Adapter
        trsactionlistAdapter = new MaindowndatalistAdapetr(maintrsactionlistEvents);
        recyJylist.setAdapter(trsactionlistAdapter);
        mTransactionrecordSend(strChoose);

    }

    private void mTransactionrecordSend(String sends) {
        //get transaction json
        PyObject get_history_tx = null;
        try {
            get_history_tx = Daemon.commands.callAttr("get_all_tx_list", sends);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (get_history_tx != null) {
            getHistroyIntry(get_history_tx);
        }

    }

    private void getHistroyIntry(PyObject get_history_tx) {
        //get transaction list
        if (!get_history_tx.isEmpty()) {
            String strHistory = get_history_tx.toString();
           // Log.i("strHistory", "onPage----: " + strHistory);
            if (TextUtils.isEmpty(strHistory) || strHistory.length() == 2) {
                tetNone.setVisibility(View.VISIBLE);
                recyJylist.setVisibility(View.GONE);
                refreshLayout.finishRefresh();
            } else {
                tetNone.setVisibility(View.GONE);
                recyJylist.setVisibility(View.VISIBLE);
                //show trsaction ist
                showTrsactionlist(strHistory);
            }

        } else {
            refreshLayout.finishRefresh();
            tetNone.setVisibility(View.VISIBLE);
            recyJylist.setVisibility(View.GONE);
        }

    }

    //show trsaction list
    private void showTrsactionlist(String strHistory) {
        refreshLayout.finishRefresh();
        try {
            JSONArray jsonArray = new JSONArray(strHistory);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                MaintrsactionlistEvent maintrsactionlistEvent = new MaintrsactionlistEvent();
                String type = jsonObject.getString("type");
                //    private String strChoosestate;
                String tx_hash = jsonObject.getString("tx_hash");
                String amount = jsonObject.getString("amount");
                is_mine = jsonObject.getBoolean("is_mine");//false ->get   true ->push
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
                    maintrsactionlistEvent.setTx_status(tx_status);
                    maintrsactionlistEvent.setType(type);
                    maintrsactionlistEvents.add(maintrsactionlistEvent);
                } else {

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
                trsactionlistAdapter.notifyDataSetChanged();
                trsactionlistAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                    private boolean status;
                    private String tx_hash1;
                    @Override
                    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                        String typeDele = maintrsactionlistEvents.get(position).getType();
                        switch (view.getId()) {
                            case R.id.lin_Item:
                                try {
                                    JSONObject jsonObject = jsonArray.getJSONObject(position);
                                    String tx_hash1 = jsonObject.getString("tx_hash");
                                    Intent intent = new Intent(TransactionRecordsActivity.this, TransactionDetailsActivity.class);
                                    if (typeDele.equals("tx")) {
                                        String tx_Onclick = jsonObject.getString("tx");
                                        intent.putExtra("keyValue", "B");
                                        intent.putExtra("listType", typeDele);
                                        intent.putExtra("tx_hash", tx_hash1);
                                        intent.putExtra("strwalletType", strwalletType);
                                        intent.putExtra("isIsmine", is_mine);
                                        intent.putExtra("dataTime", date);
                                        intent.putExtra("txCreatTrsaction", tx_Onclick);
                                        startActivity(intent);

                                    } else {
                                        intent.putExtra("tx_hash", tx_hash1);
                                        intent.putExtra("isIsmine", is_mine);
                                        intent.putExtra("strwalletType", strwalletType);
                                        intent.putExtra("dataTime", date);
                                        intent.putExtra("keyValue", "B");
                                        intent.putExtra("listType", typeDele);
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
                                    try {
                                        Daemon.commands.callAttr("remove_local_tx", tx_hash1);
                                        maintrsactionlistEvents.remove(position);
                                        trsactionlistAdapter.notifyItemChanged(position);
                                        mTransactionrecordSend(strChoose);
                                        EventBus.getDefault().post(new FirstEvent("22"));

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
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @SingleClick
    @OnClick({R.id.img_backTrsa})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.img_backTrsa) {
            finish();
        }
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radio_one:
                strChoose = "send";
                maintrsactionlistEvents.clear();
                mTransactionrecordSend(strChoose);
                if (trsactionlistAdapter != null) {
                    trsactionlistAdapter.notifyDataSetChanged();
                }

                break;
            case R.id.radio_two:
                strChoose = "receive";
                maintrsactionlistEvents.clear();
                mTransactionrecordSend(strChoose);
                if (trsactionlistAdapter != null) {
                    trsactionlistAdapter.notifyDataSetChanged();
                }
                break;
        }
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        maintrsactionlistEvents.clear();
        //trsaction list data
        mTransactionrecordSend(strChoose);
        if (trsactionlistAdapter != null) {
            trsactionlistAdapter.notifyDataSetChanged();
        }
    }
}
