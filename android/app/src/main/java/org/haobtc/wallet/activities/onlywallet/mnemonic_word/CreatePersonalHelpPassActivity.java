package org.haobtc.wallet.activities.onlywallet.mnemonic_word;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.chaquo.python.Kwarg;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.onlywallet.AppWalletSetPassActivity;
import org.haobtc.wallet.activities.onlywallet.RemeberMnemonicWordActivity;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.MyDialog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreatePersonalHelpPassActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.edt_Pass1)
    EditText edtPass1;
    @BindView(R.id.edt_Pass2)
    EditText edtPass2;
    @BindView(R.id.btn_setPin)
    Button btnSetPin;
    private MyDialog myDialog;
    private String type;
    private String seed;
    private String name;

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_personal_help_pass;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        myDialog = MyDialog.showDialog(CreatePersonalHelpPassActivity.this);
        Intent intent = getIntent();
        type = intent.getStringExtra("newWallet_type");
        seed = intent.getStringExtra("strNewseed");
        name = intent.getStringExtra("strnewWalletname");

    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_back, R.id.btn_setPin})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_setPin:
                myDialog.show();
                improtWallet();
                break;
        }
    }

    private void improtWallet() {
        String strPass1 = edtPass1.getText().toString();
        String strPass2 = edtPass2.getText().toString();
        if (TextUtils.isEmpty(strPass1)) {
            mToast(getResources().getString(R.string.set_pass));
            myDialog.dismiss();
            return;
        }
        if (TextUtils.isEmpty(strPass2)) {
            mToast(getResources().getString(R.string.set_pass_second));
            myDialog.dismiss();
            return;
        }
        if (!strPass1.equals(strPass2)) {
            mToast(getResources().getString(R.string.two_different_pass));
            myDialog.dismiss();
            return;
        }
        boolean passType = isPassType(strPass1);
        Log.i("passType", "passType: " + passType);
        if (!passType) {
            mToast(getResources().getString(R.string.passtype_wrong));
            myDialog.dismiss();
            return;
        }
        try {
            Daemon.commands.callAttr("create", name, strPass1, new Kwarg("seed", seed));

        } catch (Exception e) {
            myDialog.dismiss();
            e.printStackTrace();
            if (e.getMessage().contains("path is exist")) {
                mToast(getResources().getString(R.string.changewalletname));
            }
            return;
        }
        try {
            Daemon.commands.callAttr("load_wallet", name);
            Daemon.commands.callAttr("select_wallet", name);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        mIntent(CreateInputHelpWordWalletSuccseActivity.class);
    }

    //judge mobile is wrong or right
    public boolean isPassType(String mobiles) {
        Pattern p = Pattern
                .compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[$@$!%*?&])[A-Za-z\\d$@$!%*?&]{8,}");
        Matcher m = p.matcher(mobiles);

        return m.matches();
    }
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:

                    break;
            }
        }
    };

}




