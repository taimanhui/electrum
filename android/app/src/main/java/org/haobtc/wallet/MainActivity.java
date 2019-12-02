package org.haobtc.wallet;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import org.haobtc.wallet.entries.DummyContent;
import org.haobtc.wallet.activities.GuideActivity;
import org.haobtc.wallet.adapter.MyPagerAdapter;
import org.haobtc.wallet.activities.ReceivedPageActivity;
import org.haobtc.wallet.activities.SendOne2OneMainPageActivity;
import org.haobtc.wallet.activities.SettingActivity;
import org.haobtc.wallet.activities.SignaturePageActivity;
import org.haobtc.wallet.activities.TransactionRecordsActivity;
import org.haobtc.wallet.adapter.MyItemRecyclerViewAdapterTransaction;
import com.chaquo.python.PyObject;

import java.util.ArrayList;
import java.util.List;
import java.lang.String;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import org.haobtc.wallet.utils.Global;
import org.haobtc.wallet.utils.Daemon;

public class MainActivity extends AppCompatActivity {
    private ImageView imageViewSweep, imageViewSetting;
    private TextView textView;
    private RecyclerView recyclerView;
    private List<DummyContent.DummyItem> dummyItems = new ArrayList<>();
    private List<View> viewList = new ArrayList<>();//ViewPager数据源
    private MyPagerAdapter myPagerAdapter;//适配器
    private ViewPager viewPager;
    private Button button_send, button_receive, button_signature;
    private final String FIRST_RUN = "is_first_run";
    SharedPreferences sharedPreferences;
    private static Daemon daemonModel;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        sharedPreferences = getSharedPreferences("state", Context.MODE_PRIVATE);
        super.onCreate(savedInstanceState);

        //test python code
        Global.app = this.getApplication();
        Python.start(new AndroidPlatform(Global.app));
        Global.py = Python.getInstance();
        Global.py.getModule("electrum.constants").callAttr("set_testnet");

        Global.mHandler = null;
        if (Global.mHandler == null) {
            Global.mHandler = new Handler(Looper.getMainLooper());
        }

        Global.guiDaemon = Global.py.getModule("electrum_gui.android.daemon");
        Global.guiConsole = Global.py.getModule("electrum_gui.android.console");
        initDaemon();

        {
            String name = "hahahahhahh222";
            String password = "111111";
            int m = 2;
            int n = 2;
            String xpub1 ="Vpub5gLTnhnQig7SLNhWCqE2AHqt8zhJGQwuwEAKQE67bndddSzUMAmab7DxZF9b9wynVyY2URM61SWY67QYaPV6oQrB41vMKQbeHveRvuThAmm";
            String xpub2 ="Vpub5gyCX33B53xAyfEaH1Jfnp5grizbHfxVz6bWLPD92nLcbKMsQzSbM2eyGiK4qiRziuoRhoeVMoPLvEdfbQxGp88PN9cU6zupSSuiPi3RjEg";

//            //create wallet
//            //daemonModel.commands.callAttr("delete_wallet", name);
//            daemonModel.commands.callAttr("set_multi_wallet_info", name, m, n);
//            daemonModel.commands.callAttr("add_xpub", xpub1);
//            daemonModel.commands.callAttr("add_xpub", xpub2);
//            List<PyObject> info= daemonModel.commands.callAttr("get_keystores_info").asList();
//            System.out.println("main.kt onCreate keystors .....========" + info.toString());
//            daemonModel.commands.callAttr("create_multi_wallet", name);
            //daemonModel.commands.callAttr("get_xpub_from_hw")

            //load_wallet
            daemonModel.commands.callAttr("load_wallet", name, password);
            daemonModel.commands.callAttr("select_wallet", name);

            String wallet_str = daemonModel.commands.callAttr("get_wallets_list_info").toString();
            System.out.println("main.kt onCreate wallet info is  .....======================" + wallet_str);

            daemonModel.commands.callAttr("mktx", "", "", "0.001");
        }

        // main page
        /*if (sharedPreferences.getBoolean(FIRST_RUN, false)) {
            setContentView(R.layout.main_activity);
            initView();
        } else {*/
        // guide page
            initGuide();
       /* }*/

    }

    private static void initDaemon(){
        Global.guiDaemon.callAttr("set_excepthook", Global.mHandler);
        daemonModel = new Daemon();
    }

    private void initGuide() {
        Intent intent = new Intent(this, GuideActivity.class);
        startActivity(intent);
        sharedPreferences.edit().putBoolean(FIRST_RUN, true).apply();
    }

    private void initView() {

        imageViewSweep = findViewById(R.id.sweep);
        imageViewSetting = findViewById(R.id.setting);
        textView = findViewById(R.id.textView_more);
        imageViewSweep.setOnClickListener(v -> {

            // todo;扫码
        });
        imageViewSetting.setOnClickListener(v -> {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        });
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(this, TransactionRecordsActivity.class);
            startActivity(intent);
        });

        RecyclerView.Adapter adapter = new MyItemRecyclerViewAdapterTransaction(dummyItems, item -> {

        });
        recyclerView = findViewById(R.id.recycler);
        recyclerView.setAdapter(adapter);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.wallet_card, null);
        button_send = view.findViewById(R.id.wallet_card_bn1);
        button_receive = view.findViewById(R.id.wallet_card_bn2);
        button_signature = view.findViewById(R.id.wallet_card_bn3);
        button_send.setOnClickListener(v -> {
            Intent intent = new Intent(this, SendOne2OneMainPageActivity.class);
            startActivity(intent);
        });
        button_receive.setOnClickListener(v -> {
            Intent intent = new Intent(this, ReceivedPageActivity.class);
            startActivity(intent);
        });
        button_signature.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignaturePageActivity.class);
            startActivity(intent);
        });
        View view1 = inflater.inflate(R.layout.wallet_card_add,null);
        List<View> viewList = new ArrayList<>();
        viewList.add(view);
        viewList.add(view1);
        myPagerAdapter = new MyPagerAdapter(viewList);
        viewPager = findViewById(R.id.wallet_card_vp);
        viewPager.setAdapter(myPagerAdapter);
        int sizeInPixel = this.getResources().getDimensionPixelSize(R.dimen.layout_margin);
        viewPager.setPageMargin(sizeInPixel);
    }
    /**
     *该方法封装了添加页面的代码逻辑实现，参数text为要展示的数据
     */
    public void addPage(String text){
        LayoutInflater inflater = LayoutInflater.from(this);//获取LayoutInflater的实例
        View view = inflater.inflate(R.layout.wallet_card, null);//调用LayoutInflater实例的inflate()方法来加载页面的布局

        TextView textView = view.findViewById(R.id.wallet_card);//获取该View对象的TextView实例
        textView.setText(text);//展示数据

        viewList.add(view);//为数据源添加一项数据
        myPagerAdapter.notifyDataSetChanged();//通知UI更新
    }
    /**
     * 删除当前页面
     */
    public void delPage(){
        int position = viewPager.getCurrentItem();//获取当前页面位置
        viewList.remove(position);//删除一项数据源中的数据
        myPagerAdapter.notifyDataSetChanged();//通知UI更新

    }

}
