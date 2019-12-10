package org.haobtc.wallet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.SinatrayPersonAdapetr;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TransactionDetailsActivity extends BaseActivity {

    @BindView(R.id.img_progressone)
    ImageView imgProgressone;
    @BindView(R.id.img_progresstwo)
    ImageView imgProgresstwo;
    @BindView(R.id.img_progressthree)
    ImageView imgProgressthree;
    @BindView(R.id.img_progressfour)
    ImageView imgProgressfour;
    @BindView(R.id.tet_Trone)
    TextView tetTrone;
    @BindView(R.id.tet_Trtwo)
    TextView tetTrtwo;
    @BindView(R.id.tet_Trthree)
    TextView tetTrthree;
    @BindView(R.id.tet_Trfore)
    TextView tetTrfore;
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tv_in_tb2)
    TextView tvInTb2;
    @BindView(R.id.img_share)
    ImageView imgShare;
    @BindView(R.id.tb2)
    RelativeLayout tb2;
    @BindView(R.id.btn1)
    Button btn1;
    @BindView(R.id.btn2)
    Button btn2;
    @BindView(R.id.btn3)
    Button btn3;
    @BindView(R.id.btn4)
    Button btn4;
    @BindView(R.id.card_Trantime)
    CardView cardTrantime;
    @BindView(R.id.tet_getMoneyaddress)
    TextView tetGetMoneyaddress;
    @BindView(R.id.tet_payAddress)
    TextView tetPayAddress;
    @BindView(R.id.cardView4)
    CardView cardView4;
    @BindView(R.id.textView12)
    TextView textView12;
    @BindView(R.id.textView14)
    TextView textView14;
    @BindView(R.id.textView13)
    TextView textView13;
    @BindView(R.id.textView15)
    TextView textView15;
    @BindView(R.id.textView17)
    TextView textView17;
    @BindView(R.id.textView16)
    TextView textView16;
    @BindView(R.id.view)
    View view;
    @BindView(R.id.textView18)
    TextView textView18;
    @BindView(R.id.view1)
    View view1;
    @BindView(R.id.textView19)
    TextView textView19;
    @BindView(R.id.textView20)
    TextView textView20;
    @BindView(R.id.cardView3)
    CardView cardView3;
    @BindView(R.id.sig_trans)
    Button sigTrans;
    public static final String TAG = "org.haobtc.wallet.activities.TransactionDetailsActivity";
    @BindView(R.id.recy_Signatory)
    RecyclerView recySignatory;


    @Override
    public int getLayoutId() {
        return R.layout.trans_details;
    }

    @Override
    public void initView() {
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String jsondef_get = intent.getStringExtra("jsondef_get");


    }

    @Override
    public void initData() {
        //sinagray people
        mSinatoryPerson();

    }

    private void mSinatoryPerson() {
        ArrayList<String> strSinalist = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strSinalist.add("联署人"+(i+1));
        }
        SinatrayPersonAdapetr sinatrayPersonAdapetr = new SinatrayPersonAdapetr(strSinalist);
        recySignatory.setAdapter(sinatrayPersonAdapetr);

    }


    @OnClick({R.id.img_back, R.id.img_share, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.img_share:
                break;
            case R.id.btn1:
                imgProgressone.setVisibility(View.VISIBLE);
                imgProgresstwo.setVisibility(View.GONE);
                imgProgressthree.setVisibility(View.GONE);
                imgProgressfour.setVisibility(View.GONE);
                break;
            case R.id.btn2:
                imgProgressone.setVisibility(View.GONE);
                imgProgresstwo.setVisibility(View.VISIBLE);
                imgProgressthree.setVisibility(View.GONE);
                imgProgressfour.setVisibility(View.GONE);
                break;
            case R.id.btn3:
                imgProgressone.setVisibility(View.GONE);
                imgProgresstwo.setVisibility(View.GONE);
                imgProgressthree.setVisibility(View.VISIBLE);
                imgProgressfour.setVisibility(View.GONE);
                break;
            case R.id.btn4:
                imgProgressone.setVisibility(View.GONE);
                imgProgresstwo.setVisibility(View.GONE);
                imgProgressthree.setVisibility(View.GONE);
                imgProgressfour.setVisibility(View.VISIBLE);
                break;
        }
    }

}

