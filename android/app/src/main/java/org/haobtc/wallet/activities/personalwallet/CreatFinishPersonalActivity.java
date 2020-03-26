package org.haobtc.wallet.activities.personalwallet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.chaquo.python.PyObject;
import com.google.gson.Gson;
import com.yzq.zxinglibrary.encode.CodeCreator;
import org.haobtc.wallet.MainActivity;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.PublicPersonAdapter;
import org.haobtc.wallet.bean.GetCodeAddressBean;
import org.haobtc.wallet.event.AddBixinKeyEvent;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.MyDialog;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreatFinishPersonalActivity extends BaseActivity {

    @BindView(R.id.img_backCreat)
    ImageView imgBackCreat;
    @BindView(R.id.tetWalletname)
    TextView tetWalletname;
    @BindView(R.id.img_Orcode)
    ImageView imgOrcode;
    @BindView(R.id.tet_Preservation)
    TextView tetPreservation;
    @BindView(R.id.bn_complete_add_cosigner)
    Button bnCompleteAddCosigner;
    @BindView(R.id.recl_keyView)
    RecyclerView reclKeyView;
    private Bitmap bitmap;
    private String walletNames;
    private MyDialog myDialog;
    private String flagTag;
    private ArrayList<AddBixinKeyEvent> keyList;
    private String strBixinname;
    private Intent intent;

    @Override
    public int getLayoutId() {
        return R.layout.activity_creat_finish_personal;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        myDialog = MyDialog.showDialog(CreatFinishPersonalActivity.this);
        intent = getIntent();
        walletNames = intent.getStringExtra("walletNames");
        flagTag = intent.getStringExtra("flagTag");
        tetWalletname.setText(walletNames);

    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    try {
                        Daemon.commands.callAttr("create_multi_wallet", walletNames);
                    } catch (Exception e) {
                        e.printStackTrace();
                        myDialog.dismiss();
                        String message = e.getMessage();
                        if ("BaseException: file already exists at path".equals(message)) {
                            mToast(getResources().getString(R.string.changewalletname));
                        }
                        return;
                    }
                    myDialog.dismiss();
                    mIntent(MainActivity.class);
                    break;
            }
        }
    };

    @Override
    public void initData() {
        keyList = new ArrayList<>();
        mGeneratecode();
        //all bixinkey
        checkAllBixinkey();

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

    @OnClick({R.id.img_backCreat, R.id.tet_Preservation, R.id.bn_complete_add_cosigner})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_backCreat:
                finish();
                break;
            case R.id.tet_Preservation:
                boolean toGallery = saveBitmap(bitmap);
                if (toGallery) {
                    mToast(getResources().getString(R.string.preservationbitmappic));
                } else {
                    Toast.makeText(this, R.string.preservationfail, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bn_complete_add_cosigner:
                myDialog.show();
                handler.sendEmptyMessage(1);
                break;
        }
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
