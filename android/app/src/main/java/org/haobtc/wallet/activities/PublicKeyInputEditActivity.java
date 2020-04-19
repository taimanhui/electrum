package org.haobtc.wallet.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.aop.SingleClick;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PublicKeyInputEditActivity extends BaseActivity {
    @BindView(R.id.edit_public_key)
    EditText editTextPublicKey;
    @BindView(R.id.bn_sweep_create)
    Button bnSweepCreate;
    @BindView(R.id.bn_paste_create)
    Button bnPasteCreate;
    @BindView(R.id.bn_confirm_create)
    Button bnConfirmCreate;
    @BindView(R.id.img_back)
    ImageView imgBack;
    private RxPermissions rxPermissions;
    private static final int REQUEST_CODE = 0;


    public int getLayoutId() {
        return R.layout.input_address_edit;
    }

    public void initView() {

        ButterKnife.bind(this);
        rxPermissions = new RxPermissions(this);

    }

    @Override
    public void initData() {

    }

    @SingleClick
    @OnClick({R.id.bn_sweep_create, R.id.bn_paste_create, R.id.bn_confirm_create,R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bn_sweep_create:
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
            case R.id.bn_paste_create:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData data = clipboard.getPrimaryClip();
                    if (data != null && data.getItemCount() > 0) {
                        editTextPublicKey.setText(data.getItemAt(0).getText());
                    }
                }
                break;
            case R.id.bn_confirm_create:
                if (editTextPublicKey.getText().toString().equals("1234")) {
                    Intent intent = new Intent(this, SelectMultiSigWalletActivity.class);
                    startActivity(intent);
                } else {
                    // TODO:

                }
                break;
            case R.id.img_back:
                finish();
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
                Log.i("content", "on------: " + content);
                if (!TextUtils.isEmpty(content)) {
                    if (content.contains("bitcoin:")) {
                        String replace = content.replaceAll("bitcoin:", "");
                        editTextPublicKey.setText(replace);
                    } else {
                        editTextPublicKey.setText(content);
                    }
                }
            }
        }
    }

}
