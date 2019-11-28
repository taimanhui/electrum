package org.haobtc.wallet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;

import org.haobtc.wallet.R;
import org.haobtc.wallet.utils.CommonUtils;

public class SignaturePageActivity extends AboutActivity {
    private Button buttonImport, buttonSweep, buttonPaste, buttonConfirm;
    private EditText editTextRaw;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parse_raw_trans);
        initView();

    }

    private void initView() {
        CommonUtils.enableToolBar(this, R.string.signature);
        buttonConfirm = findViewById(R.id.confirm_sig);
        buttonImport = findViewById(R.id.import_file);
        buttonSweep = findViewById(R.id.sweep_sig);
        buttonPaste = findViewById(R.id.paste_sig);
        editTextRaw = findViewById(R.id.edit_raw);
        buttonConfirm.setOnClickListener(v -> {
           String raw =  editTextRaw.getText().toString();
            Intent intent = new Intent(this, TransactionDetailsActivity.class);
            startActivity(intent);
        });
        buttonImport.setOnClickListener(v -> {

        });
        buttonSweep.setOnClickListener(v -> {

        });
        buttonPaste.setOnClickListener(v -> {

        });


    }
}
