package org.haobtc.wallet.activities.sign;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.utils.Daemon;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CheckSignMessageActivity extends BaseActivity {

    @BindView(R.id.testOriginal)
    TextView testOriginal;
    @BindView(R.id.testPublickey)
    TextView testPublickey;
    @BindView(R.id.testSignedMsg)
    TextView testSignedMsg;
    private String signMsg;
    private String signAddress;
    private String signedFinish;

    @Override
    public int getLayoutId() {
        return R.layout.activity_check_sign_message;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        Intent intent = getIntent();
        signMsg = intent.getStringExtra("signMsg");
        signAddress = intent.getStringExtra("signAddress");
        signedFinish = intent.getStringExtra("signedFinish");
        testOriginal.setText(signMsg);
        testPublickey.setText(signAddress);
        testSignedMsg.setText(signedFinish);
    }

    @Override
    public void initData() {

    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.testCopyPublickey, R.id.testCopySignedMsg})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.testCopyPublickey:
                copyMessage(testPublickey);
                break;
            case R.id.testCopySignedMsg:
                copyMessage(testSignedMsg);
                break;
            case R.id.btnConfirm:
                checkSigned();
                break;
        }
    }

    private void checkSigned() {
        PyObject verify_message = null;
        try {
            verify_message = Daemon.commands.callAttr("verify_message", signAddress, signMsg, signedFinish);
        } catch (Exception e) {
            e.printStackTrace();
            if (e.getMessage().contains("Invalid Bitcoin address")) {
                mToast(getString(R.string.changeaddress));
            }
        }
        if (verify_message != null) {
            boolean verify = verify_message.toBoolean();
            if (verify) {
                mlToast(getString(R.string.checksign_succsse));
            } else {
                mlToast(getString(R.string.checksign_fail));
            }
        }
    }

    private void copyMessage(TextView editMsg) {
        //copy text
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // The text is placed on the system clipboard.
        cm.setText(editMsg.getText());
        Toast.makeText(CheckSignMessageActivity.this, R.string.copysuccess, Toast.LENGTH_LONG).show();

    }

}
