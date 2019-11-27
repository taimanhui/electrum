package com.haobtc.coldwallet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.haobtc.coldwallet.R;

public class CreateWalletActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {
   private boolean checked;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_wallet);
        initView();
    }

    private void initView() {
        Button button_create_wallet, button_import_wallet;
        RadioGroup radioButtonAg = findViewById(R.id.radio_group_button_ag);
        button_create_wallet = findViewById(R.id.bn_create_wallet);
        button_import_wallet = findViewById(R.id.bn_import_wallet);
        radioButtonAg.setOnCheckedChangeListener(this);
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
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        checked = true;
    }
}
