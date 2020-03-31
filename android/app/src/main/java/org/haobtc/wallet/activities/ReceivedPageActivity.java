package org.haobtc.wallet.activities;

import android.Manifest;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.chaquo.python.PyObject;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.encode.CodeCreator;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.bean.GetCodeAddressBean;
import org.haobtc.wallet.utils.Daemon;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ReceivedPageActivity extends BaseActivity {

    @BindView(R.id.imageView2)
    ImageView imageView2;
    @BindView(R.id.textView5)
    TextView textView5;
    @BindView(R.id.textView6)
    TextView textView6;
    @BindView(R.id.button)
    Button button;
    @BindView(R.id.img_back)
    ImageView imgBack;
    private Bitmap bitmap;
    private RxPermissions rxPermissions;
    @Override
    public int getLayoutId() {
        return R.layout.address_info;
    }

    @Override
    public void initView() {

        ButterKnife.bind(this);
        rxPermissions = new RxPermissions(this);
    }

    @Override
    public void initData() {
        //Generate QR code
        mGeneratecode();


    }

    private void mGeneratecode() {
        PyObject walletAddressShowUi = null;
        try {
            walletAddressShowUi = Daemon.commands.callAttr("get_wallet_address_show_UI");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (walletAddressShowUi != null) {
            String strCode = walletAddressShowUi.toString();
            Log.i("strCode", "mGenerate--: " + strCode);
            Gson gson = new Gson();
            GetCodeAddressBean getCodeAddressBean = gson.fromJson(strCode, GetCodeAddressBean.class);
            String qr_data = getCodeAddressBean.getQr_data();
            String addr = getCodeAddressBean.getAddr();
            textView5.setText(addr);
            bitmap = CodeCreator.createQRCode(qr_data, 248, 248, null);
            imageView2.setImageBitmap(bitmap);
        }

    }


    @OnClick({R.id.textView6, R.id.button,R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.textView6:
                //copy text
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // The text is placed on the system clipboard.
                cm.setText(textView5.getText());
                Toast.makeText(ReceivedPageActivity.this, R.string.copysuccess, Toast.LENGTH_LONG).show();

                break;
            case R.id.button:
                rxPermissions
                        .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                File file = saveImageToGallery(this, bitmap);
                                if (file!=null){
                                    String path = insertImageToSystem(this, file.getPath());
                                    Intent imageIntent = new Intent(Intent.ACTION_SEND);
                                    imageIntent.setType("image/jpeg");
                                    imageIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
                                    startActivity(Intent.createChooser(imageIntent, getString(R.string.share)));

                                }else{
                                    mToast(getString(R.string.pictrue_fail));
                                }

                            } else { // Oups permission denied
                                Toast.makeText(this, R.string.reservatpion_photo, Toast.LENGTH_SHORT).show();
                            }
                        }).dispose();

                break;
            case R.id.img_back:
                finish();
                break;
        }
    }

    private File saveImageToGallery(Context context, Bitmap bmp) {
        // 首先保存图片
        String storePath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator;
        Log.i("storePath", "storePath: "+storePath);
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".png";
        File file = new File(appDir, fileName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            //通过io流的方式来压缩保存图片
            boolean isSuccess = false;
            if (bmp != null) {
                isSuccess = bmp.compress(Bitmap.CompressFormat.PNG, 60, fos);
            }
            fos.flush();
            fos.close();

            //把文件插入到系统图库
            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);

            //保存图片后发送广播通知更新数据库
            Uri uri = Uri.fromFile(file);
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            if (isSuccess) {
//                Toast.makeText(context, "成功", Toast.LENGTH_SHORT).show();
                return file;
            } else {
                Toast.makeText(context, "失败", Toast.LENGTH_SHORT).show();
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String insertImageToSystem(Context context, String imagePath) {
        String url = "";
        try {
            url = MediaStore.Images.Media.insertImage(context.getContentResolver(), imagePath, "yup", "");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return url;
    }

}

