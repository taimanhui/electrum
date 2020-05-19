package org.haobtc.wallet.activities.settings.recovery_set;

import android.Manifest;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.encode.CodeCreator;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.aop.SingleClick;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BackupMessageActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_preversation)
    TextView tetPreversation;
    @BindView(R.id.textView5)
    TextView textView5;
    @BindView(R.id.copy_tet)
    TextView copyTet;
    @BindView(R.id.btn_continue)
    Button btncontinue;
    @BindView(R.id.tet_Keyname)
    TextView tetKeyname;
    @BindView(R.id.img_orcode)
    ImageView imgOrcode;
    private RxPermissions rxPermissions;
    private Intent intent;
    private String flagWhere;
    private Bitmap bitmap;

    @Override
    public int getLayoutId() {
        return R.layout.activity_backup_message;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        rxPermissions = new RxPermissions(this);
        intent = getIntent();
        inits();
    }

    private void inits() {
        String strKeyname = intent.getStringExtra("strKeyname");
        flagWhere = intent.getStringExtra("flagWhere");
        String backupMessage = intent.getStringExtra("backupMessage");

        textView5.setText(backupMessage);
        tetKeyname.setText(strKeyname);
        if (!TextUtils.isEmpty(flagWhere)) {
            if ("Backup".equals(flagWhere)) {
                btncontinue.setText(getString(R.string.recover_backup));
            } else {
                btncontinue.setText(getString(R.string.finish));
            }
        }
        if (!TextUtils.isEmpty(backupMessage)){
            bitmap = CodeCreator.createQRCode(backupMessage, 248, 248, null);
            imgOrcode.setImageBitmap(bitmap);
        }
    }

    @Override
    public void initData() {

    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.tet_preversation, R.id.copy_tet, R.id.btn_continue})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_preversation:
                rxPermissions
                        .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                boolean toGallery = saveBitmap(bitmap);
                                if (toGallery) {
                                    mToast(getString(R.string.preservationbitmappic));
                                } else {
                                    Toast.makeText(this, R.string.preservationfail, Toast.LENGTH_SHORT).show();
                                }

                            } else { // Oups permission denied
                                Toast.makeText(this, R.string.reservatpion_photo, Toast.LENGTH_SHORT).show();
                            }
                        }).dispose();
                break;
            case R.id.copy_tet:
                //copy text
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // The text is placed on the system clipboard.
                cm.setText(textView5.getText());
                Toast.makeText(BackupMessageActivity.this, R.string.copysuccess, Toast.LENGTH_LONG).show();
                break;
            case R.id.btn_continue:
                if ("Backup".equals(flagWhere)) {
                    // new version code
                    Intent intent = new Intent(this, CommunicationModeSelector.class);
                    startActivity(intent);
                } else {
                    finish();
                }
                break;
        }
    }

    public boolean saveBitmap(Bitmap bitmap) {
        try {
            File filePic = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + System.currentTimeMillis() + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            boolean success = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return success;

        } catch (IOException ignored) {
            return false;
        }
    }
}
