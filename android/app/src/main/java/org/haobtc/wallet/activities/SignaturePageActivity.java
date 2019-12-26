package org.haobtc.wallet.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;
import org.haobtc.wallet.utils.Daemon;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SignaturePageActivity extends BaseActivity implements View.OnClickListener, TextWatcher {
    @BindView(R.id.tet_Error)
    TextView tetError;
    private Button buttonImport, buttonSweep, buttonPaste, buttonConfirm;
    private EditText editTextRaw;
    private RxPermissions rxPermissions;
    private static final int REQUEST_CODE = 0;

    @Override
    public int getLayoutId() {
        return R.layout.parse_raw_trans;
    }

    @Override
    public void initView() {
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        CommonUtils.enableToolBar(this, R.string.signature);
        rxPermissions = new RxPermissions(this);
        buttonConfirm = findViewById(R.id.confirm_sig);
        buttonImport = findViewById(R.id.import_file);
        buttonSweep = findViewById(R.id.sweep_sig);
        buttonPaste = findViewById(R.id.paste_sig);
        editTextRaw = findViewById(R.id.edit_raw);
        buttonConfirm.setOnClickListener(this);
        buttonImport.setOnClickListener(this);
        buttonSweep.setOnClickListener(this);
        buttonPaste.setOnClickListener(this);
        buttonConfirm.setEnabled(false);
        buttonConfirm.setBackground(getResources().getDrawable(R.drawable.button_bk_grey));
        editTextRaw.addTextChangedListener(this);


    }

    @Override
    public void initData() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm_sig:
                String raw = editTextRaw.getText().toString();
                Intent intent = new Intent(this, TransactionDetailsActivity.class);
                startActivity(intent);
                break;
            case R.id.import_file:

                break;
            case R.id.sweep_sig:
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
            case R.id.paste_sig:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData data = clipboard.getPrimaryClip();
                    if (data != null && data.getItemCount() > 0) {
                        editTextRaw.setText(data.getItemAt(0).getText());
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Scan QR code / barcode return
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                editTextRaw.setText(content);
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String strRaw = editTextRaw.getText().toString();
        if (!TextUtils.isEmpty(strRaw)) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {

                private PyObject is_valiad_xpub;

                @Override
                public void run() {
                    try {
                        is_valiad_xpub = Daemon.commands.callAttr("is_valiad_xpub", strRaw);

                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("printStackTrace", "run:=====  " + e.getMessage());
                        tetError.setVisibility(View.VISIBLE);
                        buttonConfirm.setEnabled(false);
                        buttonConfirm.setBackground(getResources().getDrawable(R.drawable.button_bk_grey));

                    }
                    if (is_valiad_xpub != null) {
                        tetError.setVisibility(View.INVISIBLE);
                        buttonConfirm.setEnabled(true);
                        buttonConfirm.setBackground(getResources().getDrawable(R.drawable.button_bk));
                        String strValiad = is_valiad_xpub.toString();
                        Log.e("printStackTrace", "run-----  " + strValiad);

                    }

                }
            }, 300);


            buttonConfirm.setEnabled(true);
            buttonConfirm.setBackground(getResources().getDrawable(R.drawable.button_bk));
        } else {
            buttonConfirm.setEnabled(false);
            buttonConfirm.setBackground(getResources().getDrawable(R.drawable.button_bk_grey));
        }

    }

}
