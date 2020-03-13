package org.haobtc.wallet.activities.onlywallet;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.manywallet.CustomerDialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ImportHistoryWalletActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.create_trans_one2one)
    Button createTransOne2one;
    private CustomerDialogFragment dialogFragment;

    @Override
    public int getLayoutId() {
        return R.layout.activity_import_history_wallet;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_back, R.id.create_trans_one2one})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.create_trans_one2one:
                // new version code
                showPopupAddCosigner1();
                

                break;
        }
    }

    private void showPopupAddCosigner1() {
        dialogFragment = new CustomerDialogFragment("", null, "");
        dialogFragment.show(getSupportFragmentManager(), "");
    }
}
