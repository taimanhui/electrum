package org.haobtc.wallet.activities.settings;

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

import java.util.ArrayList;
import java.util.List;
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

    @Override
    public int getLayoutId() {
        return R.layout.activity_bixin_keymenage;
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
            reclBixinKeyList.setAdapter(bixinkeyManagerAdapter);
            bixinkeyManagerAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    String firmwareVersion = "V" + deviceValue.get(position).getMajorVersion() + "." + deviceValue.get(position).getMinorVersion() + "." + deviceValue.get(position).getPatchVersion();
                    Intent intent = new Intent(BixinKEYManageActivity.this, HardwareDetailsActivity.class);
                    intent.putExtra("bleName", deviceValue.get(position).getBleName());
                    intent.putExtra("firmwareVersion", firmwareVersion);
                    intent.putExtra("bleVerson", "V" + deviceValue.get(position).getMajorVersion() + "." + deviceValue.get(position).getPatchVersion());
                    intent.putExtra("device_id", deviceValue.get(position).getDeviceId());
                    startActivity(intent);
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







