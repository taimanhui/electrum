package org.haobtc.wallet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import org.haobtc.wallet.activities.CreateWalletPageActivity;
import org.haobtc.wallet.activities.GuideActivity;
import org.haobtc.wallet.activities.ReceivedPageActivity;
import org.haobtc.wallet.activities.SendOne2OneMainPageActivity;
import org.haobtc.wallet.activities.SettingActivity;
import org.haobtc.wallet.activities.SignaturePageActivity;
import org.haobtc.wallet.activities.TransactionDetailsActivity;
import org.haobtc.wallet.activities.TransactionRecordsActivity;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.MaindowndatalistAdapetr;
import org.haobtc.wallet.adapter.MyPagerAdapter;
import org.haobtc.wallet.bean.MainSweepcodeBean;
import org.haobtc.wallet.bean.MainWheelBean;
import org.haobtc.wallet.bean.MaintrsactionlistEvent;
import org.haobtc.wallet.bean.ScanCheckDetailBean;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.fragment.mainwheel.AddViewFragment;
import org.haobtc.wallet.fragment.mainwheel.WheelViewpagerFragment;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.MyDialog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity implements View.OnClickListener, OnRefreshListener {

    private ImageView imageViewSweep, imageViewSetting;
    private TextView textView;

    private List<View> viewList = new ArrayList<>();//ViewPager数据源
    private MyPagerAdapter myPagerAdapter;//适配器
    private Button button_send, button_receive, button_signature;
    private ViewPager viewPager;
    private final String FIRST_RUN = "is_first_run";
    SharedPreferences sharedPreferences;
    private static Daemon daemonModel;
    static final String TAG = "PythonOnAndroid";
    public static Python py;
    private RecyclerView recy_data;
    private TextView btnAddmoney;
    //remeber first back time
    private long firstTime = 0;
    private MyDialog myDialog;
    private ArrayList<String> dataListName;
    private int mCurrentPosition = 0;
    private TextView tetNone;
    private PyObject get_history_tx;
    private ArrayList<MaintrsactionlistEvent> maintrsactionlistEvents;
    private String tx_hash;
    private String date;
    private String amount;
    private boolean is_mine;
    private String confirmations;
    private JSONArray jsonArray;
    private RxPermissions rxPermissions;
    private static final int REQUEST_CODE = 0;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:

                    break;
            }
        }
    };
    private List<Fragment> fragmentList;
    private boolean jumpOr;
    private PyObject parse_qr;
    private String txCreatTrsaction;
    private PyObject parse_tx;
    private MaindowndatalistAdapetr trsactionlistAdapter;
    private int sendAmount = 0;
    private String message = "";
    private String strNames;
    private SmartRefreshLayout refreshLayout;

    @Override
    public int getLayoutId() {
        return R.layout.main_activity;
    }


    @Override
    public void initView() {
        sharedPreferences = getSharedPreferences("preferences", MODE_PRIVATE);
        //Eventbus register
        EventBus.getDefault().register(this);
//        init();
        jumpOr = sharedPreferences.getBoolean("JumpOr", true);
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
        dataListName = new ArrayList<>();
        if (!jumpOr) {
            //Rolling Wallet
            mWheelplanting();
        }

    }


    private void mWheelplanting() {
        fragmentList = new ArrayList<>();

        PyObject get_wallets_list_info = null;
        try {
            get_wallets_list_info = Daemon.commands.callAttr("get_wallets_list_info");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (get_wallets_list_info != null) {
            String toString = get_wallets_list_info.toString();
            Log.i("javaBean", "mjavaBean----: " + toString + "----lenth----" + toString.length());
            Gson gson = new Gson();
            MainWheelBean mainWheelBean = gson.fromJson(toString, MainWheelBean.class);
            List<MainWheelBean.WalletsBean> wallets = mainWheelBean.getWallets();
            strNames = wallets.get(0).getName();
            if (wallets != null) {
                for (int i = 0; i < wallets.size(); i++) {
                    String walletType = wallets.get(i).getWalletType();
                    String balance = wallets.get(i).getBalance();
                    String name = wallets.get(i).getName();
                    dataListName.add(name);
                    String streplace = walletType.replaceAll("of", "/");
                    fragmentList.add(new WheelViewpagerFragment(name, streplace, balance));

                }
                dataListName.add("");
                fragmentList.add(new AddViewFragment());
                viewPager.setOffscreenPageLimit(4);
                viewPager.setPageMargin(40);
                viewPager.setAdapter(new ViewPagerFragmentStateAdapter(getSupportFragmentManager(), fragmentList));

                //choose wallet
                try {
                    Daemon.commands.callAttr("load_wallet", dataListName.get(0));
                    Daemon.commands.callAttr("select_wallet", dataListName.get(0));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //trsaction list data
                downMainListdata();

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
                    myDialog.show();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            maintrsactionlistEvents.clear();
                            Log.i("onPageSelected", "mCurrentPosition-----" + mCurrentPosition);

                            if (mCurrentPosition != position) {
                                strNames = dataListName.get(position);
                                Log.i("onPageSelected", "strNames-----" + strNames);
                                if (!TextUtils.isEmpty(strNames)) {
                                    try {
                                        Daemon.commands.callAttr("load_wallet", strNames);
                                        Daemon.commands.callAttr("select_wallet", strNames);

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Log.i("onPageSelected", "try-----" + e.getMessage());
                                        return;
                                    }
                                    //trsaction list data
                                    downMainListdata();
                                }
                            }
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
        try {
            //get transaction json
            get_history_tx = Daemon.commands.callAttr("get_all_tx_list");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //get transaction list
        if (!get_history_tx.isEmpty()) {
            tetNone.setVisibility(View.GONE);
            recy_data.setVisibility(View.VISIBLE);
            String strHistory = get_history_tx.toString();
            Log.i("strHistory", "onPage----: " + strHistory);

            if (strHistory.length() == 2) {
                myDialog.dismiss();
                tetNone.setVisibility(View.VISIBLE);
                recy_data.setVisibility(View.GONE);
            } else {
                //show trsaction list
                showTrsactionlist(strHistory);
            }

        } else {
            tetNone.setVisibility(View.VISIBLE);
            recy_data.setVisibility(View.GONE);
        }

    }

    //show trsaction list
    private void showTrsactionlist(String strHistory) {
        refreshLayout.finishRefresh();
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
                if (type.equals("history")) {
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
                    maintrsactionlistEvent.setDate(date);
                    maintrsactionlistEvent.setAmount(amount);
                    maintrsactionlistEvent.setIs_mine(is_mine);
                    maintrsactionlistEvent.setType(type);
                    maintrsactionlistEvent.setTx_status(tx_status);
                    maintrsactionlistEvent.setInvoice_id(invoice_id);
                    maintrsactionlistEvents.add(maintrsactionlistEvent);
                }

            }
            //Binder Adapter
            trsactionlistAdapter = new MaindowndatalistAdapetr(maintrsactionlistEvents);
            recy_data.setAdapter(trsactionlistAdapter);
            myDialog.dismiss();
            trsactionlistAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                @Override
                public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                    String typeDele = maintrsactionlistEvents.get(position).getType();
                    switch (view.getId()){
                        case R.id.lin_Item:
                            try {
                                JSONObject jsonObject = jsonArray.getJSONObject(position);
                                String tx_hash1 = jsonObject.getString("tx_hash");
                                Intent intent = new Intent(MainActivity.this, TransactionDetailsActivity.class);
                                if (typeDele.equals("tx")){
                                    String tx_Onclick = jsonObject.getString("tx");
                                    intent.putExtra("keyValue", "B");
                                    intent.putExtra("listType", typeDele);
                                    intent.putExtra("txCreatTrsaction", tx_Onclick);
                                    startActivity(intent);

                                }else{
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
                            if ("tx".equals(typeDele)){
                                String invoice_id = maintrsactionlistEvents.get(position).getInvoice_id();
                                try {
                                    Daemon.commands.callAttr("delete_invoice", invoice_id);
                                    maintrsactionlistEvents.remove(position);
                                    trsactionlistAdapter.notifyItemChanged(position);
                                    trsactionlistAdapter.notifyDataSetChanged();
                                    downMainListdata();

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }else{
                                mToast(getResources().getString(R.string.delete_unBroad));
                            }
                            break;
                    }
                }
            });

        } catch (JSONException e) {
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
                Intent intent6 = new Intent(MainActivity.this, CreateWalletPageActivity.class);
                startActivity(intent6);

                break;

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(FirstEvent updataHint) {
        String msgVote = updataHint.getMsg();
        if (msgVote.equals("11")) {
            Log.i("threadMode", "event: " + msgVote);
            //Rolling Wallet
            mWheelplanting();

        }else if (msgVote.equals("22")){
            maintrsactionlistEvents.clear();
            //trsaction list data
            downMainListdata();
            trsactionlistAdapter.notifyDataSetChanged();

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
                                intent.putExtra("wallet_name",strNames);
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
                return true;
            } else {
                System.exit(0);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        maintrsactionlistEvents.clear();
        //trsaction list data
        downMainListdata();
        trsactionlistAdapter.notifyDataSetChanged();

    }
}