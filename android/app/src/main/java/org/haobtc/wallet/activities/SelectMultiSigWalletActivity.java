package org.haobtc.wallet.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.haobtc.wallet.R;
import org.haobtc.wallet.utils.CommonUtils;

public class SelectMultiSigWalletActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_mutisig_wallet);
        initView();
    }

    private void initView() {
        CommonUtils.enableToolBar(this, R.string.select_mutisig);


    }
}
