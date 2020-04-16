package org.haobtc.wallet.activities.settings;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.BixinkeyManagerAdapter;
import org.haobtc.wallet.bean.HardwareFeatures;
import org.haobtc.wallet.utils.Daemon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BixinKEYManageActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.recl_bixinKey_list)
    RecyclerView reclBixinKeyList;
    private List<HardwareFeatures> deviceValue;
    private SharedPreferences.Editor edit;

    @Override
    public int getLayoutId() {
        return R.layout.activity_bixin_keymenage;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void initView() {
        ButterKnife.bind(this);
        deviceValue = new ArrayList<>();
        SharedPreferences devices = getSharedPreferences("devices", MODE_PRIVATE);
        edit = devices.edit();
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
            reclBixinKeyList.setAdapter(bixinkeyManagerAdapter);
            bixinkeyManagerAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                @Override
                public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                    switch (view.getId()) {
                        case R.id.relativeLayout_bixinkey:
                            String firmwareVersion = "V" + deviceValue.get(position).getMajorVersion() + "." + deviceValue.get(position).getMinorVersion() + "." + deviceValue.get(position).getPatchVersion();
                            Intent intent = new Intent(BixinKEYManageActivity.this, HardwareDetailsActivity.class);
                            intent.putExtra("bleName", deviceValue.get(position).getBleName());
                            intent.putExtra("firmwareVersion", firmwareVersion);
                            intent.putExtra("bleVerson", "V" + deviceValue.get(position).getMajorVersion() + "." + deviceValue.get(position).getPatchVersion());
                            intent.putExtra("device_id", deviceValue.get(position).getDeviceId());
                            startActivity(intent);
                            break;
                        case R.id.linear_delete:
                            String key_deviceId = deviceValue.get(position).getDeviceId();
                            edit.remove(key_deviceId);
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

    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        if (view.getId() == R.id.img_back) {
            finish();
        }
    }
}







