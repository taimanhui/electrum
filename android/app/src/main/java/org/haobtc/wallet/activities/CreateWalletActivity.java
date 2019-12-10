package org.haobtc.wallet.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;


import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.set.AgreementActivity;

public class CreateWalletActivity extends BaseActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private boolean checked = true;
    //remeber first back time
    private long firstTime = 0;
    private TextView tetAgreement;
    private SharedPreferences preferences;
    private SharedPreferences.Editor edit;


    @Override
    public int getLayoutId() {
        return R.layout.create_wallet;
    }

    @Override
    public void initView() {
        preferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        edit = preferences.edit();
        edit.putBoolean("JumpOr", false);
        edit.apply();
        Button button_create_wallet, button_import_wallet;
        tetAgreement = findViewById(R.id.tet_Agreement);
        CheckBox radioButtonAg = findViewById(R.id.radio_group_button_ag);
        button_create_wallet = findViewById(R.id.bn_create_wallet);
        button_import_wallet = findViewById(R.id.bn_import_wallet);
        radioButtonAg.setOnCheckedChangeListener(this);
        tetAgreement.setOnClickListener(this);
        button_create_wallet.setOnClickListener(v -> {
            if (checked) {
                Intent intent = new Intent(this, CreateWalletPageActivity.class);
                startActivity(intent);
            }

        });
        button_import_wallet.setOnClickListener(v -> {
            if (checked) {
                Intent intent = new Intent(this, ImportWalletPageActivity.class);
                startActivity(intent);
            }

        });

    }

    @Override
    public void initData() {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tet_Agreement:
                Intent intent = new Intent(this, AgreementActivity.class);
                startActivity(intent);
                break;
            case R.id.bn_create_wallet:
                Intent intent1 = new Intent(this, CreateWalletPageActivity.class);
                startActivity(intent1);
                break;
            case R.id.bn_import_wallet:
                Intent intent2 = new Intent(this, ImportWalletPageActivity.class);
                startActivity(intent2);
                break;

        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            checked = true;
        } else {
            checked = false;
        }
    }
}
