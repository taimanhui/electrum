package org.haobtc.wallet.activities;

import android.widget.ArrayAdapter;

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.RecyclerView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.MaindowndatalistAdapetr;
import org.haobtc.wallet.utils.CommonUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TransactionRecordsActivity extends BaseActivity {
    @BindView(R.id.recy_jylist)
    RecyclerView recyJylist;
    @BindView(R.id.spi_BTC)
    AppCompatSpinner spiBTC;
    @BindView(R.id.spi_ZT)
    AppCompatSpinner spiZT;
    private ArrayList<String> strTyoe;
    private ArrayList<String> strZTlist;

    public int getLayoutId() {
        return R.layout.transaction_records;
    }

    public void initView() {
        ButterKnife.bind(this);
        CommonUtils.enableToolBar(this, R.string.transaction_records);
    }

    public void initData() {
        strTyoe = new ArrayList<>();
        strZTlist = new ArrayList<>();
        //datalist
        mTransactionrecord();

        //spinnerTYPE
        mSpinnerTypeLeft();

        //spinnerZT
        mSpinnerZTRight();

    }

    public void mSpinnerTypeLeft() {
        strTyoe.add("发币");
        strTyoe.add("收币");
        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> arrayTypeAdapter = new ArrayAdapter<String>(TransactionRecordsActivity.this, R.layout.spinnerlin, strTyoe);
        //设置样式
        arrayTypeAdapter.setDropDownViewResource(R.layout.spinnerlin);
        spiBTC.setAdapter(arrayTypeAdapter);

    }

    private void mSpinnerZTRight() {
        strZTlist.add("全部");
        strZTlist.add("待广播");
        strZTlist.add("待你签名");
        strZTlist.add("待确认");
        strZTlist.add("已确认");
        // 建立Adapter并且绑定数据源
        ArrayAdapter<String> arrayZTAdapter = new ArrayAdapter<String>(TransactionRecordsActivity.this, R.layout.spinnerlin, strZTlist);
        //设置样式
        arrayZTAdapter.setDropDownViewResource(R.layout.spinnerlin);
        spiZT.setAdapter(arrayZTAdapter);
    }

    private void mTransactionrecord() {
        ArrayList<String> dataList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            dataList.add("ahiedjlmk32lk2n42nk44" + i);
        }
        MaindowndatalistAdapetr myItemRecyclerViewAdapterTransaction = new MaindowndatalistAdapetr(dataList);
        recyJylist.setAdapter(myItemRecyclerViewAdapterTransaction);

    }

}
