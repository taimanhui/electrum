package org.haobtc.wallet.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.chaquo.python.PyObject;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.entries.FsActivity;
import org.haobtc.wallet.utils.CommonUtils;
import org.haobtc.wallet.utils.Daemon;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dr.android.fileselector.FileSelectConstant;


public class SignaturePageActivity extends BaseActivity implements TextWatcher {
    @BindView(R.id.tet_Error)
    TextView tetError;
    @BindView(R.id.edit_raw)
    EditText editTextRaw;
    @BindView(R.id.import_file)
    Button buttonImport;
    @BindView(R.id.sweep_sig)
    Button buttonSweep;
    @BindView(R.id.paste_sig)
    Button buttonPaste;
    @BindView(R.id.confirm_sig)
    Button buttonConfirm;
    private RxPermissions rxPermissions;
    private static final int REQUEST_CODE = 0;
    private PyObject is_valiad_xpub;

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
        buttonConfirm.setEnabled(false);
        buttonConfirm.setBackground(getResources().getDrawable(R.drawable.button_bk_grey));
        editTextRaw.addTextChangedListener(this);


    }

    @Override
    public void initData() {

    }


    @OnClick({R.id.import_file, R.id.sweep_sig, R.id.paste_sig, R.id.confirm_sig})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.import_file:
                rxPermissions
                        .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                Intent intent1 = new Intent(Intent.ACTION_GET_CONTENT);
                                intent1.setClass(getApplicationContext(), FsActivity.class);
                                intent1.putExtra(FileSelectConstant.SELECTOR_REQUEST_CODE_KEY, FileSelectConstant.SELECTOR_MODE_FILE);
//                                intent1.addCategory(Intent.CATEGORY_DEFAULT);
                                intent1.addCategory(Intent.CATEGORY_OPENABLE);
                                intent1.putExtra("keyFile", "1");
                                startActivityForResult(intent1, 1);

                            } else { // Oups permission denied
                                Toast.makeText(this, R.string.reservatpion_photo, Toast.LENGTH_SHORT).show();
                            }
                        }).dispose();


                break;
            case R.id.confirm_sig:
                String raw = editTextRaw.getText().toString();
                Intent intent = new Intent(this, TransactionDetailsActivity.class);
                startActivity(intent);
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
        } else if (requestCode == 1 && resultCode == RESULT_OK) {
            ArrayList<String> listExtra = data.getStringArrayListExtra(FileSelectConstant.SELECTOR_BUNDLE_PATHS);
            String str = listExtra.toString();
            String substring = str.substring(1);
            String strPath = substring.substring(0, substring.length() - 1);
            Log.i("listExtra", "listExtra--: " + listExtra + "   strPath ---  " + strPath);
            try {
                //read file
                PyObject read_tx_from_file = Daemon.commands.callAttr("read_tx_from_file", strPath);
                if (read_tx_from_file != null) {
                    String readFile = read_tx_from_file.toString();
                    Log.i("readFile", "tx-------: " + readFile);
                    editTextRaw.setText(readFile);

                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, getResources().getString(R.string.filestyle_wrong), Toast.LENGTH_SHORT).show();
                return;
            }

        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String strRaw = editTextRaw.getText().toString();
        Log.i("CharSequence", "------------ "+strRaw);
        if (!TextUtils.isEmpty(strRaw)) {
            try {
                try {
                    is_valiad_xpub = Daemon.commands.callAttr("is_valiad_xpub", strRaw);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                if (is_valiad_xpub != null) {
                    String strValiad = is_valiad_xpub.toString();
                    if (strValiad.equals("False")) {
                        tetError.setVisibility(View.VISIBLE);
                        buttonConfirm.setEnabled(false);
                        buttonConfirm.setBackground(getResources().getDrawable(R.drawable.button_bk_grey));
                    } else {
                        tetError.setVisibility(View.INVISIBLE);
                        buttonConfirm.setEnabled(true);
                        buttonConfirm.setBackground(getResources().getDrawable(R.drawable.button_bk));
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
                tetError.setVisibility(View.VISIBLE);
                buttonConfirm.setEnabled(false);
                buttonConfirm.setBackground(getResources().getDrawable(R.drawable.button_bk_grey));

            }

        } else {
            tetError.setVisibility(View.INVISIBLE);
            buttonConfirm.setEnabled(false);
            buttonConfirm.setBackground(getResources().getDrawable(R.drawable.button_bk_grey));
        }

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

}
