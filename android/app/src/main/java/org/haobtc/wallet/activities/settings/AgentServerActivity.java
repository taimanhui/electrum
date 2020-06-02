package org.haobtc.wallet.activities.settings;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.ChoosePayAddressAdapetr;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.bean.AddressEvent;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.utils.Daemon;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AgentServerActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.switchAgent)
    Switch switchAgent;
    @BindView(R.id.btnConfirm)
    Button btnConfirm;
    @BindView(R.id.editAgentIP)
    EditText editAgentIP;
    @BindView(R.id.editPort)
    EditText editPort;
    @BindView(R.id.editUsername)
    EditText editUsername;
    @BindView(R.id.editPass)
    EditText editPass;
    @BindView(R.id.testNodeType)
    TextView testNodeType;
    private ArrayList<AddressEvent> dataList;
    private String nodetype;
    private String strAgentIP;
    private String strPort;
    private String strUsername;
    private String strPass;
    private String strNodetype;
    private SharedPreferences preferences;
    private SharedPreferences.Editor edit;
    private String set_proxy;

    @Override
    public int getLayoutId() {
        return R.layout.activity_agent_server;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void initView() {
        ButterKnife.bind(this);
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        set_proxy = preferences.getString("set_proxy", "");
        TextChange textChange = new TextChange();
        editAgentIP.addTextChangedListener(textChange);
        editPort.addTextChangedListener(textChange);
        editUsername.addTextChangedListener(textChange);
        editPass.addTextChangedListener(textChange);
        switchAgent.setOnCheckedChangeListener(this);

        inits();

    }

    private void inits() {
        if (!TextUtils.isEmpty(set_proxy)) {
            String[] wordsList = set_proxy.split(" ");
            switch (wordsList.length) {
                case 5:
                    editPass.setText(wordsList[4]);
                case 4:
                    editUsername.setText(wordsList[3]);
                case 3:
                    testNodeType.setText(wordsList[0]);
                    editAgentIP.setText(wordsList[1]);
                    editPort.setText(wordsList[2]);

            }
        }
    }

    @Override
    public void initData() {
        dataList = new ArrayList<>();
        dataList.add(new AddressEvent("socks4"));
        dataList.add(new AddressEvent("socks5"));

    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.relNodeType, R.id.btnConfirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.relNodeType:
                showDialogs(AgentServerActivity.this, R.layout.selectwallet_item);
                break;
            case R.id.btnConfirm:
                setAgentServer();

                break;
        }
    }

    private void setAgentServer() {
        strNodetype = testNodeType.getText().toString();
        strAgentIP = editAgentIP.getText().toString();
        strPort = editPort.getText().toString();
        strUsername = editUsername.getText().toString();
        strPass = editPass.getText().toString();
        try {
            Daemon.commands.callAttr("set_proxy", strNodetype, strAgentIP, strPort, strUsername, strPass);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        edit.putString("set_proxy", strNodetype + " " + strAgentIP + " " + strPort + " " + strUsername + " " + strPass);
        edit.apply();
        EventBus.getDefault().post(new FirstEvent("set_proxy"));
        mToast(getString(R.string.set_finish));

    }

    private void showDialogs(Context context, @LayoutRes int resource) {
        //set see view
        View view = View.inflate(context, resource, null);
        Dialog dialogBtom = new Dialog(context, R.style.dialog);
        RecyclerView recyPayaddress = view.findViewById(R.id.recy_payAdress);
        Log.i("jianxoiain", "dataList: " + dataList.get(0));
        ChoosePayAddressAdapetr choosePayAddressAdapetr = new ChoosePayAddressAdapetr(AgentServerActivity.this, dataList);
        recyPayaddress.setAdapter(choosePayAddressAdapetr);
        choosePayAddressAdapetr.setmOnItemClickListener(new ChoosePayAddressAdapetr.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                nodetype = dataList.get(position).getName();
            }
        });
        //cancel dialog
        view.findViewById(R.id.cancel_select_wallet).setOnClickListener(v -> {
            dialogBtom.cancel();
        });
        view.findViewById(R.id.bn_select_wallet).setOnClickListener(v -> {
            testNodeType.setText(nodetype);
            //judge button status
            buttonColorStatus();
            dialogBtom.cancel();

        });
        dialogBtom.setContentView(view);
        Window window = dialogBtom.getWindow();
        //set pop_up size
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        //set locate
        window.setGravity(Gravity.BOTTOM);
        //set animal
        window.setWindowAnimations(R.style.AnimBottom);
        dialogBtom.show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            editAgentIP.setEnabled(true);
            editPort.setEnabled(true);
            editUsername.setEnabled(true);
            editPass.setEnabled(true);
            if (!TextUtils.isEmpty(set_proxy)) {
                String[] wordsList = set_proxy.split(" ");
                switch (wordsList.length) {
                    case 5:
                        editPass.setText(wordsList[4]);
                    case 4:
                        editUsername.setText(wordsList[3]);
                    case 3:
                        testNodeType.setText(wordsList[0]);
                        editAgentIP.setText(wordsList[1]);
                        editPort.setText(wordsList[2]);
                }
            }

        } else {
            testNodeType.setText("");
            editAgentIP.setText("");
            editPort.setText("");
            editUsername.setText("");
            editPass.setText("");
            editAgentIP.setEnabled(false);
            editPort.setEnabled(false);
            editUsername.setEnabled(false);
            editPass.setEnabled(false);
            closeAgentServer();
        }
    }

    private void closeAgentServer() {
        try {
            Daemon.commands.callAttr("set_proxy", "", "", "", "", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        strNodetype = testNodeType.getText().toString();
        strAgentIP = editAgentIP.getText().toString();
        strPort = editPort.getText().toString();
        strUsername = editUsername.getText().toString();
        strPass = editPass.getText().toString();

        if (TextUtils.isEmpty(strAgentIP) || TextUtils.isEmpty(strPort) || TextUtils.isEmpty(strNodetype)) {
            btnConfirm.setEnabled(false);
            btnConfirm.setBackground(getDrawable(R.drawable.little_radio_qian));

        } else {
            btnConfirm.setEnabled(true);
            btnConfirm.setBackground(getDrawable(R.drawable.little_radio_blue));

        }

    }

}
