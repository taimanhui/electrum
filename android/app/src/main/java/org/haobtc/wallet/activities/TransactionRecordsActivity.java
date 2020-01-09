package org.haobtc.wallet.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.MaindowndatalistAdapetr;
import org.haobtc.wallet.bean.MaintrsactionlistEvent;
import org.haobtc.wallet.utils.Daemon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TransactionRecordsActivity extends BaseActivity {
    @BindView(R.id.recy_jylist)
    RecyclerView recyJylist;
    @BindView(R.id.spi_BTC)
    AppCompatSpinner spiBTC;
    @BindView(R.id.spi_ZT)
    AppCompatSpinner spiZT;
    @BindView(R.id.tet_None)
    TextView tetNone;
    @BindView(R.id.img_backTrsa)
    ImageView imgBackTrsa;
    private ArrayList<MaintrsactionlistEvent> maintrsactionlistEvents;
    private SharedPreferences preferences;
    private int haveSelectBTC = 0;
    private int haveSelectState = 0;
    private String strChoose;
    private String strChoosestate;
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
        preferences = getSharedPreferences("preferences", MODE_PRIVATE);

    }

    public void initData() {
        maintrsactionlistEvents = new ArrayList<>();
        //datalist
        mTransactionrecordAll();
        //spinnerTYPE
        mSpinnerTypeLeft();

        //spinnerZT
        mSpinnerZTRight();

    }

    public void mSpinnerTypeLeft() {
        String[] strTyoe = getResources().getStringArray(R.array.type);

        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> arrayTypeAdapter = new ArrayAdapter<String>(TransactionRecordsActivity.this, R.layout.spinnerlin, strTyoe);
        //设置样式
        arrayTypeAdapter.setDropDownViewResource(R.layout.spinnerlin);
        spiBTC.setAdapter(arrayTypeAdapter);
        spiBTC.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                maintrsactionlistEvents.clear();
                if (position == 0) {
                    haveSelectBTC = 0;
                    if (haveSelectState == 1) {
                        //datalist
                        mTransactionrecordSate(strChoosestate);
                    } else {
                        //datalist all
                        mTransactionrecordAll();
                    }

                } else {
                    haveSelectBTC = 1;
                    if (position == 1 && haveSelectState == 1) {
                        //send
                        strChoose = "send";
                        mTransactionrecordSendTwo(strChoose, strChoosestate);

                    } else if (position == 2 && haveSelectState == 1) {
                        //receive
                        strChoose = "receive";
                        mTransactionrecordSendTwo(strChoose, strChoosestate);

                    } else if (position == 1) {
                        //send
                        strChoose = "send";
                        mTransactionrecordSend(strChoose);

                    } else if (position == 2) {
                        //receive
                        strChoose = "receive";
                        mTransactionrecordSend(strChoose);

                    }
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void mSpinnerZTRight() {
        String[] strZTlist = getResources().getStringArray(R.array.state);

        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> arrayZTAdapter = new ArrayAdapter<String>(TransactionRecordsActivity.this, R.layout.spinnerlin, strZTlist);
        //设置样式
        arrayZTAdapter.setDropDownViewResource(R.layout.spinnerlin);
        spiZT.setAdapter(arrayZTAdapter);
        spiZT.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                maintrsactionlistEvents.clear();
                if (position == 0) {
                    //all
                    haveSelectState = 0;
                    if (haveSelectBTC == 1) {
                        mTransactionrecordSend(strChoose);
                    } else {
                        //datalist
                        mTransactionrecordAll();
                    }

                } else {
                    haveSelectState = 1;
                    if (position == 1 && haveSelectBTC == 1) {
                        //tobebroadcast
                        strChoosestate = "tobebroadcast";
                        mTransactionrecordSendTwo(strChoose, strChoosestate);

                    } else if (position == 2 && haveSelectBTC == 1) {
                        //tobesign
                        strChoosestate = "tobesign";
                        mTransactionrecordSendTwo(strChoose, strChoosestate);

                    } else if (position == 3 && haveSelectBTC == 1) {
                        //tobeconfirm
                        strChoosestate = "tobeconfirm";
                        mTransactionrecordSendTwo(strChoose, strChoosestate);

                    } else if (position == 4 && haveSelectBTC == 1) {
                        //confirmed
                        strChoosestate = "confirmed";
                        mTransactionrecordSendTwo(strChoose, strChoosestate);

                    } else if (position == 1) {
                        //tobebroadcast
                        strChoosestate = "tobebroadcast";
                        mTransactionrecordSate(strChoosestate);

                    } else if (position == 2) {
                        //tobesign
                        strChoosestate = "tobesign";
                        mTransactionrecordSate(strChoosestate);

                    } else if (position == 3) {
                        //tobeconfirm
                        strChoosestate = "tobeconfirm";
                        mTransactionrecordSate(strChoosestate);

                    } else if (position == 4) {
                        //confirmed
                        strChoosestate = "confirmed";
                        mTransactionrecordSate(strChoosestate);

                    }

                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    //all data
    private void mTransactionrecordAll() {
        PyObject get_history_tx = null;
        try {
            //get transaction json
            get_history_tx = Daemon.commands.callAttr("get_all_tx_list");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (get_history_tx != null) {
            //get transaction list
            getHistroyIntry(get_history_tx);
        }

    }

    private void mTransactionrecordSend(String sends) {
        //get transaction json
        PyObject get_history_tx = null;
        try {
            get_history_tx = Daemon.commands.callAttr("get_all_tx_list", new Kwarg("tx_status", sends));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (get_history_tx != null) {
            getHistroyIntry(get_history_tx);
        }

    }

    private void mTransactionrecordSate(String states) {
        //get transaction json
        PyObject get_history_tx = null;
        try {
            get_history_tx = Daemon.commands.callAttr("get_all_tx_list", new Kwarg("history_status", states));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (get_history_tx != null) {
            getHistroyIntry(get_history_tx);
        }

    }

    private void mTransactionrecordSendTwo(String sends, String state) {
        //get transaction json
        PyObject get_history_tx = null;
        try {
            get_history_tx = Daemon.commands.callAttr("get_all_tx_list", new Kwarg("tx_status", sends), new Kwarg("history_status", state));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (get_history_tx != null) {
            //get transaction list
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
                if (type.equals("history")) {
                    date = jsonObject.getString("date");
                    confirmations = jsonObject.getString("confirmations");
                    //add attribute
                    maintrsactionlistEvent.setTx_hash(tx_hash);
                    maintrsactionlistEvent.setDate(date);
                    maintrsactionlistEvent.setAmount(amount);
                    maintrsactionlistEvent.setIs_mine(is_mine);
                    maintrsactionlistEvent.setConfirmations(confirmations);
                    maintrsactionlistEvent.setType(type);
                    maintrsactionlistEvents.add(maintrsactionlistEvent);
                } else {
                    String tx_status = jsonObject.getString("tx_status");
                    txCreatTrsaction = jsonObject.getString("tx");
                    String invoice_id = jsonObject.getString("invoice_id");//delete use
                    //add attribute
                    maintrsactionlistEvent.setTx_hash(tx_hash);
//                    maintrsactionlistEvent.setDate(date);
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

                trsactionlistAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(position);
                            String type1 = jsonObject.getString("type");
                            String tx_hash1 = jsonObject.getString("tx_hash");
                            Intent intent = new Intent(TransactionRecordsActivity.this, TransactionDetailsActivity.class);
                            intent.putExtra("tx_hash", tx_hash1);
                            intent.putExtra("keyValue", "B");
                            intent.putExtra("listType", type1);
                            intent.putExtra("txCreatTrsaction", txCreatTrsaction);
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
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
}
