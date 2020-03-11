package org.haobtc.wallet.activities.set.recovery_set;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.chaquo.python.PyObject;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.manywallet.CustomerDialogFragment;
import org.haobtc.wallet.utils.Daemon;
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

//                PyObject recovery_wallet = null;
//                try {
//                    recovery_wallet = Daemon.commands.callAttr("backup_wallet");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                if (recovery_wallet != null) {
//                    stfRecovery = recovery_wallet.toString();
//                    mToast(getResources().getString(R.string.backup_succse));
//                    Log.i("backup_wallet", "onViewClicked: "+recovery_wallet);
//                }
//
//                mIntent(BackupMessageActivity.class);
                break;
        }
    }

    private void showPopupAddCosigner1() {
        dialogFragment = new CustomerDialogFragment("", null, "");
        dialogFragment.show(getSupportFragmentManager(), "");
    }
}
