package org.haobtc.wallet.activities.onlywallet;

import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;

import com.chaquo.python.PyObject;
import com.google.gson.Gson;
import com.yzq.zxinglibrary.encode.CodeCreator;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.ReceivedPageActivity;
import org.haobtc.wallet.activities.SendOne2OneMainPageActivity;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.ChoosePayAddressAdapetr;
import org.haobtc.wallet.bean.GetCodeAddressBean;
import org.haobtc.wallet.bean.MainNewWalletBean;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.utils.Daemon;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CheckWalletDetailActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_Name)
    TextView tetName;
    @BindView(R.id.img_Code)
    ImageView imgCode;
    @BindView(R.id.tet_getBTC)
    TextView tetGetBTC;
    @BindView(R.id.tet_Copy)
    TextView tetCopy;
    @BindView(R.id.tet_deleteWallet)
    TextView tetDeleteWallet;
    private String wallet_name;
    private Dialog dialogBtom;
    private PyObject delete_wallet;

    @Override
    public int getLayoutId() {
        return R.layout.activity_check_wallet_detail;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        Intent intent = getIntent();
        wallet_name = intent.getStringExtra("wallet_name");
        tetName.setText(wallet_name);

    }

    @Override
    public void initData() {
        //Generate QR code
        mGeneratecode();

    }

    private void mGeneratecode() {
        PyObject walletAddressShowUi = null;
        try {
            walletAddressShowUi = Daemon.commands.callAttr("get_wallet_address_show_UI");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (walletAddressShowUi != null) {
            String strCode = walletAddressShowUi.toString();
            Log.i("strCode", "mGenerate--: " + strCode);
            Gson gson = new Gson();
            GetCodeAddressBean getCodeAddressBean = gson.fromJson(strCode, GetCodeAddressBean.class);
            String qr_data = getCodeAddressBean.getQr_data();
            String addr = getCodeAddressBean.getAddr();
            tetGetBTC.setText(addr);
            Bitmap bitmap = CodeCreator.createQRCode(qr_data, 248, 248, null);
            imgCode.setImageBitmap(bitmap);
        }

    }

    @OnClick({R.id.img_back, R.id.tet_Copy, R.id.tet_deleteWallet})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_Copy:
                //copy text
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // The text is placed on the system clipboard.
                cm.setText(tetGetBTC.getText());
                Toast.makeText(CheckWalletDetailActivity.this, R.string.copysuccess, Toast.LENGTH_LONG).show();

                break;
            case R.id.tet_deleteWallet:
                //delete wallet dialog
                showDialogs(CheckWalletDetailActivity.this, R.layout.delete_wallet);


                //delete wallet
//                deleteWallet();
                break;
        }
    }

    private void showDialogs(Context context, @LayoutRes int resource) {
        //set see view
        View view = View.inflate(context, resource, null);
        dialogBtom = new Dialog(context, R.style.dialog);
        //delete
        //cancel dialog
        view.findViewById(R.id.tet_ConfirmDelete).setOnClickListener(v -> {
            Log.i("wallet_name", "showDialogs: "+wallet_name);
            try {
                Daemon.commands.callAttr("delete_wallet", wallet_name);
                EventBus.getDefault().post(new FirstEvent("11"));
                mToast(getResources().getString(R.string.delete_succse));
                Log.i("delete_wallet", "-----------: ");
            } catch (Exception e) {
                Log.i("delete_wallet", "===========: "+e.getMessage());
                e.printStackTrace();
            }

            dialogBtom.cancel();
        });
        //cancel dialog
        view.findViewById(R.id.tet_cancle).setOnClickListener(v -> {
            dialogBtom.cancel();
        });

        dialogBtom.setContentView(view);
        Window window = dialogBtom.getWindow();
        //set pop_up size
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        //set locate
        window.setGravity(Gravity.BOTTOM);
        //set animal
        window.setWindowAnimations(R.style.AnimBottom);
        dialogBtom.show();
    }

}
