package org.haobtc.wallet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.utils.CommonUtils;

public class Send2ManyActivity extends AppCompatActivity {
    public static final String TOTAL_AMOUNT = "org.haobtc.coldwallet.activities.Send2ManyActivity.TOTAL";
    public static final String ADDRESS = "org.haobtc.coldwallet.activities.Send2ManyActivity.ADDRESS";
    private Button buttonSweep, buttonPaste, buttonAdd, buttonNext;
    private EditText editTextAddress, editTextAmount;
    private TextView textViewTotal;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_to_many);
        initView();
    }

    private void initView() {
        CommonUtils.enableToolBar(this, R.string.send2many);
        buttonAdd = findViewById(R.id.bn_add_to);
        buttonSweep = findViewById(R.id.bn_sweep_2many);
        buttonPaste = findViewById(R.id.bn_paste_2many);
        buttonNext = findViewById(R.id.bn_send2many_next);
        editTextAddress = findViewById(R.id.edit_address_2many);
        editTextAmount = findViewById(R.id.edit_amount_2many);
        textViewTotal = findViewById(R.id.total);
        buttonAdd.setOnClickListener(v -> {

        });

        buttonSweep.setOnClickListener(v -> {

        });
        buttonPaste.setOnClickListener(v -> {

        });
        buttonNext.setOnClickListener(v -> {
            Intent intent = new Intent(this, SendOne2ManyMainPageActivity.class);
            intent.putExtra(TOTAL_AMOUNT, textViewTotal.getText().toString());
            intent.putExtra(ADDRESS, "");
            startActivity(intent);

        });
        recyclerView = findViewById(R.id.recycler_add_to);
       // recyclerView.setAdapter();



    }
}
