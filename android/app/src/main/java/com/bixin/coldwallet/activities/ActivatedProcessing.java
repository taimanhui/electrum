package com.bixin.coldwallet.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bixin.coldwallet.R;

public class ActivatedProcessing extends AppCompatActivity {
    private TextView textViewConnect, textViewPIN, textViewProcess;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activing_process);
        initView();
    }

    private void initView() {
        textViewConnect = findViewById(R.id.connect_state);
        textViewPIN = findViewById(R.id.pin_setting_state);
        textViewProcess = findViewById(R.id.activate_state);
    }
}
