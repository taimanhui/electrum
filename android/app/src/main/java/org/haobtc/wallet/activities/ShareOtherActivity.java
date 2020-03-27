package org.haobtc.wallet.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;

import com.chaquo.python.PyObject;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.encode.CodeCreator;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
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
    private String rowTrsaction;
    private Bitmap bitmap;
    private boolean toGallery;
    private Dialog dialogBtom;
    private String rowTx;

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
        rowTx = intent.getStringExtra("rowTx");
        Log.i("rowTrsaction", "init----: " + rowTrsaction);
        rxPermissions = new RxPermissions(this);


    }

    @Override
    public void initData() {
        //or code
        if (!TextUtils.isEmpty(rowTrsaction)) {

            PyObject get_qr_data_from_raw_tx = null;
            try {
                get_qr_data_from_raw_tx = Daemon.commands.callAttr("get_qr_data_from_raw_tx", rowTrsaction);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            if (get_qr_data_from_raw_tx != null) {
                String strCode = get_qr_data_from_raw_tx.toString();
                Log.i("strCode", "onView: " + strCode);
                if (!TextUtils.isEmpty(strCode)) {
                    if (strCode.length() >= 150) {
                        tetBigMessage.setVisibility(View.VISIBLE);
                        imgOrcode.setVisibility(View.GONE);
                    } else {
                        bitmap = CodeCreator.createQRCode(strCode, 248, 248, null);
                        imgOrcode.setImageBitmap(bitmap);
                    }

                }
            }
        }

        tetTrsactionText.setText(rowTx);

    }


    @OnClick({R.id.tet_Preservation, R.id.tet_Copy, R.id.lin_downLoad, R.id.img_back, R.id.tet_open})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tet_Preservation:
                rxPermissions
                        .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                toGallery = saveBitmap(bitmap);
                                if (toGallery) {
                                    mToast(getResources().getString(R.string.preservationbitmappic));
                                } else {
                                    Toast.makeText(this, R.string.preservationfail, Toast.LENGTH_SHORT).show();
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
                if (strOpen.equals(getResources().getString(R.string.spin_open))) {
                    LinearLayout.LayoutParams linearParams1 = (LinearLayout.LayoutParams) tetTrsactionText.getLayoutParams();
                    linearParams1.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    tetTrsactionText.setLayoutParams(linearParams1);
                    tetOpen.setText(getResources().getString(R.string.retract));

                } else {
                    LinearLayout.LayoutParams linearParams1 = (LinearLayout.LayoutParams) tetTrsactionText.getLayoutParams();
                    linearParams1.height = 200;
                    tetTrsactionText.setLayoutParams(linearParams1);
                    tetOpen.setText(getResources().getString(R.string.spin_open));
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
            showPopupFilename(ShareOtherActivity.this, R.layout.dialog_view, strPath);

        }
    }

    private void showPopupFilename(Context context, @LayoutRes int resource, String stPath) {
        //set view
        View inflate = View.inflate(context, resource, null);
        dialogBtom = new Dialog(context, R.style.dialog);
        EditText edtFilename = inflate.findViewById(R.id.edit_Filename);
        TextView teCancle = inflate.findViewById(R.id.tet_Cancle);
        TextView teConfirm = inflate.findViewById(R.id.tet_Confirm);
        teCancle.setOnClickListener(v -> {
            dialogBtom.cancel();
        });
        teConfirm.setOnClickListener(v -> {
            String strFilename = edtFilename.getText().toString();
            String fullFilename = stPath + "/" + strFilename + ".psbt";

            try {
                commands.callAttr("save_tx_to_file", fullFilename, rowTx);
                mToast(getResources().getString(R.string.downloadsuccse));

            } catch (Exception e) {
                e.printStackTrace();
                Log.i("printException", "show---" + e.getMessage());
                mToast(getResources().getString(R.string.downloadfail));
            }

            dialogBtom.cancel();
        });


        dialogBtom.setContentView(inflate);
        dialogBtom.setCanceledOnTouchOutside(false);
        Window window = dialogBtom.getWindow();
        //Set pop-up size
        window.setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        //set locate
        window.setGravity(Gravity.BOTTOM);
        //set animal
        window.setWindowAnimations(R.style.AnimBottom);
        dialogBtom.show();

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

}

