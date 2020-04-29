package org.haobtc.wallet.activities;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.okhttp.Request;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.aop.SingleClick;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;

public class VerificationKEYActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;

    @Override
    public int getLayoutId() {
        return CommunicationModeSelector.isNFC ? R.layout.processing_nfc : R.layout.processing_ble;

    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        // 设置沉浸式状态栏
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        Intent intent = getIntent();
        String strVerification = intent.getStringExtra("strVerification");
        Log.i("strVerification", "initView: " + strVerification);
    }

    @Override
    public void initData() {
//        verification();

    }

    private void verification() {
        HashMap<String, String> pramas = new HashMap<>();
        pramas.put("serialno", "");
        pramas.put("signature", "");
        OkHttpUtils.post().url("https://key.bixin.com/lengqian.bo/")
                .params(pramas)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Request request, Exception e) {
                        Log.i("onError", "onError: ---- "+request);
                    }

                    @Override
                    public void onResponse(String response) {
                        Log.i("onResponse", "onResponse:------- "+response);
                    }
                });
    }

    @SingleClick
    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
//                Intent intent = new Intent(VerificationKEYActivity.this, VerificationSuccessActivity.class);
//                startActivity(intent);
                break;

        }
    }

}
