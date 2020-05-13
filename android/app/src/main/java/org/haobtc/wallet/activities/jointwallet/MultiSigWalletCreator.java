package org.haobtc.wallet.activities.jointwallet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chaquo.python.PyObject;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;
import com.yzq.zxinglibrary.encode.CodeCreator;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.MainActivity;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.adapter.AddBixinKeyAdapter;
import org.haobtc.wallet.adapter.PublicPersonAdapter;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.bean.GetCodeAddressBean;
import org.haobtc.wallet.event.AddBixinKeyEvent;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.IndicatorSeekBar;
import org.haobtc.wallet.utils.MyDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.xpub;

public class MultiSigWalletCreator extends BaseActivity implements TextWatcher{

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_Trone)
    TextView tetTrone;
    @BindView(R.id.tet_Trtwo)
    TextView tetTrtwo;
    @BindView(R.id.tet_Trthree)
    TextView tetTrthree;
    @BindView(R.id.edit_Walletname)
    EditText editWalletname;
    @BindView(R.id.seek_bar_fee)
    IndicatorSeekBar seekBarFee;
    @BindView(R.id.seek_bar_num)
    IndicatorSeekBar seekBarNum;
    @BindView(R.id.button)
    Button button;
    @BindView(R.id.tv_indicator)
    TextView tvIndicator;
    @BindView(R.id.tv_indicatorTwo)
    TextView tvIndicatorTwo;
    @BindView(R.id.bn_add_key)
    LinearLayout bnAddKey;
    @BindView(R.id.img_Progree1)
    ImageView imgProgree1;
    @BindView(R.id.img_Progree2)
    ImageView imgProgree2;
    @BindView(R.id.img_Progree3)
    ImageView imgProgree3;
    @BindView(R.id.card_viewOne)
    CardView cardViewOne;
    @BindView(R.id.card_viewThree)
    CardView cardViewThree;
    @BindView(R.id.recl_BinxinKey)
    RecyclerView reclBinxinKey;
    @BindView(R.id.rel_Finish)
    RelativeLayout relFinish;
    @BindView(R.id.rel_TwoNext1)
    RelativeLayout relTwoNext1;
    @BindView(R.id.bn_complete_add_cosigner)
    Button bnCompleteAddCosigner;
    @BindView(R.id.card_three_public)
    CardView cardThreePublic;
    @BindView(R.id.tet_who_wallet)
    TextView tetWhoWallet;
    @BindView(R.id.tet_many_key)
    TextView tetManyKey;
    @BindView(R.id.img_Orcode)
    ImageView imgOrcode;
    @BindView(R.id.tet_Preservation)
    TextView tetPreservation;
    @BindView(R.id.recl_publicPerson)
    RecyclerView reclPublicPerson;
    @BindView(R.id.btn_Finish)
    Button btnFinish;
    @BindView(R.id.testseekNum)
    TextView testseekNum;
    public String pin = "";
    public static final String TAG = MultiSigWalletCreator.class.getSimpleName();
    private RxPermissions rxPermissions;
    private static final int REQUEST_CODE = 0;
    private EditText edit_sweep;
    private TextView textView;
    private ArrayList<AddBixinKeyEvent> addEventsDatas;
    private String strInditor2;
    private String strInditor1;
    private MyDialog myDialog;
    private PyObject walletAddressShowUi;
    private SharedPreferences.Editor edit;
    private int walletNameNum;
    private Bitmap bitmap;
    private int strUp1;
    private int strUp2;
    private SharedPreferences preferences;
    private int defaultKeyNameNum;

    @Override
    public int getLayoutId() {
        return R.layout.activity_many_wallet_together;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void initView() {
        ButterKnife.bind(this);
        preferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        int defaultName = preferences.getInt("defaultName", 0);
        edit = preferences.edit();
        rxPermissions = new RxPermissions(this);
        myDialog = MyDialog.showDialog(MultiSigWalletCreator.this);
        editWalletname.addTextChangedListener(this);
        walletNameNum = defaultName + 1;
        editWalletname.setText(String.format("钱包%s", String.valueOf(walletNameNum)));

    }

    @Override
    public void initData() {
        addEventsDatas = new ArrayList<>();
        seekbarLatoutup();
        seekbarLatoutdown();
    }

    private void seekbarLatoutup() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tvIndicator.getLayoutParams();
        seekBarFee.setProgress(1);
        seekBarFee.setOnSeekBarChangeListener(new IndicatorSeekBar.OnIndicatorSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, float indicatorOffset) {
                String indicatorText = String.valueOf(progress + 2);
                tvIndicator.setText(indicatorText);
                params.leftMargin = (int) indicatorOffset;
                tvIndicator.setLayoutParams(params);
                String strWalletname = editWalletname.getText().toString();
                String invator1 = tvIndicator.getText().toString();
                String invator2 = tvIndicatorTwo.getText().toString();
                if (Integer.parseInt(invator1) != 0) {
                    if (!TextUtils.isEmpty(strWalletname)) {
                        if (Integer.parseInt(invator2) == 0) {
                            button.setEnabled(false);
                            button.setBackground(getDrawable(R.drawable.button_bk_grey));
                        } else {
                            button.setEnabled(true);
                            button.setBackground(getDrawable(R.drawable.button_bk));
                        }
                    } else {
                        button.setEnabled(false);
                        button.setBackground(getDrawable(R.drawable.button_bk_grey));
                    }
                } else {
                    button.setEnabled(false);
                    button.setBackground(getDrawable(R.drawable.button_bk_grey));
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tvIndicator.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tvIndicator.setVisibility(View.VISIBLE);

            }
        });

    }

    private void seekbarLatoutdown() {
        RelativeLayout.LayoutParams paramsTwo = (RelativeLayout.LayoutParams) tvIndicator.getLayoutParams();
        seekBarNum.setProgress(1);
        seekBarNum.setOnSeekBarChangeListener(new IndicatorSeekBar.OnIndicatorSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, float indicatorOffset) {
                String indicatorText = String.valueOf(progress + 1);
                tvIndicatorTwo.setText(indicatorText);
                paramsTwo.leftMargin = (int) indicatorOffset;
                tvIndicatorTwo.setLayoutParams(paramsTwo);
                String strWalletname = editWalletname.getText().toString();
                String invator1 = tvIndicator.getText().toString();
                String invator2 = tvIndicatorTwo.getText().toString();
                if (Integer.parseInt(invator1) != 0) {
                    if (!TextUtils.isEmpty(strWalletname)) {
                        if (Integer.parseInt(invator2) == 0) {
                            button.setEnabled(false);
                            button.setBackground(getDrawable(R.drawable.button_bk_grey));
                        } else {
                            button.setEnabled(true);
                            button.setBackground(getDrawable(R.drawable.button_bk));
                        }

                    } else {
                        button.setEnabled(false);
                        button.setBackground(getDrawable(R.drawable.button_bk_grey));
                    }
                } else {
                    button.setEnabled(false);
                    button.setBackground(getDrawable(R.drawable.button_bk_grey));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                tvIndicatorTwo.setVisibility(View.VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                tvIndicatorTwo.setVisibility(View.VISIBLE);

            }
        });
    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.button, R.id.bn_add_key, R.id.btn_Finish, R.id.bn_complete_add_cosigner, R.id.tet_Preservation})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.button:
                mCreatWalletNext();
                break;
            case R.id.bn_add_key:
                CommunicationModeSelector.runnables.clear();
                CommunicationModeSelector.runnables.add(runnable);
                CommunicationModeSelector.runnables.add(runnable2);
                Intent intent = new Intent(this, CommunicationModeSelector.class);
                intent.putExtra("tag", TAG);
                startActivity(intent);
                break;
            case R.id.btn_Finish:
                Intent intent1 = new Intent(this, MainActivity.class);
                startActivity(intent1);
                finishAffinity();
                break;
            case R.id.bn_complete_add_cosigner:
                myDialog.show();
                handler.sendEmptyMessage(1);
                break;
            case R.id.tet_Preservation:
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

    //get code
    private void mGeneratecode() {
        try {
            walletAddressShowUi = Daemon.commands.callAttr("get_wallet_address_show_UI");

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (walletAddressShowUi != null) {
            String strCode = walletAddressShowUi.toString();
            Gson gson = new Gson();
            GetCodeAddressBean getCodeAddressBean = gson.fromJson(strCode, GetCodeAddressBean.class);
            String qr_data = getCodeAddressBean.getQr_data();
            bitmap = CodeCreator.createQRCode(qr_data, 248, 248, null);
            imgOrcode.setImageBitmap(bitmap);

        }
    }

    private void mCreatWalletNext() {
        strInditor1 = tvIndicator.getText().toString();
        strInditor2 = tvIndicatorTwo.getText().toString();
        String strWalletname = editWalletname.getText().toString();
        strUp1 = Integer.parseInt(strInditor1);
        strUp2 = Integer.parseInt(strInditor2);
        if (TextUtils.isEmpty(strWalletname)) {
            mToast(getString(R.string.set_wallet));
            return;
        }
        if (strUp1 == 0) {
            mToast(getString(R.string.set_public_num));
            return;
        }
        if (strUp2 == 0) {
            mToast(getString(R.string.set_sign_num));
            return;
        }
        if (strUp1 < 2) {
            mToast(getString(R.string.public_person_2));
            return;
        }
        if (strUp2 > strUp1) {
            mToast(getString(R.string.signnum_dongt_public));
            return;
        }

        cardViewOne.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
        imgProgree1.setVisibility(View.GONE);
        imgProgree2.setVisibility(View.VISIBLE);
        imgProgree3.setVisibility(View.GONE);
        reclBinxinKey.setVisibility(View.VISIBLE);
        bnAddKey.setVisibility(View.VISIBLE);
        relTwoNext1.setVisibility(View.VISIBLE);
        tetTrtwo.setTextColor(getColor(R.color.button_bk_disableok));
        bnCompleteAddCosigner.setText(String.format("%s (0-%s)", getString(R.string.next), strInditor1));

        bnCompleteAddCosigner.setEnabled(false);

    }


    private void showInputDialogs(Context context, @LayoutRes int resource) {
        //set see view
        View view = View.inflate(context, resource, null);
        Dialog dialogBtoms = new Dialog(context, R.style.dialog);

        EditText edit_bixinName = view.findViewById(R.id.edit_keyName);
        TextView tet_Num = view.findViewById(R.id.txt_textNum);
        edit_sweep = view.findViewById(R.id.edit_public_key_cosigner_popup);
        int defaultKeyNum = preferences.getInt("defaultKeyNum", 0);
        defaultKeyNameNum = defaultKeyNum + 1;
        edit_bixinName.setText(String.format("pub%s", String.valueOf(defaultKeyNameNum)));
        edit_bixinName.addTextChangedListener(new TextWatcher() {
            CharSequence input;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                input = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tet_Num.setText(String.format(Locale.CHINA, "%d/20", input.length()));
                if (input.length() > 19) {
                    Toast.makeText(MultiSigWalletCreator.this, R.string.moreinput_text, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        edit_sweep.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String strLine = edit_sweep.getText().toString();
                if (!TextUtils.isEmpty(strLine)) {
                    view.findViewById(R.id.btn_ConfirmAll).setVisibility(View.GONE);
                    view.findViewById(R.id.lin_ComfirmAll).setVisibility(View.VISIBLE);
                } else {
                    view.findViewById(R.id.btn_ConfirmAll).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.lin_ComfirmAll).setVisibility(View.GONE);
                }
            }
        });
        //sweep
        view.findViewById(R.id.sweep_cosigner_popup).setOnClickListener(v -> {
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

        });
        //stick
        view.findViewById(R.id.paste_cosigner_popup1).setOnClickListener(v -> {
            //get Shear plate TODO：
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData data = clipboard.getPrimaryClip();
                if (data != null && data.getItemCount() > 0) {
                    edit_sweep.setText(data.getItemAt(0).getText());
                }
            }
        });

        view.findViewById(R.id.btn_Clear).setOnClickListener(v -> {
            edit_sweep.setText("");
        });
        view.findViewById(R.id.btn_ConfirmAll).setOnClickListener(v -> {
            String strBixinname = edit_bixinName.getText().toString();
            String strSweep = edit_sweep.getText().toString();
            if (TextUtils.isEmpty(strBixinname)) {
                mToast(getString(R.string.input_name));
                return;
            }
            if (TextUtils.isEmpty(strSweep)) {
                mToast(getString(R.string.input_public_address));
            }
        });
        view.findViewById(R.id.btn_Confirm).setOnClickListener(v -> {
            String strBixinname = edit_bixinName.getText().toString();
            String strSweep = edit_sweep.getText().toString();
            if (TextUtils.isEmpty(strBixinname)) {
                mToast(getString(R.string.input_name));
                return;
            }
            if (TextUtils.isEmpty(strSweep)) {
                mToast(getString(R.string.input_public_address));
                return;
            }
            AddBixinKeyEvent addBixinKeyEvent = new AddBixinKeyEvent();
            boolean exist = false;
            if (addEventsDatas.size() != 0) {
                for (int i = 0; i < addEventsDatas.size(); i++) {
                    if (strSweep.equals(addEventsDatas.get(i).getKeyaddress())) {
                        mToast(getString(R.string.please_change_xpub));
                        exist = true;
                        break;
                    }
                }
                if (!exist) {
                    addBixinKeyEvent = new AddBixinKeyEvent();
                    addBixinKeyEvent.setKeyname(strBixinname);
                    addBixinKeyEvent.setKeyaddress(strSweep);
                    addEventsDatas.add(addBixinKeyEvent);
                    dialogBtoms.cancel();
                }
            } else {
                addBixinKeyEvent.setKeyname(strBixinname);
                addBixinKeyEvent.setKeyaddress(strSweep);
                addEventsDatas.add(addBixinKeyEvent);
                dialogBtoms.cancel();
            }
            //public person
            PublicPersonAdapter publicPersonAdapter = new PublicPersonAdapter(addEventsDatas);
            reclPublicPerson.setAdapter(publicPersonAdapter);
            //bixinKEY
            AddBixinKeyAdapter addBixinKeyAdapter = new AddBixinKeyAdapter(addEventsDatas);
            reclBinxinKey.setAdapter(addBixinKeyAdapter);
            int cosignerNum = Integer.parseInt(strInditor1);
            bnCompleteAddCosigner.setText(String.format(Locale.CHINA, getString(R.string.next) + "（%d-%d)", addEventsDatas.size(), cosignerNum));

            if (addEventsDatas.size() == cosignerNum) {
                bnCompleteAddCosigner.setEnabled(true);
                bnCompleteAddCosigner.setBackground(getDrawable(R.drawable.little_radio_blue));
                bnAddKey.setVisibility(View.GONE);
            }
            edit.putInt("defaultKeyNum",defaultKeyNameNum);
            edit.apply();
            addBixinKeyAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                @Override
                public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                    switch (view.getId()) {
                        case R.id.img_deleteKey:
                            try {
                                Daemon.commands.callAttr("delete_xpub", strSweep);
                            } catch (Exception e) {
                                e.printStackTrace();
                                if (e.getMessage().contains("the xpub to be delete not in keystore")) {
                                    mToast(getString(R.string.no_delete_xpub));
                                }
                            }
                            addEventsDatas.remove(position);
                            addBixinKeyAdapter.notifyDataSetChanged();
                            bnAddKey.setVisibility(View.VISIBLE);
                            bnCompleteAddCosigner.setText(String.format(Locale.CHINA, getString(R.string.next) + "（%d-%d)", addEventsDatas.size(), cosignerNum));
                            bnCompleteAddCosigner.setBackground(getDrawable(R.drawable.little_radio_qian));
                            bnCompleteAddCosigner.setEnabled(false);

                            break;
                    }
                }
            });
        });

        //cancel dialog
        view.findViewById(R.id.img_cancle).setOnClickListener(v -> {
            dialogBtoms.cancel();
        });

        dialogBtoms.setContentView(view);
        Window window = dialogBtoms.getWindow();
        //set pop_up size
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        //set locate
        window.setGravity(Gravity.BOTTOM);
        //set animal
        window.setWindowAnimations(R.style.AnimBottom);
        dialogBtoms.show();
    }

    private void showConfirmPubDialog(Context context, @LayoutRes int resource, String xpub) {
        //set see view
        View view = View.inflate(context, resource, null);
        Dialog dialogBtoms = new Dialog(context, R.style.dialog);

        EditText edit_bixinName = view.findViewById(R.id.edit_keyName);
        TextView tet_Num = view.findViewById(R.id.txt_textNum);
        textView = view.findViewById(R.id.text_public_key_cosigner_popup);
        textView.setText(xpub);
        int defaultKeyNum = preferences.getInt("defaultKeyNum", 0);
        defaultKeyNameNum = defaultKeyNum + 1;
        edit_bixinName.setText(String.format("pub%s", String.valueOf(defaultKeyNameNum)));
        edit_bixinName.addTextChangedListener(new TextWatcher() {
            CharSequence input;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                input = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tet_Num.setText(String.format(Locale.CHINA, "%d/20", input.length()));
                if (input.length() > 19) {
                    Toast.makeText(MultiSigWalletCreator.this, R.string.moreinput_text, Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String strBixinname = edit_bixinName.getText().toString();
            String strSweep = textView.getText().toString();
            if (TextUtils.isEmpty(strBixinname)) {
                mToast(getString(R.string.input_name));
                return;
            }
            if (TextUtils.isEmpty(strSweep)) {
                mToast(getString(R.string.input_public_address));
                return;
            }
            AddBixinKeyEvent addBixinKeyEvent = new AddBixinKeyEvent();
            boolean exist = false;
            if (addEventsDatas.size() != 0) {
                for (int i = 0; i < addEventsDatas.size(); i++) {
                    if (strSweep.equals(addEventsDatas.get(i).getKeyaddress())) {
                        mToast(getString(R.string.please_change_xpub));
                        exist = true;
                        break;
                    }
                }
                if (!exist) {
                    addBixinKeyEvent = new AddBixinKeyEvent();
                    addBixinKeyEvent.setKeyname(strBixinname);
                    addBixinKeyEvent.setKeyaddress(strSweep);
                    addEventsDatas.add(addBixinKeyEvent);
                    dialogBtoms.cancel();
                }
            } else {
                addBixinKeyEvent.setKeyname(strBixinname);
                addBixinKeyEvent.setKeyaddress(strSweep);
                addEventsDatas.add(addBixinKeyEvent);
                dialogBtoms.cancel();
            }

            //public person
            PublicPersonAdapter publicPersonAdapter = new PublicPersonAdapter(addEventsDatas);
            reclPublicPerson.setAdapter(publicPersonAdapter);
            //bixinKEY
            AddBixinKeyAdapter addBixinKeyAdapter = new AddBixinKeyAdapter(addEventsDatas);
            reclBinxinKey.setAdapter(addBixinKeyAdapter);
            int cosignerNum = Integer.parseInt(strInditor1);
            bnCompleteAddCosigner.setText(String.format(Locale.CHINA, getString(R.string.next) + "（%d-%d)", addEventsDatas.size(), cosignerNum));
            if (addEventsDatas.size() == cosignerNum) {
                bnCompleteAddCosigner.setEnabled(true);
                bnCompleteAddCosigner.setBackground(getDrawable(R.drawable.little_radio_blue));
                bnAddKey.setVisibility(View.GONE);
            }
            edit.putInt("defaultKeyNum",defaultKeyNameNum);
            edit.apply();
            addBixinKeyAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                @Override
                public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                    if (view.getId() == R.id.img_deleteKey) {
                        try {
                            Daemon.commands.callAttr("delete_xpub", strSweep);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        addEventsDatas.remove(position);
                        addBixinKeyAdapter.notifyDataSetChanged();
                        bnAddKey.setVisibility(View.VISIBLE);
                        bnCompleteAddCosigner.setText(String.format(Locale.CHINA, getString(R.string.next) + "（%d-%d)", addEventsDatas.size(), cosignerNum));

                        bnCompleteAddCosigner.setBackground(getDrawable(R.drawable.little_radio_qian));
                        bnCompleteAddCosigner.setEnabled(false);
                    }
                }
            });
            dialogBtoms.cancel();
        });

        //cancel dialog
        view.findViewById(R.id.img_cancle).setOnClickListener(v -> {
            dialogBtoms.cancel();
        });


        dialogBtoms.setContentView(view);
        Window window = dialogBtoms.getWindow();
        //set pop_up size
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        //set locate
        window.setGravity(Gravity.BOTTOM);
        //set animal
        window.setWindowAnimations(R.style.AnimBottom);
        dialogBtoms.show();
    }


    private Runnable runnable = () -> showInputDialogs(MultiSigWalletCreator.this, R.layout.bixinkey_input);
    private Runnable runnable2 = () -> showConfirmPubDialog(this, R.layout.bixinkey_confirm, xpub);

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                edit_sweep.setText(content);
            }
        }
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String indication = tvIndicator.getText().toString();
        String indication2 = tvIndicatorTwo.getText().toString();
        if (!TextUtils.isEmpty(s.toString())) {
            if (s.length() > 13) {
                editWalletname.setTextSize(13);
            } else {
                editWalletname.setTextSize(15);
            }
            if (Integer.parseInt(indication) != 0) {
                if (Integer.parseInt(indication2) != 0) {
                    button.setEnabled(true);
                    button.setBackground(getDrawable(R.drawable.button_bk));
                } else {
                    button.setEnabled(false);
                    button.setBackground(getDrawable(R.drawable.button_bk_grey));
                }
            } else {
                button.setEnabled(false);
                button.setBackground(getDrawable(R.drawable.button_bk_grey));
            }
        } else {
            button.setEnabled(false);
            button.setBackground(getDrawable(R.drawable.button_bk_grey));
        }

    }

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                ArrayList<String> pubList = new ArrayList<>();
                String strWalletname = editWalletname.getText().toString();
                strInditor1 = tvIndicator.getText().toString();
                strInditor2 = tvIndicatorTwo.getText().toString();
                for (int i = 0; i < addEventsDatas.size(); i++) {
                    String keyaddress = addEventsDatas.get(i).getKeyaddress();
                    pubList.add("\"" + keyaddress + "\"");
                }

                try {
                    Daemon.commands.callAttr("import_create_hw_wallet", strWalletname, strUp2, strUp1, pubList.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    myDialog.dismiss();
                    String message = e.getMessage();
                    if ("BaseException: file already exists at path".equals(message)) {
                        mToast(getString(R.string.changewalletname));
                    } else if (message.contains("The same xpubs have create wallet")) {
                        mToast(getString(R.string.xpub_have_wallet));
                    } else if (message.contains("invaild type of xpub")) {
                        mToast(getString(R.string.xpub_wrong));
                    } else if (message.contains("Wrong key type p2wpkh")) {
                        mToast(getString(R.string.wrong_key_type));
                    }
                    return;
                }
                edit.putInt("defaultName", walletNameNum);
                edit.apply();
                //Generate QR code
                mGeneratecode();
                myDialog.dismiss();
                cardViewOne.setVisibility(View.GONE);
                button.setVisibility(View.GONE);
                imgProgree1.setVisibility(View.GONE);
                imgProgree2.setVisibility(View.GONE);
                imgProgree3.setVisibility(View.VISIBLE);
                reclBinxinKey.setVisibility(View.GONE);
                bnAddKey.setVisibility(View.GONE);
                relTwoNext1.setVisibility(View.GONE);
                relFinish.setVisibility(View.VISIBLE);
                tetTrthree.setTextColor(getColor(R.color.button_bk_disableok));
                cardViewThree.setVisibility(View.VISIBLE);
                relFinish.setVisibility(View.VISIBLE);
                cardThreePublic.setVisibility(View.VISIBLE);
                tetWhoWallet.setText(String.format("%s  （%s/%s）", strWalletname, strInditor1, strInditor2));
                tetManyKey.setText(String.format("%s%s%s", getString(R.string.is_use), strInditor1, getString(R.string.the_only_bixinkey)));
                EventBus.getDefault().post(new FirstEvent("11"));
            }
        }
    };
}
