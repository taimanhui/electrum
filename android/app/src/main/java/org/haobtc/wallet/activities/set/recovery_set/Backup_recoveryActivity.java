package org.haobtc.wallet.activities.set.recovery_set;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.manywallet.CustomerDialogFragment;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.haobtc.wallet.adapter.BixinkeyManagerAdapter;

import java.util.ArrayList;
import java.util.HashSet;
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
    private CustomerDialogFragment dialogFragment;
    public final static  String TAG = Backup_recoveryActivity.class.getSimpleName();
    private Set<String> bixinKEYlist;

    @Override
    public int getLayoutId() {
        return R.layout.activity_backup_recovery;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        Set<String> set = new HashSet<>();
        set.add("BixinKEY-ORSPR");
        SharedPreferences.Editor edit = preferences.edit();
        edit.putStringSet("BixinKEYlist",set);
        edit.apply();
        bixinKEYlist = preferences.getStringSet("BixinKEYlist", null);

    }

    @Override
    public void initData() {
        if (bixinKEYlist!=null){
            List<String> keyList = new ArrayList<String>(bixinKEYlist);
            BixinkeyManagerAdapter bixinkeyManagerAdapter = new BixinkeyManagerAdapter(keyList);
            reclCheckKey.setAdapter(bixinkeyManagerAdapter);
            bixinkeyManagerAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    Intent intent = new Intent(Backup_recoveryActivity.this, BackupMessageActivity.class);
                    intent.putExtra("strKeyname",keyList.get(position));
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
                showPopupAddCosigner1();
                break;
        }
    }

    private void showPopupAddCosigner1() {
        dialogFragment = new CustomerDialogFragment(TAG, null, "");
        dialogFragment.show(getSupportFragmentManager(), "");
    }
}
