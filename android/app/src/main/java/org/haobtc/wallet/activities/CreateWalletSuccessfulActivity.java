package org.haobtc.wallet.activities;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.google.gson.Gson;
import com.yzq.zxinglibrary.encode.CodeCreator;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.MainActivity;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.bean.GetCodeAddressBean;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.utils.Daemon;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateWalletSuccessfulActivity extends BaseActivity {
    @BindView(R.id.img_code_public_key)
    ImageView imgCodePublicKey;
    @BindView(R.id.text_public_key)
    TextView textPublicKey;
    @BindView(R.id.copy_public_key)
    TextView copyPublicKey;
    @BindView(R.id.btn_enter_wallet)
    Button btnEnterWallet;
    @BindView(R.id.img_back)
    ImageView imgBack;

    private SharedPreferences preferences;
    private final String FIRST_RUN = "is_first_run";
    private SharedPreferences.Editor edit;
    private String strCode;
    private PyObject walletAddressShowUi;

    @Override
    public int getLayoutId() {
        return R.layout.crete_successful;
    }


    public void initView() {
        ButterKnife.bind(CreateWalletSuccessfulActivity.this);
        preferences = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        edit = preferences.edit();


    }

    @Override
    public void initData() {
        //Generate QR code
        mGeneratecode();

    }

    private void mGeneratecode() {
        try {
            walletAddressShowUi = Daemon.commands.callAttr("get_wallet_address_show_UI");

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (walletAddressShowUi!=null){
            strCode = walletAddressShowUi.toString();
            Gson gson = new Gson();
            GetCodeAddressBean getCodeAddressBean = gson.fromJson(strCode, GetCodeAddressBean.class);
            String qr_data = getCodeAddressBean.getQr_data();
            String addr = getCodeAddressBean.getAddr();
            textPublicKey.setText(addr);
            Bitmap bitmap = CodeCreator.createQRCode(qr_data, 248, 248, null);
            imgCodePublicKey.setImageBitmap(bitmap);
        }
    }

    @OnClick({R.id.copy_public_key, R.id.btn_enter_wallet,R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.copy_public_key:
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // The text is placed on the system clipboard.
                cm.setText(textPublicKey.getText());
                Toast.makeText(CreateWalletSuccessfulActivity.this, R.string.copysuccess, Toast.LENGTH_LONG).show();
                break;
            case R.id.btn_enter_wallet:
                edit.putBoolean(FIRST_RUN, true);
                edit.apply();
                EventBus.getDefault().post(new FirstEvent("11"));
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);

                break;
            case R.id.img_back:
                finish();
                break;
        }
    }

}
