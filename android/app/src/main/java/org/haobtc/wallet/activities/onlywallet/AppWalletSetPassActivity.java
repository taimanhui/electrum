package org.haobtc.wallet.activities.onlywallet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.MyDialog;

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
    private String strSeed;
    private String strpyObject;
    private MyDialog myDialog;

    @Override
    public int getLayoutId() {
        return R.layout.activity_app_wallet_set_pass;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        strSeed = preferences.getString("strSeed", "");
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
                String strPass1 = edtPass1.getText().toString();
                String strPass2 = edtPass2.getText().toString();
                if (TextUtils.isEmpty(strPass1)) {
                    mToast(getResources().getString(R.string.set_pass));
                    return;
                }
                if (TextUtils.isEmpty(strPass2)) {
                    mToast(getResources().getString(R.string.set_pass_second));
                    return;
                }
                if (!strPass1.equals(strPass2)) {
                    mToast(getResources().getString(R.string.two_different_pass));
                    return;
                }
                if (!TextUtils.isEmpty(strSeed)) {
                    try {
                        Daemon.commands.callAttr("create", strName, strPass1, new Kwarg("seed", strSeed));
                        if (!TextUtils.isEmpty(strSeed)) {
                            try {
                                Daemon.commands.callAttr("load_wallet", strName,new Kwarg("password", strPass1));
                                Daemon.commands.callAttr("select_wallet", strName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            Intent intent = new Intent(AppWalletSetPassActivity.this, RemeberMnemonicWordActivity.class);
                            intent.putExtra("strSeed",strSeed);
                            startActivity(intent);
                            myDialog.dismiss();
                        } else {
                            myDialog.dismiss();
                        }

                    } catch (Exception e) {
                        myDialog.dismiss();
                        e.printStackTrace();
                        Log.i("strpyObject", "Exception----- " + e.getMessage());
                        if (e.getMessage().contains("path is exist")){
                            mToast(getResources().getString(R.string.changewalletname));
                        }
                        return;
                        //local taste noodle trial level soda mobile orchard amazing bean gossip library
                    }
                } else {
                    try {
                        PyObject pyObject = Daemon.commands.callAttr("create", strName, strPass1);
                        strpyObject = pyObject.toString();
                        if (!TextUtils.isEmpty(strpyObject)) {
                            try {
                                Daemon.commands.callAttr("load_wallet", strName,new Kwarg("password", strPass1));
                                Daemon.commands.callAttr("select_wallet", strName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            edit.putString("strSeed",strpyObject);
                            edit.apply();
                            myDialog.dismiss();
                            Intent intent = new Intent(AppWalletSetPassActivity.this, RemeberMnemonicWordActivity.class);
                            intent.putExtra("strSeed",strpyObject);
                            startActivity(intent);
                        } else {
                            myDialog.dismiss();
                        }
                    } catch (Exception e) {
                        Log.i("strpyObject", "Exception::::: " + e.getMessage());
                        myDialog.dismiss();
                        e.printStackTrace();
                        if (e.getMessage().contains("path is exist")){
                            mToast(getResources().getString(R.string.changewalletname));
                        }
                        return;
                    }
                }

                break;
        }
    }

}
