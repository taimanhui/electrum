package org.haobtc.wallet.activities.transaction;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.InputaddrScanAdapter;
import org.haobtc.wallet.adapter.MoreAddressAdapter;
import org.haobtc.wallet.adapter.OutputaddrScanAdapter;
import org.haobtc.wallet.adapter.TranscationPayAddressAdapter;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.bean.CheckAddressAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DeatilMoreAddressActivity extends BaseActivity {
    @BindView(R.id.img_backReceict)
    ImageView imgBackReceict;
    @BindView(R.id.recy_moreAddress)
    RecyclerView recyMoreAddress;
    @BindView(R.id.tv_in_tb2)
    TextView tvInTb2;
    private List jsondef_get;
    private List addressList;
    private List payAddress;
    private List jsondef_getScan;
    private List payAddressScan;

    @Override
    public int getLayoutId() {
        return R.layout.activity_deatil_more_address;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        String addressType = getIntent().getStringExtra("addressType");
        jsondef_get = (List) getIntent().getSerializableExtra("jsondef_get");
        jsondef_getScan = (List) getIntent().getSerializableExtra("jsondef_getScan");
        addressList = (List) getIntent().getSerializableExtra("listdetail");
        payAddress = (List) getIntent().getSerializableExtra("payAddress");
        payAddressScan = (List) getIntent().getSerializableExtra("payAddressScan");
        if ("pay".equals(addressType)) {
            tvInTb2.setText(getString(R.string.from));
        }
    }

    @Override
    public void initData() {
        if (jsondef_get != null) {
            MoreAddressAdapter moreAddressAdapter = new MoreAddressAdapter(jsondef_get);
            recyMoreAddress.setAdapter(moreAddressAdapter);

        } else if (jsondef_getScan != null) {
            OutputaddrScanAdapter outputaddrScanAdapter = new OutputaddrScanAdapter(jsondef_getScan);
            recyMoreAddress.setAdapter(outputaddrScanAdapter);

        } else if (addressList != null) {
            CheckAddressAdapter checkAddressAdapter = new CheckAddressAdapter(addressList);
            recyMoreAddress.setAdapter(checkAddressAdapter);

        } else if (payAddress != null) {
            TranscationPayAddressAdapter transcationPayAddressAdapter = new TranscationPayAddressAdapter(payAddress);
            recyMoreAddress.setAdapter(transcationPayAddressAdapter);
        } else if (payAddressScan != null) {
            InputaddrScanAdapter inputaddrScanAdapter = new InputaddrScanAdapter(payAddressScan);
            recyMoreAddress.setAdapter(inputaddrScanAdapter);
        }

    }

    @SingleClick
    @OnClick({R.id.img_backReceict})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_backReceict:
                finish();
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
