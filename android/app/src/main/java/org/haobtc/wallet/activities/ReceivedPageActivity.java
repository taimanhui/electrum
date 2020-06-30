package org.haobtc.wallet.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.chaquo.python.PyObject;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.encode.CodeCreator;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.bean.GetCodeAddressBean;
import org.haobtc.wallet.utils.Daemon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

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
            Gson gson = new Gson();
            GetCodeAddressBean getCodeAddressBean = gson.fromJson(strCode, GetCodeAddressBean.class);
            String qr_data = getCodeAddressBean.getQr_data();
            String addr = getCodeAddressBean.getAddr();
            textView5.setText(addr);
            bitmap = mCreate2DCode(qr_data);
            imageView2.setImageBitmap(bitmap);
        }

    }

    public static Bitmap mCreate2DCode(String str) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, 240, 240);
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SingleClick
    @OnClick({R.id.textView6, R.id.button, R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.textView6:
                //copy text
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // The text is placed on the system clipboard.
                Objects.requireNonNull(cm, "ClipboardManager not available").setPrimaryClip(ClipData.newPlainText(null, textView5.getText()));
                Toast.makeText(ReceivedPageActivity.this, R.string.copysuccess, Toast.LENGTH_LONG).show();

                break;
            case R.id.button:
                rxPermissions
                        .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                String uri = saveImageToGallery(this, bitmap);
                                if (uri != null) {
                                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(uri));
                                    shareIntent.setType("image/*");
                                    shareIntent.putExtra(Intent.EXTRA_TEXT, textView5.getText().toString());
                                    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    shareIntent = Intent.createChooser(shareIntent, "Here is the title of Select box");
                                    startActivity(shareIntent);

                                } else {
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


    private String saveImageToGallery(Context context, @NonNull Bitmap bmp) {
        // Save picture
        String storePath = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator;
        File appDir = new File(storePath);
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = System.currentTimeMillis() + ".png";
        File file = new File(appDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            //Compress and save pictures by IO stream
            boolean isSuccess;
            isSuccess = bmp.compress(Bitmap.CompressFormat.PNG, 60, fos);
            fos.flush();
            //Insert file into system library
            if (!isSuccess) {
                mToast(getString(R.string.fail));
                return null;
            }
            String uri = MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), fileName, null);
            //Send broadcast notice to update database after saving picture
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse(uri)));
            return uri;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}

