package org.haobtc.wallet.activities.set;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CurrencyActivity extends BaseActivity {


    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.radio_one)
    RadioGroup radioOne;
    @BindView(R.id.radio_two)
    RadioGroup radioTwo;

    @Override
    public int getLayoutId() {
        return R.layout.activity_currency;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);


    }

    @Override
    public void initData() {
        radioSelectOne();
        radioSelectTwo();
    }

    private void radioSelectOne() {
        RadioButton[] radioOnearray = new RadioButton[radioOne.getChildCount()];
        for (int i = 0; i < radioOnearray.length; i++) {
            radioOnearray[i] = (RadioButton) radioOne.getChildAt(i);
        }
        radioOnearray[0].setChecked(true);
        radioOne.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

            }
        });

    }

    private void radioSelectTwo() {
        RadioButton[] radioArraytwo =  new RadioButton[radioTwo.getChildCount()];
        for (int i = 0; i < radioArraytwo.length; i++) {
            radioArraytwo[i] = (RadioButton) radioTwo.getChildAt(i);
        }
        radioArraytwo[0].setChecked(true);
        radioTwo.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

            }
        });
    }

    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
        }
    }
}
