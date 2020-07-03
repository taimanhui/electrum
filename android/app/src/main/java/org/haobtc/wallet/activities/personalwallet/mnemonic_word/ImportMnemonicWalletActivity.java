package org.haobtc.wallet.activities.personalwallet.mnemonic_word;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ImportMnemonicWalletActivity extends BaseActivity {

    @BindView(R.id.recl_mnemonic)
    RecyclerView reclMnemonic;

    @Override
    public int getLayoutId() {
        return R.layout.activity_import_mnemonic_wallet;

    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        String strNewseed = getIntent().getStringExtra("strNewseed");

//        Intent intent = new Intent(ImportMnemonicWalletActivity.this, CreateHelpWordWalletActivity.class);
//        intent.putExtra("strNewseed", newSeed);
//        startActivity(intent);

    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
        }
    }
}
