package org.haobtc.wallet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.gyf.immersionbar.ImmersionBar;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;

public class Send2ManyActivity extends BaseActivity implements View.OnClickListener {
    public static final String TOTAL_AMOUNT = "org.haobtc.wallet.activities.Send2ManyActivity.TOTAL";
    public static final String ADDRESS = "org.haobtc.activities.Send2ManyActivity.ADDRESS";
    private Button buttonSweep, buttonPaste, buttonNext;
    private EditText editTextAddress, editTextAmount;
    private LinearLayout buttonAdd;
    private TextView textViewTotal;
    private RecyclerView recyclerView;

    public int getLayoutId() {
        return R.layout.send_to_many;
    }

    public void initView() {
        CommonUtils.enableToolBar(this, R.string.send2many);
        buttonAdd = findViewById(R.id.lin_add_to);
        buttonSweep = findViewById(R.id.bn_sweep_2many);
        buttonPaste = findViewById(R.id.bn_paste_2many);
        buttonNext = findViewById(R.id.bn_send2many_next);
        editTextAddress = findViewById(R.id.edit_address_2many);
        editTextAmount = findViewById(R.id.edit_amount_2many);
        textViewTotal = findViewById(R.id.total);
        recyclerView = findViewById(R.id.recycler_add_to);
        buttonAdd.setOnClickListener(this);
        buttonSweep.setOnClickListener(this);
        buttonPaste.setOnClickListener(this);
        buttonNext.setOnClickListener(this);
        // recyclerView.setAdapter();
    }

    @Override
    public void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.lin_add_to:

                break;
            case R.id.bn_sweep_2many:

                break;
            case R.id.bn_paste_2many:

                break;
            case R.id.bn_send2many_next:
                Intent intent = new Intent(this, SendOne2ManyMainPageActivity.class);
                intent.putExtra(TOTAL_AMOUNT, textViewTotal.getText().toString());
                intent.putExtra(ADDRESS, "");
                startActivity(intent);
                break;
        }
    }
}
