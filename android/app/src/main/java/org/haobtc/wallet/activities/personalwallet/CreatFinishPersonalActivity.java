package org.haobtc.wallet.activities.personalwallet;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.PyObject;
import com.google.gson.Gson;
import com.yzq.zxinglibrary.encode.CodeCreator;

import org.haobtc.wallet.MainActivity;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.SettingActivity;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.PublicPersonAdapter;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.bean.GetCodeAddressBean;
import org.haobtc.wallet.event.AddBixinKeyEvent;
import org.haobtc.wallet.utils.Daemon;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreatFinishPersonalActivity extends BaseActivity {

    @BindView(R.id.tetWalletname)
    TextView tetWalletname;
    @BindView(R.id.img_Orcode)
    ImageView imgOrcode;
    @BindView(R.id.tet_Preservation)
    TextView tetPreservation;
    @BindView(R.id.recl_keyView)
    RecyclerView reclKeyView;
    private Bitmap bitmap;
    private String walletNames;
    private String flagTag;
    private ArrayList<AddBixinKeyEvent> keyList;
    private String strBixinname;
    private Intent intent;
    private SharedPreferences.Editor edit;
    private boolean active_set_pin;

    @Override
    public int getLayoutId() {
        return R.layout.activity_creat_finish_personal;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        //Whether to set pin after activation
        active_set_pin = preferences.getBoolean("Active_set_PIN", false);
        intent = getIntent();
        walletNames = intent.getStringExtra("walletNames");
        flagTag = intent.getStringExtra("flagTag");
        tetWalletname.setText(walletNames);

    }

    @Override
    public void initData() {
        if (active_set_pin) {
            edit.putBoolean("Active_set_PIN", false);
            edit.apply();
            //show set PIN dialog
            showSetPINDialog();
        }

        keyList = new ArrayList<>();
        //get wallet QR code
        mGeneratecode();
        //all bixinkey
        checkAllBixinkey();

    }

    //show Whether to set pin after activation dialog
    private void showSetPINDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreatFinishPersonalActivity.this);
            builder.setTitle(getString(R.string.if_set_PIN));
            builder.setNegativeButton(getString(R.string.not_yet), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    builder.create().cancel();
                }
            });
            builder.setPositiveButton(getString(R.string.goto_set), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(CreatFinishPersonalActivity.this, SettingActivity.class);
                    intent.putExtra("ActiveSetPIN", "ActiveSetPIN");
                    startActivity(intent);
                }
            });
            builder.create().show();
    }

    private void checkAllBixinkey() {
        if (flagTag.equals("personal")) {
            strBixinname = intent.getStringExtra("strBixinname");
            AddBixinKeyEvent addBixinKeyEvent = new AddBixinKeyEvent();
            addBixinKeyEvent.setKeyname(strBixinname);
            keyList.add(addBixinKeyEvent);
            //public person
            PublicPersonAdapter publicPersonAdapter = new PublicPersonAdapter(keyList);
            reclKeyView.setAdapter(publicPersonAdapter);

        } else {
            List bixinKeylist = (List) intent.getSerializableExtra("strBixinlist");
            for (int i = 0; i < bixinKeylist.size(); i++) {
                AddBixinKeyEvent addBixinKeyEvent = new AddBixinKeyEvent();
                addBixinKeyEvent.setKeyname(((AddBixinKeyEvent) bixinKeylist.get(i)).getKeyname());
                keyList.add(addBixinKeyEvent);
            }
            //public person
            PublicPersonAdapter publicPersonAdapter = new PublicPersonAdapter(keyList);
            reclKeyView.setAdapter(publicPersonAdapter);

        }

    }

    @SingleClick
    @OnClick({R.id.tet_Preservation, R.id.bn_complete_add_cosigner})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.tet_Preservation:
                boolean toGallery = saveBitmap(bitmap);
                if (toGallery) {
                    mToast(getString(R.string.preservationbitmappic));
                } else {
                    Toast.makeText(this, R.string.preservationfail, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bn_complete_add_cosigner:
                mIntent(MainActivity.class);
                finishAffinity();
                break;
        }
    }

    private void mGeneratecode() {
        PyObject walletAddressShowUi = null;
        try {
            Daemon.commands.callAttr("select_wallet", walletNames);
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
            bitmap = CodeCreator.createQRCode(qr_data, 248, 248, null);
            imgOrcode.setImageBitmap(bitmap);
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
