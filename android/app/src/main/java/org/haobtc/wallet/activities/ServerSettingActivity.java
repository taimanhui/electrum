package org.haobtc.wallet.activities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.chaquo.python.PyObject;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.settings.AgentServerActivity;
import org.haobtc.wallet.activities.settings.BlockChooseActivity;
import org.haobtc.wallet.activities.settings.ElectrumNodeChooseActivity;
import org.haobtc.wallet.activities.settings.QuotationServerActivity;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.bean.CNYBean;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.utils.Daemon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ServerSettingActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.switch_cynchronez)
    Switch switchCynchronez;
    @BindView(R.id.rel_quotationChoose)
    RelativeLayout relQuotationChoose;
    @BindView(R.id.rel_blockChoose)
    RelativeLayout relBlockChoose;
    @BindView(R.id.rel_Electrum_Choose)
    RelativeLayout relElectrumChoose;
    @BindView(R.id.tet_defaultServer)
    TextView tetDefaultServer;
    @BindView(R.id.testBlockcheck)
    TextView testBlockcheck;
    @BindView(R.id.testElectrumNode)
    TextView testElectrumNode;
    private SharedPreferences.Editor edit;
    private SharedPreferences preferences;
    private String exchangeName;
    private String blockServerLine;
    private String electrumTest;
    private PyObject get_server_list;
    private ArrayList<CNYBean> electrumList;

    public int getLayoutId() {
        return R.layout.server_setting;
    }

    @SuppressLint("CommitPrefEdits")
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        exchangeName = preferences.getString("exchangeName", "");
        blockServerLine = preferences.getString("blockServerLine", "");
        electrumTest = preferences.getString("electrumTest", "");
        edit = preferences.edit();
        inits();

    }

    private void inits() {
        electrumList = new ArrayList<>();
        //synchronize server
        boolean set_syn_server = preferences.getBoolean("set_syn_server", false);
        if (set_syn_server) {
            switchCynchronez.setChecked(true);
        } else {
            switchCynchronez.setChecked(false);
        }
        //get default Server
        if (!TextUtils.isEmpty(exchangeName)) {
            tetDefaultServer.setText(exchangeName);
        } else {
            getdefaultServer();
        }
        //get block Browser
        if (!TextUtils.isEmpty(blockServerLine)) {
            testBlockcheck.setText(blockServerLine);
        }
        //get electrum node
        if (!TextUtils.isEmpty(electrumTest)) {
            testElectrumNode.setText(electrumTest);
        }else{
            //get electrum list
            getElectrumData();
        }

    }
    private void getElectrumData() {
        try {
            get_server_list = Daemon.commands.callAttr("get_server_list");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (get_server_list != null) {
            String get_server = get_server_list.toString();
            Log.i("get_server_list", "get_server_list: "+get_server);
            Map<String, Object> jsonToMap = JSONObject.parseObject(get_server);
            Set<String> keySets = jsonToMap.keySet();
            for (String k : keySets) {
                //get key
                String v = jsonToMap.get(k).toString();
                Map<String, Object> vToMap = JSONObject.parseObject(v);
                Set<String> vkeySets = vToMap.keySet();
                for (String vk : vkeySets) {
                    if ("s".equals(vk)) {
                        String vvalue = vToMap.get(vk).toString();
                        String strElectrum = k + ":" + vvalue;
                        CNYBean cnyBean = new CNYBean(strElectrum, false);
                        electrumList.add(cnyBean);
                    }
                }
            }
            testElectrumNode.setText(electrumList.get(0).getName());

        }
    }

    @Override
    public void initData() {
        switchCyn();

    }

    private void switchCyn() {
        switchCynchronez.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    try {
                        Daemon.commands.callAttr("set_syn_server", true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    edit.putBoolean("set_syn_server", true);
                    edit.apply();
                    mToast(getString(R.string.set_success));
                } else {
                    try {
                        Daemon.commands.callAttr("set_syn_server", false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    edit.putBoolean("set_syn_server", false);
                    edit.apply();
                }
            }
        });
    }

    //get default Server
    private void getdefaultServer() {
        PyObject get_exchanges = null;
        try {
            get_exchanges = Daemon.commands.callAttr("get_exchanges");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (get_exchanges != null) {
            Log.i("get_exchanges", "getExchangelist: " + get_exchanges);
            List<PyObject> pyObjects = get_exchanges.asList();
            String defalutServer = pyObjects.get(0).toString();
            tetDefaultServer.setText(defalutServer);
        }
    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.rel_quotationChoose, R.id.rel_blockChoose, R.id.rel_Electrum_Choose, R.id.relAgent_Choose})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.rel_quotationChoose:
                mIntent(QuotationServerActivity.class);
                break;
            case R.id.rel_blockChoose:
                mIntent(BlockChooseActivity.class);
                break;
            case R.id.rel_Electrum_Choose:
                mIntent(ElectrumNodeChooseActivity.class);
                break;
            case R.id.relAgent_Choose:
                mIntent(AgentServerActivity.class);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(FirstEvent updataHint) {
        String msgVote = updataHint.getMsg();
        if (msgVote.equals("defaultServer")) {
            exchangeName = preferences.getString("exchangeName", "");
            tetDefaultServer.setText(exchangeName);
        }else if (msgVote.equals("block_check")){
            blockServerLine = preferences.getString("blockServerLine", "");
            testBlockcheck.setText(blockServerLine);
        }else if (msgVote.equals("changeElectrumNode")){
            electrumTest = preferences.getString("electrumTest", "");
            testElectrumNode.setText(electrumTest);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
