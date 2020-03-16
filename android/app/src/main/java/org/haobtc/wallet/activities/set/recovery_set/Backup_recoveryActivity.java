package org.haobtc.wallet.activities.set.recovery_set;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.manywallet.CustomerDialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Backup_recoveryActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_keyName)
    TextView tetKeyName;
    private String stfRecovery;
    private CustomerDialogFragment dialogFragment;
    public final static  String TAG = Backup_recoveryActivity.class.getSimpleName();

    @Override
    public int getLayoutId() {
        return R.layout.activity_backup_recovery;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_back, R.id.tet_keyName})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_keyName:
                showPopupAddCosigner1();
                break;
        }
    }

    private void showPopupAddCosigner1() {
        dialogFragment = new CustomerDialogFragment(TAG, null, "");
        dialogFragment.show(getSupportFragmentManager(), "");
    }
}
