package org.haobtc.wallet.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.chaquo.python.PyObject;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.ElectrumListAdapter;
import org.haobtc.wallet.utils.Daemon;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.jessyan.autosize.utils.ScreenUtils;

public class ServerSettingActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.switch_cynchronez)
    Switch switchCynchronez;
    @BindView(R.id.recl_nodeChose)
    RecyclerView reclNodeChose;
    @BindView(R.id.tet_addNode)
    TextView tetAddNode;
    private PyObject get_server_list;
    private ArrayList<String> electrumList;

    public int getLayoutId() {
        return R.layout.server_setting;
    }

    public void initView() {
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {
        electrumList = new ArrayList<>();
        //get electrum list
        getElectrumData();

    }
    //get electrum list
    private void getElectrumData() {
        try {
            get_server_list = Daemon.commands.callAttr("get_server_list");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (get_server_list!=null){
            String get_server = get_server_list.toString();
            Map<String,Object> jsonToMap = JSONObject.parseObject(get_server);
            Set<String> keySets = jsonToMap.keySet();
            Iterator<String> ki = keySets.iterator();
            while(ki.hasNext()){
                String k = ki.next();
                //获取前面的key
                String v = jsonToMap.get(k).toString();
                Map<String,Object> vToMap = JSONObject.parseObject(v);
                Set<String> vkeySets = vToMap.keySet();
                Iterator<String> vki = vkeySets.iterator();
                while(vki.hasNext()) {
                    String vk = vki.next();
                    if("s".equals(vk)) {
                        String vvalue = vToMap.get(vk).toString();
                        String strElectrum = k+":"+vvalue;
                        electrumList.add(strElectrum);
                    }
                }
            }
            ElectrumListAdapter electrumListAdapter = new ElectrumListAdapter(electrumList);
            reclNodeChose.setAdapter(electrumListAdapter);

        }
    }

    @OnClick({R.id.img_back, R.id.tet_addNode})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_addNode:
                dialogAddnode();
                break;
        }
    }

    private void dialogAddnode() {
        View view = LayoutInflater.from(this).inflate(R.layout.add_node_layout, null, false);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view).create();
        ImageView img_Cancle = view.findViewById(R.id.cancel_select_wallet);
        img_Cancle.setOnClickListener(v -> {
            alertDialog.dismiss();
        });
        alertDialog.show();

    }

}
