package org.haobtc.wallet;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

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
import org.haobtc.wallet.activities.TransactionRecordsActivity;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.MaindowndatalistAdapetr;
import org.haobtc.wallet.adapter.MyPagerAdapter;
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

import org.haobtc.wallet.utils.Global;

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

    @Override
    public int getLayoutId() {
        return R.layout.main_activity;
    }


    @Override
    public void initView() {
        sharedPreferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        //Eventbus register
        EventBus.getDefault().register(this);
        if (sharedPreferences.getBoolean(FIRST_RUN, false)) {
            init();

        } else {
            boolean jumpOr = sharedPreferences.getBoolean("JumpOr", true);
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
        viewPager = findViewById(R.id.viewPager);
        btnAddmoney = findViewById(R.id.tet_Addmoney);
        recy_data = findViewById(R.id.recy_data);
        imageViewSetting = findViewById(R.id.img_setting);
        textView = findViewById(R.id.textView_more);
        tetNone = findViewById(R.id.tet_None);
        imageViewSweep.setOnClickListener(this);
        imageViewSetting.setOnClickListener(this);
        textView.setOnClickListener(this);
        btnAddmoney.setOnClickListener(this);

        dataListName = new ArrayList<>();
        //Rolling Wallet
        mWheelplanting();
        //Lower list data
        mTransactionrecord();
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


    }

    private void mTransactionrecord() {
        ArrayList<String> dataList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            dataList.add("ahiedjlmk32lk2n42nk44" + i);
        }
        MaindowndatalistAdapetr myItemRecyclerViewAdapterTransaction = new MaindowndatalistAdapetr(dataList);
        recy_data.setAdapter(myItemRecyclerViewAdapterTransaction);

    }

    private void mWheelplanting() {
        List<Fragment> fragmentList = new ArrayList<>();
        myDialog.show();
        PyObject get_wallets_list_info = Daemon.commands.callAttr("get_wallets_list_info");
        String toString = get_wallets_list_info.toString();
        Log.i("javaBean", "mjavaBean----: " + toString + "----lenth----" + toString.length());
        if (!TextUtils.isEmpty(toString)) {
            try {
                JSONArray jsonArray = new JSONArray(toString);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String wallet_type = jsonObject.getString("wallet_type");
                    String balance = jsonObject.getString("balance");
                    String name = jsonObject.getString("name");
                    dataListName.add(name);
                    String streplace = wallet_type.replaceAll("of", "/");
                    fragmentList.add(new WheelViewpagerFragment(name, streplace, balance));

                }
                dataListName.add("");
                fragmentList.add(new AddViewFragment());
                viewPager.setOffscreenPageLimit(3);
                viewPager.setPageMargin(40);
                viewPager.setAdapter(new ViewPagerFragmentStateAdapter(getSupportFragmentManager(), fragmentList));
                //choose wallet
                Daemon.commands.callAttr("load_wallet", dataListName.get(0));
                Daemon.commands.callAttr("select_wallet", dataListName.get(0));
                //get transaction json
                get_history_tx = Daemon.commands.callAttr("get_history_tx");
                //get transaction list
                if (get_history_tx != null) {
                    tetNone.setVisibility(View.GONE);
                    recy_data.setVisibility(View.VISIBLE);
                    if (!get_history_tx.isEmpty()) {
                        String strHistory = get_history_tx.toString();
                        Log.i("strHistory", "onPage----: " + strHistory);
                    } else {
                        Toast.makeText(MainActivity.this, "get_history_tx.isEmpty()", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    tetNone.setVisibility(View.VISIBLE);
                    recy_data.setVisibility(View.GONE);
                }

                myDialog.dismiss();

                //scroll
                viewPagerScroll();


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    //viewPagerScroll
    private void viewPagerScroll() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (mCurrentPosition != position) {
                    String strNames = dataListName.get(position);
                    if (!TextUtils.isEmpty(strNames)) {
                        Daemon.commands.callAttr("load_wallet", strNames);
                        Daemon.commands.callAttr("select_wallet", strNames);
                        get_history_tx = Daemon.commands.callAttr("get_history_tx");
                        if (get_history_tx != null) {
                            tetNone.setVisibility(View.GONE);
                            recy_data.setVisibility(View.VISIBLE);
                            if (!get_history_tx.isEmpty()) {
                                String strHistory = get_history_tx.toString();
                                Log.i("strHistory", "onPage----: " + strHistory);
                            } else {
                                Toast.makeText(MainActivity.this, "get_history_tx.isEmpty()", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            tetNone.setVisibility(View.VISIBLE);
                            recy_data.setVisibility(View.GONE);
                        }

                    }

                }
                mCurrentPosition = position;

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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