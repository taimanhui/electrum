package org.haobtc.wallet.activities.set.recovery_set;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.manywallet.CustomerDialogFragment;
import org.haobtc.wallet.adapter.BixinkeyManagerAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class Backup_recoveryActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_keyName)
    TextView tetKeyName;
    @BindView(R.id.reclCheckKey)
    RecyclerView reclCheckKey;
    private String stfRecovery;
    private Set<String> backupBixinKEYlist;

    @Override
    public int getLayoutId() {
        return R.layout.activity_backup_recovery;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        backupBixinKEYlist = preferences.getStringSet("backupBixinKEYlist", null);

    }

    @Override
    public void initData() {
        if (backupBixinKEYlist != null) {
            List<String> keyList = new ArrayList<String>(backupBixinKEYlist);
            for (int i = 0; i < keyList.size(); i++) {
                Log.d("fetching values", "fetch value " + keyList.get(i));
            }
            BixinkeyManagerAdapter bixinkeyManagerAdapter = new BixinkeyManagerAdapter(keyList);
            reclCheckKey.setAdapter(bixinkeyManagerAdapter);

            bixinkeyManagerAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    Intent intent = new Intent(Backup_recoveryActivity.this, BackupMessageActivity.class);
                    intent.putExtra("strKeyname", keyList.get(position));
                    intent.putExtra("flagWhere","Backup");
                    startActivity(intent);
                }
            });
        }
    }

    @OnClick({R.id.img_back, R.id.tet_keyName})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_keyName:
                // new version code
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
//                    Log.i("backup_wallet", "onViewClicked: " + recovery_wallet);
//                }
//                mIntent(BackupMessageActivity.class);
                break;
        }
    }

    private void showPopupAddCosigner1() {
        CustomerDialogFragment dialogFragment = new CustomerDialogFragment("", null, "");
        dialogFragment.show(getSupportFragmentManager(), "");
    }

}
