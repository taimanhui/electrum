package org.haobtc.wallet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.azhon.appupdate.config.UpdateConfiguration;
import com.azhon.appupdate.listener.OnButtonClickListener;
import com.azhon.appupdate.listener.OnDownloadListener;
import com.azhon.appupdate.manager.DownloadManager;
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
import org.haobtc.wallet.activities.SendOne2OneMainPageActivity;
import org.haobtc.wallet.activities.SettingActivity;
import org.haobtc.wallet.activities.TransactionDetailsActivity;
import org.haobtc.wallet.activities.TransactionRecordsActivity;
import org.haobtc.wallet.activities.base.ApplicationObserver;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.MaindowndatalistAdapetr;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.bean.AddressEvent;
import org.haobtc.wallet.bean.MainSweepcodeBean;
import org.haobtc.wallet.bean.MaintrsactionlistEvent;
import org.haobtc.wallet.bean.UpdateInfo;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.event.FixWalletNameEvent;
import org.haobtc.wallet.event.MainpageWalletEvent;
import org.haobtc.wallet.event.SecondEvent;
import org.haobtc.wallet.fragment.mainwheel.AddViewFragment;
import org.haobtc.wallet.fragment.mainwheel.CheckHideWalletFragment;
import org.haobtc.wallet.fragment.mainwheel.WheelViewpagerFragment;
import org.haobtc.wallet.utils.Daemon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.executorService;


public class MainActivity extends BaseActivity implements View.OnClickListener, OnRefreshListener, OnButtonClickListener, OnDownloadListener {

    private ViewPager viewPager;
    SharedPreferences sharedPreferences;
    public static Python py;
    private RecyclerView recy_data;
    //remeber first back time
    private long firstTime = 0;
    private TextView tetNone;
    private ArrayList<MaintrsactionlistEvent> maintrsactionlistEvents;
    private String date;
    private boolean is_mine;
    private JSONArray jsonArray;
    private RxPermissions rxPermissions;
    private static final int REQUEST_CODE = 0;
    private List<Fragment> fragmentList;
    private MaindowndatalistAdapetr trsactionlistAdapter;
    private String strNames;
    private SmartRefreshLayout refreshLayout;
    private ArrayList<AddressEvent> walletnameList;
    private String strType;
    private int scrollPos = 0;//scrollPos --> recyclerview position != The last one || second to last
    PyObject get_wallets_list_info = null;
    private DownloadManager manager;
    private SharedPreferences.Editor edit;

    @Override
    public int getLayoutId() {
        return R.layout.main_activity;
    }

    @Override
    public void initView() {
        //Eventbus register
        EventBus.getDefault().register(this);
        sharedPreferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = sharedPreferences.edit();

        //FIRST_RUN,if frist run
        String FIRST_RUN = "is_first_run";
        edit.putBoolean(FIRST_RUN, true);
        edit.apply();
        init();
    }

