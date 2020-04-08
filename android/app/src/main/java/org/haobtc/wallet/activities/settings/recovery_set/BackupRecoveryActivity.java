package org.haobtc.wallet.activities.settings.recovery_set;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector;
import org.haobtc.wallet.adapter.BixinkeyManagerAdapter;
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

    @Override
    public int getLayoutId() {
        return R.layout.activity_backup_recovery;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        deviceValue = new ArrayList<>();
        SharedPreferences devices = getSharedPreferences("devices", MODE_PRIVATE);
        Map<String, ?> devicesAll = devices.getAll();
        //key
        for (Map.Entry<String, ?> entry : devicesAll.entrySet()) {
            String mapValue = (String) entry.getValue();
            HardwareFeatures hardwareFeatures = new Gson().fromJson(mapValue, HardwareFeatures.class);
            deviceValue.add(hardwareFeatures);
        }
    }

    @Override
    public void initData() {
        if (deviceValue != null) {
            BixinkeyManagerAdapter bixinkeyManagerAdapter = new BixinkeyManagerAdapter(deviceValue);
            reclCheckKey.setAdapter(bixinkeyManagerAdapter);
            bixinkeyManagerAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    Intent intent = new Intent(BackupRecoveryActivity.this, BackupMessageActivity.class);
                    intent.putExtra("strKeyname",deviceValue.get(position).getBleName());
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
        CommunicationModeSelector dialogFragment = new CommunicationModeSelector(TAG, null, "");
        dialogFragment.show(getSupportFragmentManager(), "");
    }
}
