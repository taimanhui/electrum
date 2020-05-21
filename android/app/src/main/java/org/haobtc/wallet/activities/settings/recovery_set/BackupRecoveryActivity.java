package org.haobtc.wallet.activities.settings.recovery_set;

import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.common.base.Strings;
import com.google.gson.Gson;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.activities.settings.BixinKEYManageActivity;
import org.haobtc.wallet.activities.settings.HardwareDetailsActivity;
import org.haobtc.wallet.adapter.BixinkeyManagerAdapter;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.bean.HardwareFeatures;

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
    private List<HardwareFeatures> deviceValue;
    private SharedPreferences.Editor edit;
    private SharedPreferences devices;

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

    @Override
    protected void onResume() {
        super.onResume();
        deviceValue = new ArrayList<>();
        devices = getSharedPreferences("devices", MODE_PRIVATE);
        edit = devices.edit();
        Map<String, ?> devicesAll = devices.getAll();
        //key
        for (Map.Entry<String, ?> entry : devicesAll.entrySet()) {
            String mapValue = (String) entry.getValue();
            HardwareFeatures hardwareFeatures = new Gson().fromJson(mapValue, HardwareFeatures.class);
            String backupMessage = hardwareFeatures.getBackupMessage();
            if (!TextUtils.isEmpty(backupMessage)){
                deviceValue.add(hardwareFeatures);
            }
        }
        if (deviceValue != null) {
            BixinkeyManagerAdapter bixinkeyManagerAdapter = new BixinkeyManagerAdapter(deviceValue);
            reclCheckKey.setAdapter(bixinkeyManagerAdapter);
            bixinkeyManagerAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                @SingleClick
                @Override
                public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                    switch (view.getId()) {
                        case R.id.relativeLayout_bixinkey:
                            Intent intent = new Intent(BackupRecoveryActivity.this, BackupMessageActivity.class);
                            intent.putExtra("label", Strings.isNullOrEmpty(deviceValue.get(position).getLabel()) ? deviceValue.get(position).getBleName(): deviceValue.get(position).getLabel());
                            intent.putExtra("message",deviceValue.get(position).getBackupMessage());
                            intent.putExtra("tag","recovery");
                            startActivity(intent);
                            break;
                        case R.id.linear_delete:
                            String blename = deviceValue.get(position).getBleName();
                            edit.remove(blename);
                            edit.apply();
                            deviceValue.remove(position);
                            bixinkeyManagerAdapter.notifyItemChanged(position);
                            bixinkeyManagerAdapter.notifyDataSetChanged();
                            mToast(getString(R.string.delete_succse));
                            break;
                    }
                }
            });
        }
    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.tet_keyName})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_keyName:
                Intent intent = new Intent(this, CommunicationModeSelector.class);
                intent.putExtra("tag", TAG);
                startActivity(intent);
                break;
        }
    }

}
