package org.haobtc.wallet.activities.sign;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.android.CaptureActivity;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.Daemon;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SignMessageActivity extends BaseActivity {

    @BindView(R.id.editInputAddress)
    EditText editInputAddress;
    @BindView(R.id.editInputPublicKey)
    EditText editInputPublicKey;
    @BindView(R.id.checkSignedMsg)
    TextView checkSignedMsg;
    @BindView(R.id.btnConfirm)
    Button btnConfirm;
    @BindView(R.id.testCopyMsg)
    TextView testCopyMsg;
    @BindView(R.id.testCopyAddress)
    TextView testCopyAddress;
    @BindView(R.id.testCopySignedMsg)
    TextView testCopySignedMsg;
    private RxPermissions rxPermissions;
    private static final int REQUEST_CODE = 0;
    private String strSignMsg;
    private String strinputAddress;

    @Override
    public int getLayoutId() {
        return R.layout.activity_sign_message;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {
        Intent intent = getIntent();
        strSignMsg = intent.getStringExtra("strSignMsg");
        rxPermissions = new RxPermissions(this);
        TextChange textChange = new TextChange();
        editInputAddress.addTextChangedListener(textChange);
        editInputPublicKey.addTextChangedListener(textChange);
        editInputAddress.setText(strSignMsg);
    }

    @OnClick({R.id.img_back, R.id.textCheckSign, R.id.sweepAddress, R.id.pasteAddress, R.id.pastePublicKey, R.id.testCopyMsg, R.id.testCopyAddress, R.id.testCopySignedMsg, R.id.btnConfirm})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.textCheckSign:
                mIntent(CheckSignActivity.class);
                break;
            case R.id.sweepAddress:
                rxPermissions
                        .request(Manifest.permission.CAMERA)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                //If you have already authorized it, you can directly jump to the QR code scanning interface
                                Intent intent2 = new Intent(this, CaptureActivity.class);
                                startActivityForResult(intent2, REQUEST_CODE);
                            } else { // Oups permission denied
                                Toast.makeText(this, R.string.photopersion, Toast.LENGTH_SHORT).show();
                            }
                        }).dispose();
                break;
            case R.id.pasteAddress:
                //paste
                pasteMessage(editInputAddress);
                break;
            case R.id.pastePublicKey:
                //paste
                pasteMessage(editInputPublicKey);
                break;
            case R.id.testCopyMsg:
                //copy
                copyContent(editInputAddress);
                break;
            case R.id.testCopyAddress:
                //copy
                copyContent(editInputPublicKey);
                break;
            case R.id.testCopySignedMsg:
                //copy
                copyContent(checkSignedMsg);
                break;
            case R.id.btnConfirm:
                //sign
                toSignMsg();
                break;
        }
    }

    //copy text
    private void copyContent(TextView editContent) {
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        // The text is placed on the system clipboard.
        assert cm != null;
        cm.setText(editContent.getText());
        mToast(getString(R.string.copysuccess));
    }

    //sign message
    private void toSignMsg() {
        if (btnConfirm.getText().toString().equals(getString(R.string.confirm))){
            signDialog();
        }else{
            finish();
        }
    }

    private void signDialog() {
        View view1 = LayoutInflater.from(SignMessageActivity.this).inflate(R.layout.input_wallet_pass, null, false);
        AlertDialog alertDialog = new AlertDialog.Builder(SignMessageActivity.this).setView(view1).create();
        EditText str_pass = view1.findViewById(R.id.edit_password);
        view1.findViewById(R.id.btn_enter_wallet).setOnClickListener(v -> {
            String strPassword = str_pass.getText().toString();
            strinputAddress = editInputAddress.getText().toString();
            if (TextUtils.isEmpty(strPassword)) {
                mToast(getString(R.string.please_input_pass));
                return;
            }
            PyObject sign_message = null;
            try {
                sign_message = Daemon.commands.callAttr("sign_message", strinputAddress, strSignMsg, strPassword);
            } catch (Exception e) {
                if (e.getMessage().contains("Incorrect password")) {
                    mToast(getString(R.string.wrong_pass));
                }
                alertDialog.dismiss();
                e.printStackTrace();
                return;
            }
            if (sign_message != null) {
                String signedMessage = sign_message.toString();
                checkSignedMsg.setText(signedMessage);
                testCopySignedMsg.setVisibility(View.VISIBLE);
                btnConfirm.setText(getString(R.string.finish));
                alertDialog.dismiss();
            }

        });
        view1.findViewById(R.id.cancel_select_wallet).setOnClickListener(v -> {
            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    public void pasteMessage(EditText editString) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData data = clipboard.getPrimaryClip();
            if (data != null && data.getItemCount() > 0) {
                editString.setText(data.getItemAt(0).getText());
            }
        }
    }

    class TextChange implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            //judge button status
            buttonColorStatus();
        }
    }

    //judge button status
    private void buttonColorStatus() {
        strinputAddress = editInputAddress.getText().toString();
        String strInputPublicKey = editInputPublicKey.getText().toString();
        if (TextUtils.isEmpty(strinputAddress) || TextUtils.isEmpty(strInputPublicKey)) {
            btnConfirm.setEnabled(false);
            btnConfirm.setBackground(getDrawable(R.drawable.button_bk_grey));
            if (!TextUtils.isEmpty(strinputAddress)) {
                testCopyMsg.setVisibility(View.VISIBLE);
            } else {
                testCopyMsg.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(strInputPublicKey)) {
                testCopyAddress.setVisibility(View.VISIBLE);
            } else {
                testCopyAddress.setVisibility(View.GONE);
            }
        } else {
            btnConfirm.setEnabled(true);
            btnConfirm.setBackground(getDrawable(R.drawable.button_bk));
            testCopyMsg.setVisibility(View.VISIBLE);
            testCopyAddress.setVisibility(View.VISIBLE);
        }

    }

}
