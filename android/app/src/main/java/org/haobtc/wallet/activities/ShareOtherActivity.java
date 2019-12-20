package org.haobtc.wallet.activities;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.chaquo.python.PyObject;
import com.thirdgoddess.tnt.image.ImageUtils;
import com.yzq.zxinglibrary.encode.CodeCreator;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.Daemon;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShareOtherActivity extends BaseActivity {

    @BindView(R.id.img_Orcode)
    ImageView imgOrcode;
    @BindView(R.id.tet_Preservation)
    TextView tetPreservation;
    @BindView(R.id.tet_TrsactionText)
    TextView tetTrsactionText;
    @BindView(R.id.tet_Copy)
    TextView tetCopy;
    @BindView(R.id.lin_downLoad)
    LinearLayout lindownLoad;
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_open)
    TextView tetOpen;
    private String rowTrsaction;
    private Bitmap bitmap;

    @Override
    public int getLayoutId() {
        return R.layout.activity_share_other;
    }

    @Override
    public void initView() {
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        Intent intent = getIntent();
        rowTrsaction = intent.getStringExtra("rowTrsaction");
        Log.i("rowTrsaction", "init----: " + rowTrsaction);

        //add read write peimission
        String[] PERMISSIONS = {
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.WRITE_EXTERNAL_STORAGE"};
        int permission = ContextCompat.checkSelfPermission(this,
                "android.permission.WRITE_EXTERNAL_STORAGE");
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);
        }

    }

    @Override
    public void initData() {
        //or code
        if (!TextUtils.isEmpty(rowTrsaction)) {
            PyObject get_qr_data_from_raw_tx = Daemon.commands.callAttr("get_qr_data_from_raw_tx", rowTrsaction);
            if (get_qr_data_from_raw_tx != null) {
                String strCode = get_qr_data_from_raw_tx.toString();
                Log.i("strCode", "onView: " + strCode);
                if (!TextUtils.isEmpty(strCode)) {
                    bitmap = CodeCreator.createQRCode(strCode, 248, 248, null);
                    imgOrcode.setImageBitmap(bitmap);
                }
            }
        }

        tetTrsactionText.setText(rowTrsaction);

    }


    @OnClick({R.id.tet_Preservation, R.id.tet_Copy, R.id.lin_downLoad, R.id.img_back,R.id.tet_open})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tet_Preservation:
                saveBitmap(bitmap);
                break;
            case R.id.tet_Copy:
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // The text is placed on the system clipboard.
                cm.setText(tetTrsactionText.getText());
                Toast.makeText(this, R.string.copysuccess, Toast.LENGTH_SHORT).show();
                break;
            case R.id.lin_downLoad:
                break;
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_open:


                break;
        }
    }

    public void saveBitmap(Bitmap bitmap) {
        boolean ifSucsee = ImageUtils.saveBitmap(ShareOtherActivity.this, bitmap);
        if (ifSucsee) {
            Toast.makeText(this, R.string.preservationbitmappic, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.preservationfail, Toast.LENGTH_SHORT).show();

        }
    }

}