    private void init() {
        boolean user_agreement = sharedPreferences.getBoolean("user_agreement", false);
        if (!user_agreement) {
            //User agreement dialog
            userAgreementDialog();
        }
        rxPermissions = new RxPermissions(this);
        ImageView imageViewSweep = findViewById(R.id.img_sweep);
        TextView btnAddmoney = findViewById(R.id.tet_Addmoney);
        viewPager = findViewById(R.id.viewPager);
        recy_data = findViewById(R.id.recy_data);
        ImageView imageViewSetting = findViewById(R.id.img_setting);
        TextView textView = findViewById(R.id.textView_more);
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

    @Override
    public void initData() {
        maintrsactionlistEvents = new ArrayList<>();
        fragmentList = new ArrayList<>();
        //Binder Adapter
        trsactionlistAdapter = new MaindowndatalistAdapetr(maintrsactionlistEvents);
        recy_data.setAdapter(trsactionlistAdapter);
        //Rolling Wallet
        mWheelplanting();
        if (ApplicationObserver.tryUpdate) {
            ApplicationObserver.tryUpdate = false;
            getUpdateInfo();
        }
    }

    private void mWheelplanting() {
        walletnameList.clear();
        fragmentList.clear();
        //wallet list
        if (Daemon.commands == null) {
            finishAndRemoveTask();
            System.exit(0);
        }
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                //wallet list
                try {
                    get_wallets_list_info = Daemon.commands.callAttr("list_wallets");
                } catch (Exception e) {
                    e.printStackTrace();
                    addwalletFragment();
                    return;
                }
                mainhandler.sendEmptyMessage(1);
            }
        });
    }

    private void showWalletList() {
        if (get_wallets_list_info != null && get_wallets_list_info.size() != 0) {
            String toStrings = get_wallets_list_info.toString();
            Log.i("mWheelplanting", "toStrings: " + toStrings);
            if (toStrings.length() != 2) {
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
                Log.i("mWheelplanting", "mWheelplanting: " + walletnameList);
                if (walletnameList != null && walletnameList.size() != 0) {
                    strNames = walletnameList.get(0).getName();
                    strType = walletnameList.get(0).getType();
                    for (int i = 0; i < walletnameList.size(); i++) {
                        String name = walletnameList.get(i).getName();
                        String walletType = walletnameList.get(i).getType();
                        if (i == 0) {
                            fragmentList.add(new WheelViewpagerFragment(name, walletType, true));
                        } else {
                            fragmentList.add(new WheelViewpagerFragment(name, walletType));
                        }
                    }
                    fragmentList.add(new AddViewFragment());
                    fragmentList.add(new CheckHideWalletFragment());
                    viewPager.setOffscreenPageLimit(4);
                    viewPager.setPageMargin(40);
                    viewPager.setAdapter(new ViewPagerFragmentStateAdapter(getSupportFragmentManager(), fragmentList));
                } else {
                    addwalletFragment();
                }
            } else {
                addwalletFragment();
            }
        }
        //scroll
        viewPagerScroll(walletnameList.size());
    }

    //no wallet show fragment
    private void addwalletFragment() {
        fragmentList.add(new AddViewFragment());
        fragmentList.add(new CheckHideWalletFragment());
        viewPager.setOffscreenPageLimit(4);
        viewPager.setPageMargin(40);
        viewPager.setAdapter(new ViewPagerFragmentStateAdapter(getSupportFragmentManager(), fragmentList));
        //trsaction list data
        tetNone.setText(getString(R.string.no_records));
        tetNone.setVisibility(View.VISIBLE);
        recy_data.setVisibility(View.GONE);
    }

    //viewPagerScroll
    private void viewPagerScroll(int size) {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                scrollPos = position;
                if (position == (fragmentList.size() - 1) || position == (fragmentList.size() - 2)) {
                    refreshLayout.setEnableRefresh(false);
                    if (position == (fragmentList.size() - 1)) {
                        tetNone.setText(getString(R.string.hide_wallet_tips));
                        tetNone.setVisibility(View.VISIBLE);
                        recy_data.setVisibility(View.GONE);
                    } else {
                        tetNone.setText(getString(R.string.no_records));
                        tetNone.setVisibility(View.VISIBLE);
                        recy_data.setVisibility(View.GONE);
                    }

                } else {
                    refreshLayout.setEnableRefresh(true);
                    strNames = walletnameList.get(position).getName();
                    strType = walletnameList.get(position).getType();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //refresh only wallet
                            ((WheelViewpagerFragment) fragmentList.get(position)).refreshList();
                        }
                    }, 500);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void downMainListdata() {
        maintrsactionlistEvents.clear();
        PyObject get_history_tx = null;
        try {
            //get transaction json
            get_history_tx = Daemon.commands.callAttr("get_all_tx_list");
        } catch (Exception e) {
            e.printStackTrace();
            refreshLayout.finishRefresh();
            tetNone.setText(getString(R.string.no_records));
            tetNone.setVisibility(View.VISIBLE);
            recy_data.setVisibility(View.GONE);
            Log.i("downMainListdata", "downMaina===: " + e.getMessage());
            return;
        }
        //get transaction list
        if (get_history_tx != null) {
            tetNone.setVisibility(View.GONE);
            recy_data.setVisibility(View.VISIBLE);
            String strHistory = get_history_tx.toString();
            // Log.i("strHistory", "onPage----: " + strHistory);
            refreshLayout.finishRefresh();
            if (strHistory.length() == 2) {
                tetNone.setText(getString(R.string.no_records));
                tetNone.setVisibility(View.VISIBLE);
                recy_data.setVisibility(View.GONE);
            } else {
                //show trsaction list
                showTrsactionlist(strHistory);
            }
        } else {
            refreshLayout.finishRefresh();
            tetNone.setText(getString(R.string.no_records));
            tetNone.setVisibility(View.VISIBLE);
            recy_data.setVisibility(View.GONE);
        }
        trsactionlistAdapter.notifyDataSetChanged();

    }

    //show trsaction list
    private void showTrsactionlist(String strHistory) {
        Log.i("strHistory", "showTrsactionlist---: " + strHistory);
        maintrsactionlistEvents.clear();
        try {
            jsonArray = new JSONArray(strHistory);
            // Log.i("showTrsactionlist", "showTrsactionlist========: " + strHistory);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                MaintrsactionlistEvent maintrsactionlistEvent = new MaintrsactionlistEvent();
                String type = jsonObject.getString("type");
                String tx_hash = jsonObject.getString("tx_hash");
                String amount = jsonObject.getString("amount");
                is_mine = jsonObject.getBoolean("is_mine");//false ->get   true ->push
                date = jsonObject.getString("date");
                String tx_status = jsonObject.getString("tx_status");
                if (type.equals("history")) {
                    String confirmations = jsonObject.getString("confirmations");
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
            trsactionlistAdapter.notifyDataSetChanged();
            trsactionlistAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                private String tx_hash1;
                private boolean status = false;

                @SingleClick
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
                                Log.i("onItemChildClick", "onItemCh==== " + tx_hash1);
                                PyObject get_remove_flag = Daemon.commands.callAttr("get_remove_flag", tx_hash1);
                                status = get_remove_flag.toBoolean();

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (status) {
                                try {
                                    Daemon.commands.callAttr("remove_local_tx", tx_hash1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                maintrsactionlistEvents.remove(position);
//                                trsactionlistAdapter.notifyItemChanged(position);
//                                //trsaction list data
//                                downMainListdata();
                            } else {
                                mToast(getString(R.string.delete_unBroad));
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

    @SingleClick(value = 1000)
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
                intent2.putExtra("strwalletType", strType);
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
        switch (msgVote) {
            case "11":
                //Rolling Wallet
                mWheelplanting();
                break;
            case "22":
                if (scrollPos != (fragmentList.size() - 1) && scrollPos != (fragmentList.size() - 2)) {//scrollPos --> recyclerview position != The last one || second to last
                    maintrsactionlistEvents.clear();
                    //trsaction list data
                    downMainListdata();
                    trsactionlistAdapter.notifyDataSetChanged();
                }
                break;
            case "33":
                tetNone.setText(getString(R.string.no_records));
                tetNone.setVisibility(View.VISIBLE);
                recy_data.setVisibility(View.GONE);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(SecondEvent updataHint) {
        String msgVote = updataHint.getMsg();
        if (!TextUtils.isEmpty(msgVote) && msgVote.length() != 2 && msgVote.contains("{")) {
            //Rolling Wallet
            if (fragmentList != null && fragmentList.size() > scrollPos) {
                Optional.ofNullable(fragmentList.get(scrollPos)).ifPresent(fragment -> ((WheelViewpagerFragment) fragment).setValue(msgVote));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(MainpageWalletEvent updataHint) {
        String status = updataHint.getStatus();
        if (status.equals("22")) {
            int walletPos = updataHint.getPos();
            viewPager.setCurrentItem(walletPos);
            Log.i("viewPagernihao", "event:------ " + walletPos);
        }
    }

    private void getUpdateInfo() {
        // version_testnet.json version_regtest.json
        String appId = BuildConfig.APPLICATION_ID;
        String urlPrefix = "https://key.bixin.com/";
        String url = "";
        if (appId.endsWith("mainnet")) {
            url = urlPrefix + "version.json";
        } else if (appId.endsWith("testnet")) {
            url = urlPrefix + "version_testnet.json";
        } else if (appId.endsWith("regnet")) {
            url = urlPrefix + "version_regtest.json";
        }
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        Log.d("Main", "正在检查更新信息");
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Main", "获取更新信息失败");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                assert response.body() != null;
                SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
                String locate = preferences.getString("language", "");
                String info = response.body().string();
                UpdateInfo updateInfo = UpdateInfo.objectFromData(info);
                String oldInfo = preferences.getString("upgrade_info", null);
                Optional.ofNullable(oldInfo).ifPresent((s) -> {
                    UpdateInfo old = UpdateInfo.objectFromData(s);
                    if (!old.getStm32().getUrl().equals(updateInfo.getStm32().getUrl())) {
                        updateInfo.getStm32().setNeedUpload(true);
                    }
                    if (!old.getNrf().getUrl().equals(updateInfo.getNrf().getUrl())) {
                        updateInfo.getNrf().setNeedUpload(true);
                    }
                });
                preferences.edit().putString("upgrade_info", updateInfo.toString()).apply();
                String url = updateInfo.getAPK().getUrl();
                String versionName = updateInfo.getAPK().getVersionName();
                int versionCode = updateInfo.getAPK().getVersionCode();
                String size = updateInfo.getAPK().getSize().replace("M", "");
                String description = "English".equals(locate) ? updateInfo.getAPK().getChangelogEn() : updateInfo.getAPK().getChangelogCn();
                runOnUiThread(() -> attemptUpdate(url, versionName, versionCode, size, description));
            }
        });
    }

    private void attemptUpdate(String uri, String versionName, int versionCode, String size, String description) {
        String url = "https://key.bixin.com/" + uri;
        UpdateConfiguration configuration = new UpdateConfiguration()
                .setEnableLog(true)
                //.setHttpManager()
                .setJumpInstallPage(true)
                .setDialogButtonTextColor(Color.WHITE)
                .setDialogButtonColor(getColor(R.color.button_bk))
                .setDialogImage(R.drawable.update)
                .setShowNotification(true)
                .setShowBgdToast(true)
                .setForcedUpgrade(false)
                .setButtonClickListener(this)
                .setOnDownloadListener(this);

        manager = DownloadManager.getInstance(this);
        manager.setApkName("BixinKEY.apk")
                .setApkUrl(url)
                .setSmallIcon(R.drawable.app_icon)
                .setShowNewerToast(false)
                .setConfiguration(configuration)
                .setApkVersionCode(versionCode)
                .setApkVersionName(versionName)
                .setApkSize(size)
                .setApkDescription(description)
//                .setApkMD5("DC501F04BBAA458C9DC33008EFED5E7F")
                .download();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            manager.release();
        }
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Scan QR code / barcode return
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null) {
                Log.i("PyObject", "parse_qr-----:  " + data);
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                //bitcoin:mhZ5dTc91TxttEvFJifBNPNqwLAD5CxhYF
                if (!TextUtils.isEmpty(content)) {
                    PyObject parse_qr;
                    try {
                        parse_qr = Daemon.commands.callAttr("parse_pr", content);

                    } catch (Exception e) {
                        e.printStackTrace();
                        mToast(getString(R.string.address_wrong));
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
                                String sendAmount = listData.getAmount();
                                String message = listData.getMessage();
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
                                intent.putExtra("strwalletType", strType);
                                startActivity(intent);
                            } else {
                                mToast(getString(R.string.address_wrong));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            mToast(getString(R.string.address_wrong));
                        }
                    }
                }
            }
        }
    }

    private void userAgreementDialog() {
        View view1 = LayoutInflater.from(this).inflate(R.layout.user_agreement, null, false);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view1).create();
        Objects.requireNonNull(alertDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        view1.findViewById(R.id.btn_agree).setOnClickListener(v -> {
            edit.putBoolean("user_agreement", true);
            edit.apply();
            alertDialog.dismiss();
        });
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void fixName(FixWalletNameEvent event) {
        //Rolling Wallet
        mWheelplanting();
    }


    @Override
    public void onRefresh(@NonNull RefreshLayout refreshLayout) {
        //trsaction list data
        downMainListdata();
    }

    @SuppressLint("HandlerLeak")
    Handler mainhandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                showWalletList();
            }
        }
    };

    @Override
    public void onButtonClick(int id) {

    }

    @Override
    public void start() {

    }

    @Override
    public void downloading(int max, int progress) {

    }

    @Override
    public void done(File apk) {
        manager.release();
    }

    @Override
    public void cancel() {
        manager.release();
    }

    @Override
    public void error(Exception e) {
        manager.release();
    }
}