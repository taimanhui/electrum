package org.haobtc.wallet;

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
import com.thirdgoddess.tnt.viewpager_adapter.ViewPagerFragmentStateAdapter;

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
import org.haobtc.wallet.bean.MainWheelBean;
import org.haobtc.wallet.bean.MaintrsactionlistEvent;
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


public class MainActivity extends BaseActivity implements View.OnClickListener {

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
    private String rowTrsation = "";
    private String tx_hash;
    private String date;
    private String amount;
    private boolean is_mine;
    private String confirmations;
    private JSONArray jsonArray;
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

    @Override
    public int getLayoutId() {
        return R.layout.main_activity;
    }


    @Override
    public void initView() {
        sharedPreferences = getSharedPreferences("preferences", MODE_PRIVATE);
        rowTrsation = sharedPreferences.getString("rowTrsation", "");
        //Eventbus register
        EventBus.getDefault().register(this);
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
        imageViewSweep = findViewById(R.id.img_sweep);
        btnAddmoney = findViewById(R.id.tet_Addmoney);
        recy_data = findViewById(R.id.recy_data);
        imageViewSetting = findViewById(R.id.img_setting);
        textView = findViewById(R.id.textView_more);
        tetNone = findViewById(R.id.tet_None);
        imageViewSweep.setOnClickListener(this);
        imageViewSetting.setOnClickListener(this);
        textView.setOnClickListener(this);
        btnAddmoney.setOnClickListener(this);


    }

    private void initCreatWallet() {
        Intent intent = new Intent(this, CreateWalletActivity.class);
        startActivity(intent);
    }

    private void initGuide() {
        Intent intent = new Intent(this, GuideActivity.class);
        startActivity(intent);
    }

    @Override
    public void initData() {
        viewPager = findViewById(R.id.viewPager);
        maintrsactionlistEvents = new ArrayList<>();
        dataListName = new ArrayList<>();
        if (!jumpOr){
            //Rolling Wallet
            mWheelplanting();
        }

    }


    private void mWheelplanting() {
        fragmentList = new ArrayList<>();

        PyObject get_wallets_list_info = Daemon.commands.callAttr("get_wallets_list_info");
        String toString = get_wallets_list_info.toString();
        Log.i("javaBean", "mjavaBean----: " + toString + "----lenth----" + toString.length());
        if (!TextUtils.isEmpty(toString)) {
            Gson gson = new Gson();
            MainWheelBean mainWheelBean = gson.fromJson(toString, MainWheelBean.class);
            List<MainWheelBean.WalletsBean> wallets = mainWheelBean.getWallets();
            if (wallets!=null){
                for (int i = 0; i < wallets.size(); i++) {
                    String walletType = wallets.get(i).getWalletType();
                    String balance = wallets.get(i).getBalance();
                    String name = wallets.get(i).getName();
                    dataListName.add(name);
                    String streplace = walletType.replaceAll("of", "/");
                    fragmentList.add(new WheelViewpagerFragment(name, streplace, balance));

                }
            }

            dataListName.add("");
            fragmentList.add(new AddViewFragment());
            viewPager.setOffscreenPageLimit(4);
            viewPager.setPageMargin(40);
            viewPager.setAdapter(new ViewPagerFragmentStateAdapter(getSupportFragmentManager(), fragmentList));
            //choose wallet
            Daemon.commands.callAttr("load_wallet", dataListName.get(0));
            Daemon.commands.callAttr("select_wallet", dataListName.get(0));
            //get transaction json
            get_history_tx = Daemon.commands.callAttr("get_all_tx_list", rowTrsation);
            //get transaction list
            if (!get_history_tx.isEmpty()) {
                tetNone.setVisibility(View.GONE);
                recy_data.setVisibility(View.VISIBLE);
                String strHistory = get_history_tx.toString();
                Log.i("strHistory", "onPage----: " + strHistory + "   size:  "+strHistory.length());

                if (strHistory.length() == 2){
                    myDialog.dismiss();
                }else{
                    //show trsaction list
                    showTrsactionlist(strHistory);
                }

            } else {
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
                if (position != (fragmentList.size() - 1)) {
                    myDialog.show();
                }

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        maintrsactionlistEvents.clear();
                        if (mCurrentPosition != position) {
                            String strNames = dataListName.get(position);
                            if (!TextUtils.isEmpty(strNames)) {
                                try {
                                    Daemon.commands.callAttr("load_wallet", strNames);
                                    Daemon.commands.callAttr("select_wallet", strNames);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                get_history_tx = Daemon.commands.callAttr("get_all_tx_list", rowTrsation);
                                if (!get_history_tx.isEmpty()) {
                                    tetNone.setVisibility(View.GONE);
                                    recy_data.setVisibility(View.VISIBLE);
                                    String strHistory = get_history_tx.toString();
                                    Log.i("strHistory", "onPage----: " + strHistory);
                                    if (strHistory.length() == 2){
                                        myDialog.dismiss();
                                    }else{
                                        //show trsaction list
                                        showTrsactionlist(strHistory);
                                    }


                                } else {
                                    tetNone.setVisibility(View.VISIBLE);
                                    recy_data.setVisibility(View.GONE);
                                }

                            }

                        }
                        mCurrentPosition = position;
                    }
                }, 350);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    //show trsaction list
    private void showTrsactionlist(String strHistory) {
        try {
            jsonArray = new JSONArray(strHistory);
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
                    //add attribute
                    maintrsactionlistEvent.setTx_hash(tx_hash);
//                    maintrsactionlistEvent.setDate(date);
                    maintrsactionlistEvent.setAmount(amount);
                    maintrsactionlistEvent.setIs_mine(is_mine);
                    maintrsactionlistEvent.setType(type);
                    maintrsactionlistEvent.setTx_status(tx_status);
                    maintrsactionlistEvents.add(maintrsactionlistEvent);
                }

                //Binder Adapter
                recy_data.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                MaindowndatalistAdapetr trsactionlistAdapter = new MaindowndatalistAdapetr(maintrsactionlistEvents);
                recy_data.setAdapter(trsactionlistAdapter);
                myDialog.dismiss();
                trsactionlistAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                        try {
                            JSONObject jsonObject = jsonArray.getJSONObject(position);
                            String type1 = jsonObject.getString("type");
                            String tx_hash1 = jsonObject.getString("tx_hash");
                            Intent intent = new Intent(MainActivity.this, TransactionDetailsActivity.class);
                            intent.putExtra("tx_hash", tx_hash1);
                            intent.putExtra("keyValue", "B");
                            intent.putExtra("listType", type1);
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

                break;
            case R.id.img_setting:
                Intent intent1 = new Intent(this, SettingActivity.class);
                startActivity(intent1);
                break;
            case R.id.textView_more:
                Intent intent2 = new Intent(this, TransactionRecordsActivity.class);
                startActivity(intent2);
                break;
            case R.id.wallet_card_bn1:
                Intent intent3 = new Intent(this, SendOne2OneMainPageActivity.class);
                startActivity(intent3);
                break;
            case R.id.wallet_card_bn2:
                Intent intent4 = new Intent(this, ReceivedPageActivity.class);
                startActivity(intent4);
                break;
            case R.id.wallet_card_bn3:
                Intent intent5 = new Intent(this, SignaturePageActivity.class);
                startActivity(intent5);
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
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
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

}