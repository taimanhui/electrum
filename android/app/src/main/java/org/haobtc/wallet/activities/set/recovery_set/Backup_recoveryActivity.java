package org.haobtc.wallet.activities.set.recovery_set;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chaquo.python.PyObject;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.MessageManagerActivity;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.Daemon;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Backup_recoveryActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_keyName)
    TextView tetKeyName;
    @BindView(R.id.lin_OnckTwo)
    LinearLayout linOnckTwo;
    private String stfRecovery;

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

    @OnClick({R.id.img_back, R.id.tet_keyName, R.id.lin_OnckTwo})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_keyName:
                PyObject recovery_wallet = null;
                try {
                    recovery_wallet = Daemon.commands.callAttr("backup_wallet");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (recovery_wallet != null) {
                    stfRecovery = recovery_wallet.toString();
                    mToast(getResources().getString(R.string.backup_succse));
                    Log.i("backup_wallet", "onViewClicked: "+recovery_wallet);
                }

                mIntent(BackupMessageActivity.class);
                break;
            case R.id.lin_OnckTwo:
                Intent intent = new Intent(Backup_recoveryActivity.this,MessageManagerActivity.class);
                intent.putExtra("stfRecovery",stfRecovery);
                startActivity(intent);
                break;
        }
    }
}
