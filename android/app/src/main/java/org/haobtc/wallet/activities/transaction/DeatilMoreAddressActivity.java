package org.haobtc.wallet.activities.transaction;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.MoreAddressAdapter;
import org.haobtc.wallet.bean.CheckAddressAdapter;
import org.haobtc.wallet.bean.GetnewcreatTrsactionListBean;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeatilMoreAddressActivity extends BaseActivity {
    @BindView(R.id.img_backReceict)
    ImageView imgBackReceict;
    @BindView(R.id.recy_moreAddress)
    RecyclerView recyMoreAddress;
    private String jsondef_get;
    private List addressList;

    @Override
    public int getLayoutId() {
        return R.layout.activity_deatil_more_address;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        Intent intent = getIntent();
        jsondef_get = intent.getStringExtra("jsondef_get");
        addressList = (List) getIntent().getSerializableExtra("listdetail");
        Log.i("jsondef_get", "initView: "+ addressList);


    }

    @Override
    public void initData() {
        if (!TextUtils.isEmpty(jsondef_get)){
            Gson gson = new Gson();
            GetnewcreatTrsactionListBean getnewcreatTrsactionListBean = gson.fromJson(jsondef_get, GetnewcreatTrsactionListBean.class);
            List<GetnewcreatTrsactionListBean.OutputAddrBean> output_addr = getnewcreatTrsactionListBean.getOutputAddr();
            MoreAddressAdapter moreAddressAdapter = new MoreAddressAdapter(output_addr);
            recyMoreAddress.setAdapter(moreAddressAdapter);

        }else if (addressList!=null){
            CheckAddressAdapter checkAddressAdapter = new CheckAddressAdapter(addressList);
            recyMoreAddress.setAdapter(checkAddressAdapter);

        }

    }

    @OnClick({R.id.img_backReceict})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_backReceict:
                finish();
                break;
        }
    }
}
