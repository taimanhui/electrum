package org.haobtc.wallet.activities;

import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.google.gson.Gson;
import com.yzq.zxinglibrary.encode.CodeCreator;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.bean.GetCodeAddressBean;
import org.haobtc.wallet.utils.CommonUtils;
import org.haobtc.wallet.utils.Daemon;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReceivedPageActivity extends BaseActivity {

    @BindView(R.id.imageView2)
    ImageView imageView2;
    @BindView(R.id.textView5)
    TextView textView5;
    @BindView(R.id.textView6)
    TextView textView6;
    @BindView(R.id.button)
    Button button;

    @Override
    public int getLayoutId() {
        return R.layout.address_info;
    }

    @Override
    public void initView() {
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        CommonUtils.enableToolBar(this, R.string.receive);
    }

    @Override
    public void initData() {
        //Generate QR code
        mGeneratecode();


    }

    private void mGeneratecode() {
        PyObject walletAddressShowUi = Daemon.commands.callAttr("get_wallet_address_show_UI");
        String strCode = walletAddressShowUi.toString();
        Log.i("strCode", "mGenerate--: " + strCode);
        Gson gson = new Gson();
        GetCodeAddressBean getCodeAddressBean = gson.fromJson(strCode, GetCodeAddressBean.class);
        String qr_data = getCodeAddressBean.getQr_data();
        String addr = getCodeAddressBean.getAddr();
        textView5.setText(addr);
        Bitmap bitmap = CodeCreator.createQRCode(qr_data, 248, 248, null);
        imageView2.setImageBitmap(bitmap);
    }


    @OnClick({R.id.textView6, R.id.button})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.textView6:
                //copy text
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // The text is placed on the system clipboard.
                cm.setText(textView5.getText());
                Toast.makeText(ReceivedPageActivity.this, R.string.copysuccess, Toast.LENGTH_LONG).show();

                break;
            case R.id.button:
                break;
        }
    }
}

