package org.haobtc.wallet.activities.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.chaquo.python.PyObject;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.ElectrumListAdapter;
import org.haobtc.wallet.bean.CNYBean;
import org.haobtc.wallet.event.SendMoreAddressEvent;
import org.haobtc.wallet.utils.Daemon;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AnyskServerSetActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.switch_cynchronez)
    Switch switchCynchronez;
    @BindView(R.id.tet_electrumName)
    TextView reclNodeChose;
    @BindView(R.id.btn_add_server)
    Button btnAddServer;
    @BindView(R.id.editAgentIP)
    EditText editAgentIP;
    @BindView(R.id.editPort)
    EditText editPort;
    private SharedPreferences preferences;
    private SharedPreferences.Editor edit;
    private boolean open = false;
    private String strAgentIP;
    private String strPort;

    @Override
    public int getLayoutId() {
        return R.layout.activity_anysk_server_set;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void initView() {
        ButterKnife.bind(this);
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        inits();

    }

    @SuppressLint("SetTextI18n")
    private void inits() {
        TextChange textChange = new TextChange();
        editAgentIP.addTextChangedListener(textChange);
        editPort.addTextChangedListener(textChange);
        String strAgentIP = preferences.getString("strAgentIP", "");
        String strPort = preferences.getString("strPort", "");
        if (!TextUtils.isEmpty(strAgentIP)){
            editAgentIP.setText(strAgentIP);
            editPort.setText(strPort);
        }else{
            editAgentIP.setText("39.105.86.163");
            editPort.setText("8080");
        }

        //synchronize server
        boolean set_syn_server = preferences.getBoolean("set_syn_server", false);
        if (set_syn_server) {
            open = true;
            switchCynchronez.setChecked(true);
        } else {
            open = false;
            switchCynchronez.setChecked(false);
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
                    open = true;
                    try {
                        Daemon.commands.callAttr("set_syn_server", true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    edit.putBoolean("set_syn_server", true);
                    edit.apply();
                    mToast(getString(R.string.set_success));
                } else {
                    open = false;
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

    @SuppressLint("SetTextI18n")
    @OnClick({R.id.img_back, R.id.btn_add_server, R.id.tet_electrumName})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_add_server:
//                Intent intent = new Intent(AnyskServerSetActivity.this, EditAnyskServerActivity.class);
//                intent.putExtra("edit_server", "editServer");
//                startActivity(intent);
                addAnyskServer();

                break;
            case R.id.tet_electrumName:
                editAgentIP.setText("39.105.86.163");
                editPort.setText("8080");
                break;
        }
    }

    private void addAnyskServer() {
        strAgentIP = editAgentIP.getText().toString();
        strPort = editPort.getText().toString();
        try {
            Daemon.commands.callAttr("set_sync_server_host", strAgentIP, strPort);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        edit.putString("strAgentIP",strAgentIP);
        edit.putString("strPort",strPort);
        edit.apply();
        mToast(getString(R.string.add_success));
    }

    class TextChange implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            //judge button status
            buttonColorStatus();
        }
    }

    //judge button status
    private void buttonColorStatus() {
        strAgentIP = editAgentIP.getText().toString();
        strPort = editPort.getText().toString();

        if (TextUtils.isEmpty(strAgentIP) || TextUtils.isEmpty(strPort)) {
            btnAddServer.setEnabled(false);
            btnAddServer.setBackground(getDrawable(R.drawable.little_radio_qian));

        } else {
            if (open) {
                btnAddServer.setEnabled(true);
                btnAddServer.setBackground(getDrawable(R.drawable.little_radio_blue));
            } else {
                btnAddServer.setEnabled(false);
                btnAddServer.setBackground(getDrawable(R.drawable.little_radio_qian));
            }

        }

    }
}
