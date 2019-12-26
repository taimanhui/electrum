package org.haobtc.wallet.activities;

import android.Manifest;
import android.app.Activity;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.chaquo.python.PyObject;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.thirdgoddess.tnt.image.ImageUtils;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.encode.CodeCreator;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.base.MyApplication;
import org.haobtc.wallet.utils.Daemon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.utils.Daemon.commands;

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
    private RxPermissions rxPermissions;
    private String rowTrsaction;
    private Bitmap bitmap;
    //add read write peimission
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private boolean toGallery;

    @Override
    public int getLayoutId() {
        return R.layout.activity_share_other;
    }

    @Override
    public void initView() {
//        getPermission(ShareOtherActivity.this);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        Intent intent = getIntent();
        rowTrsaction = intent.getStringExtra("rowTrsaction");
        Log.i("rowTrsaction", "init----: " + rowTrsaction);
        rxPermissions = new RxPermissions(this);


    }

    @Override
    public void initData() {
        //or code
        if (!TextUtils.isEmpty(rowTrsaction)) {
            PyObject get_qr_data_from_raw_tx = commands.callAttr("get_qr_data_from_raw_tx", rowTrsaction);
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


    @OnClick({R.id.tet_Preservation, R.id.tet_Copy, R.id.lin_downLoad, R.id.img_back, R.id.tet_open})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tet_Preservation:
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                    Log.i("TAG", "1");
                    if (ActivityCompat.checkSelfPermission(ShareOtherActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) ShareOtherActivity.this, PERMISSIONS_STORAGE, 1);
                        Log.i("TAG", "2");
                    } else {
                        toGallery = saveBitmap(bitmap);

                    }
                } else {
                    toGallery = saveBitmap(bitmap);

                }
                if (toGallery) {
                    mToast(getResources().getString(R.string.preservationbitmappic));
                } else {
                    Toast.makeText(this, R.string.preservationfail, Toast.LENGTH_SHORT).show();
                }

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

    public boolean saveBitmap(Bitmap bitmap) {
        try {
            boolean ifSucsee = ImageUtils.saveBitmap(ShareOtherActivity.this, bitmap);
            if (ifSucsee) {
                return true;
            } else {
                return false;

            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("printStackTrace", "save-------: " + e.getMessage());
        }
        return false;
    }

//    private boolean saveImageToGallery(String paths, Bitmap bmp) {
//        try {
//            File filePic;
//            if ("".equals(paths)) {
//                filePic = new File(Environment.getExternalStorageDirectory().getPath() + Environment.DIRECTORY_PICTURES + System.currentTimeMillis() + ".jpg");
//                System.out.println("---------new file----------" + filePic.canWrite());
//
//            } else {
//                filePic = new File(paths);
//                System.out.println("---------new file1----------");
//            }
//            System.out.println("---------new file2----------");
//            OutputStream fos = new FileOutputStream(filePic);
//            boolean success = bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//            System.out.println("-----------------" + success + "=--------------");
//            if (success) {
//                fos.flush();
//                fos.close();
//                MediaScannerConnection.scanFile(this
//                        , new String[]{filePic.getAbsolutePath()}
//                        , new String[]{"image/jpeg"}, (path, uri) ->
//                                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show());
//                return true;
//
//            }
//
//        } catch (IOException e) {
//            System.out.println("-------------------java");
//            System.out.println(e.getMessage());
//            return false;
//        }
//        return false;
//
//    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            toGallery = saveBitmap(bitmap);
            if (toGallery) {
                mToast(getResources().getString(R.string.preservationbitmappic));
            } else {
                Toast.makeText(this, R.string.preservationfail, Toast.LENGTH_SHORT).show();
            }
        }
    }


}

