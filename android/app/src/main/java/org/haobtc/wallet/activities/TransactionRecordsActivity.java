package org.haobtc.wallet.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.MaindowndatalistAdapetr;
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

public class TransactionRecordsActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    @BindView(R.id.recy_jylist)
    RecyclerView recyJylist;
    //    @BindView(R.id.spi_BTC)
//    AppCompatSpinner spiBTC;
//    @BindView(R.id.spi_ZT)
//    AppCompatSpinner spiZT;
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
    private ArrayList<MaintrsactionlistEvent> maintrsactionlistEvents;
    private SharedPreferences preferences;
    //    private int haveSelectBTC = 0;
//    private int haveSelectState = 0;
    private String strChoose = "send";
    //    private String strChoosestate;
    private String tx_hash;
    private String date;
    private String amount;
    private boolean is_mine;
    private String confirmations;
    private String txCreatTrsaction;

    public int getLayoutId() {
        return R.layout.transaction_records;
    }

    public void initView() {
        ButterKnife.bind(this);
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        radioGroup.setOnCheckedChangeListener(this);
    }

    public void initData() {
        maintrsactionlistEvents = new ArrayList<>();
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
            Log.i("strHistory", "onPage----: " + strHistory);
            if (TextUtils.isEmpty(strHistory) || strHistory.length() == 2) {
                tetNone.setVisibility(View.VISIBLE);
                recyJylist.setVisibility(View.GONE);
            } else {
                tetNone.setVisibility(View.GONE);
                recyJylist.setVisibility(View.VISIBLE);
                //show trsaction ist
                showTrsactionlist(strHistory);
            }

        } else {
            tetNone.setVisibility(View.VISIBLE);
            recyJylist.setVisibility(View.GONE);
        }

    }

    //show trsaction list
    private void showTrsactionlist(String strHistory) {
        try {
            JSONArray jsonArray = new JSONArray(strHistory);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                MaintrsactionlistEvent maintrsactionlistEvent = new MaintrsactionlistEvent();
                String type = jsonObject.getString("type");
                tx_hash = jsonObject.getString("tx_hash");
                amount = jsonObject.getString("amount");
                is_mine = jsonObject.getBoolean("is_mine");//false ->get   true ->push
                date = jsonObject.getString("date");
                String tx_status = jsonObject.getString("tx_status");
                if (type.equals("history")) {
                    confirmations = jsonObject.getString("confirmations");
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

                    txCreatTrsaction = jsonObject.getString("tx");
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

                //Binder Adapter
                recyJylist.setLayoutManager(new LinearLayoutManager(TransactionRecordsActivity.this));
                MaindowndatalistAdapetr trsactionlistAdapter = new MaindowndatalistAdapetr(maintrsactionlistEvents);
                recyJylist.setAdapter(trsactionlistAdapter);

                trsactionlistAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
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
                                        intent.putExtra("txCreatTrsaction", tx_Onclick);
                                        startActivity(intent);

                                    } else {
                                        intent.putExtra("tx_hash", tx_hash1);
                                        intent.putExtra("keyValue", "B");
                                        intent.putExtra("listType", typeDele);
                                        startActivity(intent);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case R.id.txt_delete:
                                if ("tx".equals(typeDele)) {
                                    String invoice_id = maintrsactionlistEvents.get(position).getInvoice_id();
                                    try {
                                        Daemon.commands.callAttr("delete_invoice", invoice_id);
                                        maintrsactionlistEvents.remove(position);
                                        trsactionlistAdapter.notifyItemChanged(position);
                                        trsactionlistAdapter.notifyDataSetChanged();
                                        EventBus.getDefault().post(new FirstEvent("22"));

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    mToast(getResources().getString(R.string.delete_unBroad));
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

    @OnClick({R.id.img_backTrsa})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_backTrsa:
                finish();
                break;
        }
    }


    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId){
            case R.id.radio_one:
                strChoose = "send";
                maintrsactionlistEvents.clear();
                mTransactionrecordSend(strChoose);

                break;
            case R.id.radio_two:
                strChoose = "receive";
                maintrsactionlistEvents.clear();
                mTransactionrecordSend(strChoose);
                break;
        }
    }
}
