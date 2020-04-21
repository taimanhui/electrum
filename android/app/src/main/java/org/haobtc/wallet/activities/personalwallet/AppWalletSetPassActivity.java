package org.haobtc.wallet.activities.personalwallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.chaquo.python.PyObject;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.utils.Daemon;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.executorService;

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
    private int defaultName;
    private String strPass1;
    private PyObject pyObject = null;

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
                strPass1 = edtPass1.getText().toString();
                String strPass2 = edtPass2.getText().toString();
                if (TextUtils.isEmpty(strPass1)) {
                    mToast(getString(R.string.set_pass));
                    return;
                }
                if (TextUtils.isEmpty(strPass2)) {
                    mToast(getString(R.string.set_pass_second));
                    return;
                }
                if (!strPass1.equals(strPass2)) {
                    mToast(getString(R.string.two_different_pass));
                    return;
                }
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent1 = new Intent(AppWalletSetPassActivity.this, MnemonicActivity.class);
                        intent1.putExtra("strName", strName);
                        intent1.putExtra("strPass1", strPass1);
                        startActivity(intent1);
                        finish();
                        try {
                            pyObject = Daemon.commands.callAttr("create", strName, strPass1);
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (e.getMessage().contains("path is exist")) {
                                mToast(getString(R.string.changewalletname));
                            }else if (e.getMessage().contains("The same seed have create wallet")){
                                mToast(getString(R.string.same_seed_have));
                            }
                            return;
                        }
                        strpyObject = pyObject.toString();
                        if (!TextUtils.isEmpty(strpyObject)) {
                            int walletNameNum = defaultName + 1;
                            edit.putInt("defaultName", walletNameNum);
                            edit.putBoolean("haveCreateNopass", true);
                            edit.apply();
                            EventBus.getDefault().postSticky(new FirstEvent(strpyObject));
                        }
                    }
                });
                break;
        }
    }

}
