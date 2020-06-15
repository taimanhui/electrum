package org.haobtc.wallet.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.chaquo.python.PyObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.encode.CodeCreator;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.entries.FsActivity;
import org.haobtc.wallet.utils.Daemon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dr.android.fileselector.FileSelectConstant;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.executorService;
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
    @BindView(R.id.tet_bigMessage)
    TextView tetBigMessage;
    private RxPermissions rxPermissions;
    private Bitmap bitmap;
    private boolean toGallery;
    private String rowTx;
    private String strCode;
    PyObject get_qr_data_from_raw_tx = null;

    @Override
    public int getLayoutId() {
        return R.layout.activity_share_other;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        Intent intent = getIntent();
        rowTx = intent.getStringExtra("rowTx");
        rxPermissions = new RxPermissions(this);
    }

    @Override
    public void initData() {
        //or code
        if (!TextUtils.isEmpty(rowTx)) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    //Sub thread
                    try {
                        get_qr_data_from_raw_tx = Daemon.commands.callAttr("get_qr_data_from_raw_tx", rowTx);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    mainhandler.sendEmptyMessage(1);
                }
            });
        }
        tetTrsactionText.setText(rowTx);
    }


    @SingleClick
    @OnClick({R.id.tet_Preservation, R.id.tet_Copy, R.id.lin_downLoad, R.id.img_back, R.id.tet_open})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tet_Preservation:
                rxPermissions
                        .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                if (bitmap != null) {
                                    toGallery = saveBitmap(bitmap);
                                    if (toGallery) {
                                        mToast(getString(R.string.preservationbitmappic));
                                    } else {
                                        Toast.makeText(this, R.string.preservationfail, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            } else { // Oups permission denied
                                Toast.makeText(this, R.string.reservatpion_photo, Toast.LENGTH_SHORT).show();
                            }
                        }).dispose();


                break;
            case R.id.tet_Copy:
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // The text is placed on the system clipboard.
                cm.setText(tetTrsactionText.getText());
                Toast.makeText(this, R.string.alredycopy, Toast.LENGTH_SHORT).show();
                break;
            case R.id.lin_downLoad:
                rxPermissions
                        .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                Intent intent = new Intent();
                                intent.setClass(getApplicationContext(), FsActivity.class);
                                intent.putExtra(FileSelectConstant.SELECTOR_REQUEST_CODE_KEY, FileSelectConstant.SELECTOR_MODE_FOLDER);
                                intent.putExtra("keyFile", "2");
                                startActivityForResult(intent, 1);

                            } else { // Oups permission denied
                                Toast.makeText(this, R.string.reservatpion_photo, Toast.LENGTH_SHORT).show();
                            }
                        }).dispose();

                break;
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_open:
                String strOpen = tetOpen.getText().toString();
                if (strOpen.equals(getString(R.string.spin_open))) {
                    LinearLayout.LayoutParams linearParams1 = (LinearLayout.LayoutParams) tetTrsactionText.getLayoutParams();
                    linearParams1.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    tetTrsactionText.setLayoutParams(linearParams1);
                    tetOpen.setText(getString(R.string.retract));

                } else {
                    LinearLayout.LayoutParams linearParams1 = (LinearLayout.LayoutParams) tetTrsactionText.getLayoutParams();
                    linearParams1.height = 200;
                    tetTrsactionText.setLayoutParams(linearParams1);
                    tetOpen.setText(getString(R.string.spin_open));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent bundle) {
        super.onActivityResult(requestCode, resultCode, bundle);
        if (resultCode == RESULT_OK) {
            ArrayList<String> listExtra = bundle.getStringArrayListExtra(FileSelectConstant.SELECTOR_BUNDLE_PATHS);
            String str = listExtra.toString();
            String substring = str.substring(1);
            String strPath = substring.substring(0, substring.length() - 1);
            showPopupFilename(strPath);

        }
    }

    private void showPopupFilename(String stPath) {
        Log.i("printException", "show---_________________________________" + stPath);
        View view1 = LayoutInflater.from(this).inflate(R.layout.dialog_view, null, false);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view1).create();
        ImageView img_Cancle = view1.findViewById(R.id.img_Cancle);
        String newPath = stPath.replace("内部存储", "/storage/emulated/0");
        view1.findViewById(R.id.btn_Confirm).setOnClickListener(v -> {
            EditText editFilename = view1.findViewById(R.id.edit_Filename);
            String strFilename = editFilename.getText().toString();
            String fullFilename = newPath + "/" + strFilename + ".psbt";

            try {
                commands.callAttr("save_tx_to_file", fullFilename, rowTx);
                mToast(getString(R.string.downloadsuccse));

            } catch (Exception e) {
                e.printStackTrace();
                Log.i("printException", "show---" + e.getMessage());
                mToast(getString(R.string.downloadfail));
            }
            alertDialog.dismiss();

        });
        img_Cancle.setOnClickListener(v -> {
            alertDialog.dismiss();
        });
        alertDialog.show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("HandlerLeak")
    Handler mainhandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (get_qr_data_from_raw_tx != null) {
                        strCode = get_qr_data_from_raw_tx.toString();
                        if (!TextUtils.isEmpty(strCode)) {
                            bitmap = mCreate2DCode(strCode);
                            imgOrcode.setImageBitmap(bitmap);
                        }
                    }
                    break;
            }
        }
    };

    public static Bitmap mCreate2DCode(String str) {
        //生成二维矩阵,编码时要指定大小,
        //不要生成了图片以后再进行缩放,以防模糊导致识别失败
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(str, BarcodeFormat.QR_CODE, 240, 240);
            int width = matrix.getWidth();
            int height = matrix.getHeight();
            //  二维矩阵转为一维像素数组（一直横着排）
            int[] pixels = new int[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (matrix.get(x, y)) {
                        pixels[y * width + x] = 0xff000000;
                    }
                }
            }
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            // 通过像素数组生成bitmap, 具体参考api
            bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }
}

