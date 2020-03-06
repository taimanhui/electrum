package org.haobtc.wallet.activities.set;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.PyObject;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.CNYAdapter;
import org.haobtc.wallet.bean.CNYBean;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.utils.Daemon;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CurrencyActivity extends BaseActivity {


    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.radio_one)
    RadioGroup radioOne;
    @BindView(R.id.recl_cnyTable)
    RecyclerView reclCnyTable;
    @BindView(R.id.tet_CheckAll)
    TextView tetCheckAll;

    private SharedPreferences.Editor edit;
    private String base_unit;
    private ArrayList<CNYBean> listCNY;
    private List<PyObject> pyObjects;
    private PyObject get_currencies;
    private int cny_unit;

    @Override
    public int getLayoutId() {
        return R.layout.activity_currency;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        base_unit = preferences.getString("base_unit", "mBTC");
        cny_unit = preferences.getInt("cny_unit", 0);
        edit = preferences.edit();

    }

    @Override
    public void initData() {
        reclCnyTable.setNestedScrollingEnabled(false);
        listCNY = new ArrayList<>();
        //BTC
        radioSelectOne();
        //CNY
        radioSelectTwo();
    }

    private void radioSelectOne() {
        RadioButton[] radioOnearray = new RadioButton[radioOne.getChildCount()];
        for (int i = 0; i < radioOnearray.length; i++) {
            radioOnearray[i] = (RadioButton) radioOne.getChildAt(i);
        }
        if (base_unit.equals("BTC")) {
            radioOnearray[0].setChecked(true);
        } else if (base_unit.equals("mBTC")) {
            radioOnearray[1].setChecked(true);
        } else if (base_unit.equals("sat")) {
            radioOnearray[2].setChecked(true);
        }

        radioOne.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.btn_btcOne:
                        try {
                            Daemon.commands.callAttr("set_base_uint", "BTC");
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        edit.putString("base_unit", "BTC");
                        edit.apply();
                        break;
                    case R.id.btn_btcTwo:
                        try {
                            Daemon.commands.callAttr("set_base_uint", "mBTC");
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        edit.putString("base_unit", "mBTC");
                        edit.apply();
                        break;
                    case R.id.btn_btcThree:
                        try {
                            Daemon.commands.callAttr("set_base_uint", "sat");
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                        edit.putString("base_unit", "sat");
                        edit.apply();
                        break;

                }

            }
        });

    }

    private void radioSelectTwo() {
        try {
            get_currencies = Daemon.commands.callAttr("get_currencies");
            pyObjects = get_currencies.asList();

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("set_currency", "set_base_unit:==== " + e.getMessage());
            return;
        }
//        listCNY.add("人民币（CNY）");
//        listCNY.add("美元（USD）");
//        listCNY.add("韩元（KMR）");
        listCNY.add(new CNYBean("人民币（CNY）", false));
        listCNY.add(new CNYBean("美元（USD）", false));
        listCNY.add(new CNYBean("韩元（KMR）", false));
        if (get_currencies != null) {
            for (int i = 0; i < pyObjects.size(); i++) {
                if (!pyObjects.get(i).equals("CNY") && !pyObjects.get(i).equals("USD") && !pyObjects.get(i).equals("KMR")) {
                    listCNY.add(new CNYBean(String.valueOf(pyObjects.get(i)), false));
                }
            }

            CNYAdapter cnyAdapter = new CNYAdapter(CurrencyActivity.this, listCNY, cny_unit);
            reclCnyTable.setAdapter(cnyAdapter);
            cnyAdapter.setOnLisennorClick(new CNYAdapter.onLisennorClick() {
                @Override
                public void ItemClick(int pos) {
                    if (pos == 0) {
                        try {
                            Daemon.commands.callAttr("set_currency", "CNY");
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    } else if (pos == 1) {
                        try {
                            Daemon.commands.callAttr("set_currency", "USD");
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    } else if (pos == 2) {
                        try {
                            Daemon.commands.callAttr("set_currency", "KMR");
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    } else {
                        try {
                            Daemon.commands.callAttr("set_currency", listCNY.get(pos).getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                            return;
                        }
                    }
                    EventBus.getDefault().post(new FirstEvent("22"));
//                    Log.i(TAG, "ItemClick: ");
                    edit.putInt("cny_unit", pos);
                    edit.apply();
                }
            });

        }
    }

    @OnClick({R.id.img_back,R.id.tet_CheckAll})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_CheckAll:
                String strOpen = tetCheckAll.getText().toString();
                if (strOpen.equals(getResources().getString(R.string.check_all))) {
                    RelativeLayout.LayoutParams linearParams1 = (RelativeLayout.LayoutParams) reclCnyTable.getLayoutParams();
                    linearParams1.height = ViewGroup.LayoutParams.WRAP_CONTENT;;
                    reclCnyTable.setLayoutParams(linearParams1);
                    tetCheckAll.setText(getResources().getString(R.string.retract));

                } else {
                    RelativeLayout.LayoutParams linearParams1 = (RelativeLayout.LayoutParams) reclCnyTable.getLayoutParams();
                    linearParams1.height = 530;;
                    reclCnyTable.setLayoutParams(linearParams1);
                    tetCheckAll.setText(getResources().getString(R.string.check_all));
                }
                break;
        }
    }

}
