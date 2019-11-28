package org.haobtc.wallet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.haobtc.wallet.R;

public class WalletUnActivatedActivity extends AppCompatActivity {
    Button buttonActivate;
    public static final String TAG = "org.haobtc.coldwallet.activities.WalletUnActivatedActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activate);
        initView();
    }

    private void initView() {
        buttonActivate = findViewById(R.id.button_activate);
        buttonActivate.setOnClickListener(v -> {
            Intent intent = new Intent(this, TouchHardwareActivity.class);
            intent.putExtra(TouchHardwareActivity.FROM, TAG);
            startActivity(intent);
        });
    }
}
