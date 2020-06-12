package org.haobtc.wallet.activities.settings.recovery_set;

import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.MainActivity;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.adapter.BixinkeyBackupAdapter;
import org.haobtc.wallet.adapter.BixinkeyManagerAdapter;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.event.ExistEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BackupRecoveryActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_keyName)
    TextView tetKeyName;
    @BindView(R.id.reclCheckKey)
    RecyclerView reclCheckKey;
    public final static  String TAG = BackupRecoveryActivity.class.getSimpleName();
    private List<String> deviceValue;
    private SharedPreferences.Editor edit;
    private String activeSetPIN;

    @Override
    public int getLayoutId() {
        return R.layout.activity_backup_recovery;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        activeSetPIN = getIntent().getStringExtra("ActiveSetPIN");
        if ("ActiveSetPIN".equals(activeSetPIN)) {
            Intent intent = new Intent(this, CommunicationModeSelector.class);
            intent.putExtra("tag", TAG);
            startActivity(intent);
        }
    }

    @Override
    public void initData() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        deviceValue = new ArrayList<>();
        SharedPreferences backup = getSharedPreferences("backup", MODE_PRIVATE);
        edit = backup.edit();
        Map<String, ?> backAll = backup.getAll();
        //key
        for (Map.Entry<String, ?> entry : backAll.entrySet()) {
            String mapValue = (String) entry.getValue();
            if (!TextUtils.isEmpty(mapValue)){
                deviceValue.add(entry.getKey() + ":" + mapValue);
            }
        }
        if (deviceValue != null) {
            BixinkeyBackupAdapter backupAdapter = new BixinkeyBackupAdapter(deviceValue);
            reclCheckKey.setAdapter(backupAdapter);
            backupAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                @SingleClick
                @Override
                public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                    switch (view.getId()) {
                        case R.id.relativeLayout_bixinkey:
                            Intent intent = new Intent(BackupRecoveryActivity.this, BackupMessageActivity.class);
                            intent.putExtra("label", deviceValue.get(position).split(":", 3)[1]);
                            intent.putExtra("message",deviceValue.get(position).split(":", 3)[2]);
                            intent.putExtra("tag","recovery");
                            startActivity(intent);
                            break;
                        case R.id.linear_delete:
                            String deviceID = deviceValue.get(position).split(":", 3)[0];
                            edit.remove(deviceID).apply();
                            deviceValue.remove(position);
                            backupAdapter.notifyItemChanged(position);
                            backupAdapter.notifyDataSetChanged();
                            mToast(getString(R.string.delete_succse));
                            break;
                    }
                }
            });
        }
    }

    @SingleClick(value = 2000)
    @OnClick({R.id.img_back, R.id.tet_keyName})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                if ("ActiveSetPIN".equals(activeSetPIN)) {
                    mIntent(MainActivity.class);
                } else {
                    finish();
                }
                break;
            case R.id.tet_keyName:
                Intent intent = new Intent(this, CommunicationModeSelector.class);
                intent.putExtra("tag", TAG);
                startActivity(intent);
                break;
        }
    }

}
