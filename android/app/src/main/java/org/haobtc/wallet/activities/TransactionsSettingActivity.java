package org.haobtc.wallet.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.set.recovery_set.RecoverySetActivity;
import org.haobtc.wallet.utils.Daemon;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TransactionsSettingActivity extends BaseActivity {


    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_addNode)
    TextView tetAddNode;
    @BindView(R.id.switch_rbf)
    Switch switchRbf;
    @BindView(R.id.switch_noConfirm)
    Switch switchNoConfirm;
    @BindView(R.id.switch_find)
    Switch switchFind;
    private SharedPreferences.Editor edit;
    private SharedPreferences preferences;

    public int getLayoutId() {
        return R.layout.transaction_setting;
    }

    public void initView() {
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        init();
    }

    private void init() {
        boolean set_use_change = preferences.getBoolean("set_use_change", false);
        boolean set_unconf = preferences.getBoolean("set_unconf", true);
        if (set_unconf){
            switchNoConfirm.setChecked(true);
        }else{
            switchNoConfirm.setChecked(false);
        }
        if (set_use_change){
            switchRbf.setChecked(true);
        }else{
            switchRbf.setChecked(false);
        }
    }

    @Override
    public void initData() {
        switchChoose();

    }

    private void switchChoose() {
        //pay unConfirmed income
        switchNoConfirm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    try {
                        Daemon.commands.callAttr("set_unconf",true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    edit.putBoolean("set_unconf",true);
                    edit.apply();
                    mToast(getResources().getString(R.string.set_success));
                }else{
                    try {
                        Daemon.commands.callAttr("set_unconf",false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    edit.putBoolean("set_unconf",false);
                    edit.apply();
                }
            }
        });
        //use Give change adrress
        switchFind.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    try {
                        Daemon.commands.callAttr("set_use_change",true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    edit.putBoolean("set_use_change",true);
                    edit.apply();
                    mToast(getResources().getString(R.string.set_success));
                }else{
                    try {
                        Daemon.commands.callAttr("set_use_change",false);
                    } catch (Exception e) {
                        Log.e("Exception", "Exception++: "+e.getMessage());
                        e.printStackTrace();
                    }
                    edit.putBoolean("set_use_change",false);
                    edit.apply();
                }
            }
        });

    }


    @OnClick({R.id.img_back, R.id.tet_addNode})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_addNode:
                mIntent(RecoverySetActivity.class);
                break;
        }
    }

}
