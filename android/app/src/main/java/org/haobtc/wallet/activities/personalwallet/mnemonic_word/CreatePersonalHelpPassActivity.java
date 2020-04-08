package org.haobtc.wallet.activities.personalwallet.mnemonic_word;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.chaquo.python.Kwarg;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.MyDialog;

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
    private int walletNameNum;
    private SharedPreferences.Editor edit;

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_personal_help_pass;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        myDialog = MyDialog.showDialog(CreatePersonalHelpPassActivity.this);
        Intent intent = getIntent();
        type = intent.getStringExtra("newWallet_type");
        seed = intent.getStringExtra("strNewseed");
        name = intent.getStringExtra("strnewWalletname");
        walletNameNum = intent.getIntExtra("walletNameNum", 0);
        inits();

    }

    private void inits() {
        edtPass1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s) && !TextUtils.isEmpty(edtPass2.getText().toString())) {
                    btnSetPin.setEnabled(true);
                    btnSetPin.setBackground(getDrawable(R.drawable.button_bk));
                } else {
                    btnSetPin.setEnabled(false);
                    btnSetPin.setBackground(getDrawable(R.drawable.button_bk_grey));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edtPass2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!TextUtils.isEmpty(s) && !TextUtils.isEmpty(edtPass1.getText().toString())) {
                    btnSetPin.setEnabled(true);
                    btnSetPin.setBackground(getDrawable(R.drawable.button_bk));
                } else {
                    btnSetPin.setEnabled(false);
                    btnSetPin.setBackground(getDrawable(R.drawable.button_bk_grey));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
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
                handler.sendEmptyMessage(1);
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 1:
                    improtWallet();
                    break;
            }
        }
    };

    private void improtWallet() {
        String strPass1 = edtPass1.getText().toString();
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
        try {
            Daemon.commands.callAttr("create", name, strPass1, new Kwarg("seed", seed));
        } catch (Exception e) {
            myDialog.dismiss();
            e.printStackTrace();
            if (e.getMessage().contains("path is exist")) {
                mToast(getString(R.string.changewalletname));
            }
            return;
        }
        edit.putInt("defaultName", walletNameNum);
        edit.apply();
        try {
            Daemon.commands.callAttr("load_wallet", name);
            Daemon.commands.callAttr("select_wallet", name);
        } catch (Exception e) {
            myDialog.dismiss();
            e.printStackTrace();
            return;
        }
        myDialog.dismiss();
        mIntent(CreateInputHelpWordWalletSuccseActivity.class);
    }

}




