package org.haobtc.wallet;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.google.gson.Gson;
import com.gyf.immersionbar.ImmersionBar;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.thirdgoddess.tnt.viewpager_adapter.ViewPagerFragmentStateAdapter;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.activities.CreateWalletActivity;
import org.haobtc.wallet.activities.GuideActivity;
import org.haobtc.wallet.activities.SendOne2OneMainPageActivity;
import org.haobtc.wallet.activities.SettingActivity;
import org.haobtc.wallet.activities.TransactionDetailsActivity;
import org.haobtc.wallet.activities.TransactionRecordsActivity;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.MaindowndatalistAdapetr;
import org.haobtc.wallet.adapter.MyPagerAdapter;
import org.haobtc.wallet.bean.AddressEvent;
import org.haobtc.wallet.bean.MainSweepcodeBean;
import org.haobtc.wallet.bean.MaintrsactionlistEvent;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.event.SecondEvent;
import org.haobtc.wallet.fragment.mainwheel.AddViewFragment;
import org.haobtc.wallet.fragment.mainwheel.WheelViewpagerFragment;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.MyDialog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends BaseActivity implements View.OnClickListener, OnRefreshListener {

    private ImageView imageViewSweep, imageViewSetting;
    private TextView textView;
    private ViewPager viewPager;
    private final String FIRST_RUN = "is_first_run";
    SharedPreferences sharedPreferences;
    public static Python py;
    private RecyclerView recy_data;
    private TextView btnAddmoney;
    //remeber first back time
    private long firstTime = 0;
    private MyDialog myDialog;
    private int mCurrentPosition = 0;
    private TextView tetNone;
    private ArrayList<MaintrsactionlistEvent> maintrsactionlistEvents;
    private String tx_hash;
    private String date;
    private String amount;
    private boolean is_mine;
    private String confirmations;
    private JSONArray jsonArray;
    private RxPermissions rxPermissions;
    private static final int REQUEST_CODE = 0;
    private List<Fragment> fragmentList;
    private boolean jumpOr;
    private PyObject parse_qr;
    private String txCreatTrsaction;
    private MaindowndatalistAdapetr trsactionlistAdapter;
    private int sendAmount = 0;
    private String message = "";
    private String strNames;
    private SmartRefreshLayout refreshLayout;
    private ArrayList<AddressEvent> walletnameList;
    private String walletType;
    private String strType;

    @Override
    public int getLayoutId() {
        return R.layout.main_activity;
    }


    @Override
    public void initView() {
        sharedPreferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        //FIRST_RUN,if frist run
        edit.putBoolean(FIRST_RUN, true);
        edit.apply();
        //Eventbus register
        EventBus.getDefault().register(this);
//        init();
        jumpOr = sharedPreferences.getBoolean("JumpOr", false);
        if (sharedPreferences.getBoolean(FIRST_RUN, false)) {
            init();
        } else {
            if (jumpOr) {
                //splash
                initGuide();
            } else {
                //CreatWallet
                initCreatWallet();
            }
        }
    }

    private void init() {
        myDialog = MyDialog.showDialog(MainActivity.this);
        rxPermissions = new RxPermissions(this);
        imageViewSweep = findViewById(R.id.img_sweep);
        btnAddmoney = findViewById(R.id.tet_Addmoney);
        recy_data = findViewById(R.id.recy_data);
        imageViewSetting = findViewById(R.id.img_setting);
        textView = findViewById(R.id.textView_more);
        tetNone = findViewById(R.id.tet_None);
        refreshLayout = findViewById(R.id.smart_RefreshLayout);
        imageViewSweep.setOnClickListener(this);
        imageViewSetting.setOnClickListener(this);
        textView.setOnClickListener(this);
        btnAddmoney.setOnClickListener(this);
        refreshLayout.setEnableLoadMore(false);
        refreshLayout.setEnableRefresh(true);
        refreshLayout.setOnRefreshListener(this);

        //wallet name and balance list
        walletnameList = new ArrayList<>();

    }

    private void initCreatWallet() {
        Intent intent = new Intent(this, CreateWalletActivity.class);
        startActivity(intent);
        finish();
    }

    private void initGuide() {
        Intent intent = new Intent(this, GuideActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void initData() {
        viewPager = findViewById(R.id.viewPager);
        maintrsactionlistEvents = new ArrayList<>();
        //Binder Adapter
        trsactionlistAdapter = new MaindowndatalistAdapetr(maintrsactionlistEvents);
        recy_data.setAdapter(trsactionlistAdapter);
        if (jumpOr) {
            //Rolling Wallet
            mWheelplanting();
        }
    }

    private void mWheelplanting() {
        fragmentList = new ArrayList<>();
        walletnameList.clear();
        PyObject get_wallets_list_info = null;
        try {
            get_wallets_list_info = Daemon.commands.callAttr("list_wallets");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("mWheelplanting", "mWheelplanting:==== " + e.getMessage());
            fragmentList.add(new AddViewFragment());
            viewPager.setOffscreenPageLimit(4);
            viewPager.setPageMargin(40);
            viewPager.setAdapter(new ViewPagerFragmentStateAdapter(getSupportFragmentManager(), fragmentList));
            //trsaction list data
            tetNone.setVisibility(View.VISIBLE);
            recy_data.setVisibility(View.GONE);
            return;
        }

        if (get_wallets_list_info != null && get_wallets_list_info.size() != 0) {
            String toStrings = get_wallets_list_info.toString();
            if (toStrings.length() != 2) {
                Log.i("get_wallets_list_info", "mjavaBean-----: " + toStrings);
                com.alibaba.fastjson.JSONArray jsons = com.alibaba.fastjson.JSONObject.parseArray(toStrings);
                for (int i = 0; i < jsons.size(); i++) {
                    Map jsonToMap = (Map) jsons.get(i);
                    Set keySets = jsonToMap.keySet();
                    Iterator ki = keySets.iterator();
                    AddressEvent addressEvent = new AddressEvent();
                    while (ki.hasNext()) {
                        //get key
                        String key = (String) ki.next();
                        String value = jsonToMap.get(key).toString();
                        addressEvent.setName(key);
                        addressEvent.setType(value);
                        walletnameList.add(addressEvent);
                    }
                }
                Log.i("mWheelplanting", "mWheelplanting: " + walletnameList.toString());

                if (walletnameList != null && walletnameList.size() != 0) {
                    strNames = walletnameList.get(0).getName();
                    strType = walletnameList.get(0).getType();
                    for (int i = 0; i < walletnameList.size(); i++) {
                        String name = walletnameList.get(i).getName();
                        walletType = walletnameList.get(i).getType();
                        if (i == 0) {
                            fragmentList.add(new WheelViewpagerFragment(name, walletType, true));
                        } else {
                            fragmentList.add(new WheelViewpagerFragment(name, walletType));
                        }

                    }
                    //trsaction list data
                    downMainListdata();
                    fragmentList.add(new AddViewFragment());
                    viewPager.setOffscreenPageLimit(4);
                    viewPager.setPageMargin(40);
                    viewPager.setAdapter(new ViewPagerFragmentStateAdapter(getSupportFragmentManager(), fragmentList));

                }
            } else {
                fragmentList.add(new AddViewFragment());
                viewPager.setOffscreenPageLimit(4);
                viewPager.setPageMargin(40);
                viewPager.setAdapter(new ViewPagerFragmentStateAdapter(getSupportFragmentManager(), fragmentList));
                //trsaction list data
                tetNone.setVisibility(View.VISIBLE);
                recy_data.setVisibility(View.GONE);
            }
        }
        //scroll
        viewPagerScroll();
    }

    //viewPagerScroll
    private void viewPagerScroll() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Log.i("onPageSelected", "pos__ " + position);
                if (position == (fragmentList.size() - 1)) {
                    Log.i("onPageSelected", "名字为空");
                    mCurrentPosition = position;
                } else {
                    strNames = walletnameList.get(position).getName();
                    strType = walletnameList.get(position).getType();
                    myDialog.show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //refresh only wallet
                            if (fragmentList.size() - 1 != position) {
                                ((WheelViewpagerFragment) fragmentList.get(position)).refreshList();
                            }
                            //trsaction list data
                            downMainListdata();
                            mCurrentPosition = position;
                        }
                    }, 350);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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
            tetNone.setVisibility(View.VISIBLE);
            recy_data.setVisibility(View.GONE);
            Log.i("downMainListdata", "downMaina===: " + e.getMessage());
            return;
        }
        myDialog.dismiss();
        //get transaction list
        if (get_history_tx != null) {
            tetNone.setVisibility(View.GONE);
            recy_data.setVisibility(View.VISIBLE);
            String strHistory = get_history_tx.toString();
            Log.i("strHistory", "onPage----: " + strHistory);
            refreshLayout.finishRefresh();
            if (strHistory.length() == 2) {
                tetNone.setVisibility(View.VISIBLE);
                recy_data.setVisibility(View.GONE);
            } else {
                //show trsaction list
                showTrsactionlist(strHistory);
            }

        } else {
            refreshLayout.finishRefresh();
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
                    maintrsactionlistEvent.setType(type);
                    maintrsactionlistEvent.setTx_status(tx_status);
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
                                Intent intent = new Intent(MainActivity.this, TransactionDetailsActivity.class);
                                if (typeDele.equals("tx")) {
                                    String tx_Onclick = jsonObject.getString("tx");
                                    intent.putExtra("keyValue", "B");
                                    intent.putExtra("tx_hash", tx_hash1);
                                    intent.putExtra("isIsmine", is_mine);
                                    intent.putExtra("strwalletType", strType);
                                    intent.putExtra("listType", typeDele);
                                    intent.putExtra("dataTime", date);
                                    intent.putExtra("txCreatTrsaction", tx_Onclick);
                                    startActivity(intent);

                                } else {
                                    intent.putExtra("tx_hash", tx_hash1);
                                    intent.putExtra("isIsmine", is_mine);
                                    intent.putExtra("dataTime", date);
                                    intent.putExtra("strwalletType", strType);
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
                                mToast(getResources().getString(R.string.delete_unBroad));
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

    /**
     * set white background and black text
     */
    public void mInitState() {
        ImmersionBar.with(this).keyboardEnable(false).statusBarDarkFont(true, 0.2f).navigationBarColor(R.color.button_bk_ddake).init();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_sweep:
                rxPermissions
                        .request(Manifest.permission.CAMERA)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                //If you have already authorized it, you can directly jump to the QR code scanning interface
                                Intent intent2 = new Intent(this, CaptureActivity.class);
                                startActivityForResult(intent2, REQUEST_CODE);
                            } else { // Oups permission denied
                                Toast.makeText(this, R.string.photopersion, Toast.LENGTH_SHORT).show();
                            }
                        }).dispose();
                break;
            case R.id.img_setting:
                Intent intent1 = new Intent(this, SettingActivity.class);
                startActivity(intent1);
                break;
            case R.id.textView_more:
                Intent intent2 = new Intent(this, TransactionRecordsActivity.class);
                startActivity(intent2);
                break;
            case R.id.tet_Addmoney:
                Intent intent6 = new Intent(MainActivity.this, CreateWalletActivity.class);
                startActivity(intent6);

                break;

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(FirstEvent updataHint) {
        String msgVote = updataHint.getMsg();
        if (msgVote.equals("11")) {
            //Rolling Wallet
            mWheelplanting();

        } else if (msgVote.equals("22")) {
            maintrsactionlistEvents.clear();
            //trsaction list data
            downMainListdata();
            trsactionlistAdapter.notifyDataSetChanged();

        } else if (msgVote.equals("33")) {
            tetNone.setVisibility(View.VISIBLE);
            recy_data.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Scan QR code / barcode return
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                Log.i("ddddd", "onActivityResult: " + content);
                //bitcoin:mhZ5dTc91TxttEvFJifBNPNqwLAD5CxhYF
                if (!TextUtils.isEmpty(content)) {
                    try {
                        parse_qr = Daemon.commands.callAttr("parse_pr", content);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("PyObject", "parse_qr+++++++:  " + e.getMessage());
                        return;
                    }
                    if (parse_qr != null) {
                        String strParse = parse_qr.toString();
                        Log.i("PyObject", "parse_qr:  " + strParse);
                        try {
                            JSONObject jsonObject = new JSONObject(strParse);
                            int type = jsonObject.getInt("type");
                            Gson gson = new Gson();
                            if (type == 1) {
                                MainSweepcodeBean mainSweepcodeBean = gson.fromJson(strParse, MainSweepcodeBean.class);
                                MainSweepcodeBean.DataBean listData = mainSweepcodeBean.getData();
                                String address = listData.getAddress();
                                sendAmount = listData.getAmount();
                                message = listData.getMessage();

                                //address  -->  intent  send  activity
                                Intent intent = new Intent(MainActivity.this, SendOne2OneMainPageActivity.class);
                                intent.putExtra("sendAdress", address);
                                intent.putExtra("sendamount", sendAmount);
                                intent.putExtra("sendmessage", message);
                                intent.putExtra("wallet_name", strNames);
                                startActivity(intent);
                            } else if (type == 2) {
                                Intent intent = new Intent(MainActivity.this, TransactionDetailsActivity.class);
                                intent.putExtra("strParse", strParse);
                                intent.putExtra("keyValue", "B");
                                intent.putExtra("listType", "scan");
                                startActivity(intent);
                            } else {
                                mToast(getResources().getString(R.string.address_wrong));
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
    }

    /**
     * onclick dowble exit
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                Toast.makeText(MainActivity.this, R.string.dowbke_to_exit, Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
            } else {
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
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