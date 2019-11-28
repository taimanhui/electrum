package org.haobtc.wallet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.haobtc.wallet.R;
import org.haobtc.wallet.fragment.ItemFragmentTransaction;

public class TransactionDetailsActivity extends AppCompatActivity {
    private Button buttonSignature;
    public static final String TAG = "org.haobtc.wallet.activities.TransactionDetailsActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.trans_details);
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_trans_detail) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
        }
        initView();

    }

    private void initView() {
        TextView textView = this.findViewById(R.id.tv_in_tb2);
        textView.setText(R.string.trans_details);
        // my_child_toolbar is defined in the layout file
        this.setSupportActionBar(this.findViewById(R.id.tb2));

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = this.getSupportActionBar();
        // Enable the Up button
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
        ItemFragmentTransaction fragmentTransaction = new ItemFragmentTransaction();
        fragmentTransaction.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_trans_detail, fragmentTransaction).commit();
        buttonSignature = findViewById(R.id.sig_trans);
        buttonSignature.setOnClickListener(v -> {
            Intent intent = new Intent(this, PinSettingActivity.class);
            intent.putExtra(TouchHardwareActivity.FROM, TAG);
            startActivity(intent);
        });

    }
}
