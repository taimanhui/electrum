package org.haobtc.wallet.activities.personalwallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.MyDialog;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AppWalletSetPassActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.btn_setPin)
    Button btnSetPin;
    @BindView(R.id.edt_Pass1)
    EditText edtPass1;
    @BindView(R.id.edt_Pass2)
    EditText edtPass2;
    private String strName;
    private SharedPreferences.Editor edit;
    private String strpyObject;
    private MyDialog myDialog;
    private int defaultName;
    private String strPass1;


    @Override
    public int getLayoutId() {
        return R.layout.activity_app_wallet_set_pass;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        defaultName = preferences.getInt("defaultName", 0);
        Intent intent = getIntent();
        strName = intent.getStringExtra("strName");
        myDialog = MyDialog.showDialog(AppWalletSetPassActivity.this);

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
                strPass1 = edtPass1.getText().toString();
                String strPass2 = edtPass2.getText().toString();
                if (TextUtils.isEmpty(strPass1)) {
                    mToast(getString(R.string.set_pass));
                    myDialog.dismiss();
                    return;
                }
                if (TextUtils.isEmpty(strPass2)) {
                    mToast(getString(R.string.set_pass_second));
                    myDialog.dismiss();
                    return;
                }
                if (!strPass1.equals(strPass2)) {
                    mToast(getString(R.string.two_different_pass));
                    myDialog.dismiss();
                    return;
                }
                handler.sendEmptyMessage(1);
                int walletNameNum = defaultName + 1;
                edit.putInt("defaultName", walletNameNum);
                edit.apply();

                break;
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    PyObject pyObject = null;
                    try {
                        pyObject = Daemon.commands.callAttr("create", strName, strPass1);
                        Daemon.commands.callAttr("load_wallet", strName);
                        Daemon.commands.callAttr("select_wallet", strName);
                    } catch (Exception e) {
                        myDialog.dismiss();
                        e.printStackTrace();
                        if (e.getMessage().contains("path is exist")) {
                            mToast(getString(R.string.changewalletname));
                        }
                        return;
                    }
                    strpyObject = pyObject.toString();
                    if (!TextUtils.isEmpty(strpyObject)) {
                        edit.putBoolean("haveCreateNopass", true);
                        edit.apply();
                        myDialog.dismiss();
                        Intent intent1 = new Intent(AppWalletSetPassActivity.this, MnemonicActivity.class);
                        intent1.putExtra("strSeed", strpyObject);
                        intent1.putExtra("strName", strName);
                        intent1.putExtra("strPass1", strPass1);
                        startActivity(intent1);
                    } else {
                        myDialog.dismiss();
                    }
                    break;
            }
        }
    };

}
