package org.haobtc.wallet.activities.jointwallet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
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

import org.haobtc.wallet.MainActivity;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.WalletUnActivatedActivity;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.AddBixinKeyAdapter;
import org.haobtc.wallet.adapter.PublicPersonAdapter;
import org.haobtc.wallet.bean.GetCodeAddressBean;
import org.haobtc.wallet.bean.HardwareFeatures;
import org.haobtc.wallet.event.AddBixinKeyEvent;
import org.haobtc.wallet.fragment.ReadingPubKeyDialogFragment;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;
import org.haobtc.wallet.utils.IndicatorSeekBar;
import org.haobtc.wallet.utils.MyDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.REQUEST_ACTIVE;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.futureTask;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.executorService;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.xpub;
import static org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector.isNFC;
public class MultiSigWalletCreator extends BaseActivity implements TextWatcher {

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
    private Dialog dialogBtom;
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
    private boolean executable = true;
    private CommunicationModeSelector dialogFragment;
    private boolean isActive;
    private int walletNameNum;
    private Bitmap bitmap;
    private boolean ready;

    @Override
    public int getLayoutId() {
        return R.layout.activity_many_wallet_together;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        int defaultName = preferences.getInt("defaultName", 0);
        edit = preferences.edit();
        rxPermissions = new RxPermissions(this);
        myDialog = MyDialog.showDialog(MultiSigWalletCreator.this);
        editWalletname.addTextChangedListener(this);
        walletNameNum = defaultName +1;
        editWalletname.setText(String.format("钱包%s", String.valueOf(walletNameNum)));

    }

    @Override
    public void initData() {
        addEventsDatas = new ArrayList<>();
        seekbarLatoutup();
        seekbarLatoutdown();
    }

    private void showPopupAddCosigner1() {
        List<Runnable> runnables = new ArrayList<>();
        runnables.add(runnable);
        runnables.add(runnable2);
        dialogFragment = new CommunicationModeSelector(TAG, runnables, "");
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    private void seekbarLatoutup() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tvIndicator.getLayoutParams();
        seekBarFee.setOnSeekBarChangeListener(new IndicatorSeekBar.OnIndicatorSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, float indicatorOffset) {
                String indicatorText = String.valueOf(progress+2);
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
        seekBarNum.setOnSeekBarChangeListener(new IndicatorSeekBar.OnIndicatorSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, float indicatorOffset) {
                String indicatorText = String.valueOf(progress+2);
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
                showPopupAddCosigner1();
                break;
            case R.id.btn_Finish:
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
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
        int strUp1 = Integer.parseInt(strInditor1);
        int strUp2 = Integer.parseInt(strInditor2);
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

        try {
            Daemon.commands.callAttr("set_multi_wallet_info", strWalletname, strUp1, strUp2);
        } catch (Exception e) {
            e.printStackTrace();
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

    private void showSelectFeeDialogs(Context context, @LayoutRes int resource) {
        //set see view
        View view = View.inflate(context, resource, null);
        dialogBtom = new Dialog(context, R.style.dialog);

        view.findViewById(R.id.text_input_publickey_by_hand).setOnClickListener(v -> {
            showInputDialogs(MultiSigWalletCreator.this, R.layout.bixinkey_input);
            dialogBtom.cancel();
            showInputDialogs(MultiSigWalletCreator.this, R.layout.bixinkey_input);

        });


        //cancel dialog
        view.findViewById(R.id.img_cancel).setOnClickListener(v -> {
            dialogBtom.cancel();
        });


        dialogBtom.setContentView(view);
        Window window = dialogBtom.getWindow();
        //set pop_up size
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        //set locate
        window.setGravity(Gravity.BOTTOM);
        //set animal
        window.setWindowAnimations(R.style.AnimBottom);
        dialogBtom.show();
    }

    private void showInputDialogs(Context context, @LayoutRes int resource) {
        //set see view
        View view = View.inflate(context, resource, null);
        Dialog dialogBtoms = new Dialog(context, R.style.dialog);

        EditText edit_bixinName = view.findViewById(R.id.edit_keyName);
        TextView tet_Num = view.findViewById(R.id.txt_textNum);
        edit_sweep = view.findViewById(R.id.edit_public_key_cosigner_popup);

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
            try {
                //add
                Daemon.commands.callAttr("add_xpub", strSweep);
            } catch (Exception e) {
                Toast.makeText(this, R.string.changeaddress, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }
            AddBixinKeyEvent addBixinKeyEvent = new AddBixinKeyEvent();
            addBixinKeyEvent.setKeyname(strBixinname);
            addBixinKeyEvent.setKeyaddress(strSweep);
            addEventsDatas.add(addBixinKeyEvent);
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
            addBixinKeyAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                @Override
                public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                    switch (view.getId()) {
                        case R.id.img_deleteKey:
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

                            break;
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

    private void showConfirmPubDialog(Context context, @LayoutRes int resource, String xpub) {
        //set see view
        View view = View.inflate(context, resource, null);
        Dialog dialogBtoms = new Dialog(context, R.style.dialog);

        EditText edit_bixinName = view.findViewById(R.id.edit_keyName);
        TextView tet_Num = view.findViewById(R.id.txt_textNum);
        textView = view.findViewById(R.id.text_public_key_cosigner_popup);
        textView.setText(xpub);
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
            try {
                //add
                Daemon.commands.callAttr("add_xpub", strSweep);
            } catch (Exception e) {
                Toast.makeText(this, R.string.changeaddress, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }
            AddBixinKeyEvent addBixinKeyEvent = new AddBixinKeyEvent();
            addBixinKeyEvent.setKeyname(strBixinname);
            addBixinKeyEvent.setKeyaddress(strSweep);
            addEventsDatas.add(addBixinKeyEvent);
            //public person
            PublicPersonAdapter publicPersonAdapter = new PublicPersonAdapter(addEventsDatas);
            reclPublicPerson.setAdapter(publicPersonAdapter);
            //bixinKEY
            AddBixinKeyAdapter addBixinKeyAdapter = new AddBixinKeyAdapter(addEventsDatas);
            reclBinxinKey.setAdapter(addBixinKeyAdapter);

            int cosignerNum = Integer.parseInt(strInditor1);
            bnCompleteAddCosigner.setText(String.format(Locale.CHINA, getString(R.string.next) + "（%d-%d)", addEventsDatas.size(), cosignerNum));
            dialogFragment.dismiss();
            if (addEventsDatas.size() == cosignerNum) {
                bnCompleteAddCosigner.setEnabled(true);
                bnCompleteAddCosigner.setBackground(getDrawable(R.drawable.little_radio_blue));
                bnAddKey.setVisibility(View.GONE);
            }
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
            dialogFragment.dismiss();

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


    private HardwareFeatures getFeatures() throws Exception {
        String feature;
        try {
            feature = executorService.submit(() -> Daemon.commands.callAttr("get_feature")).get().toString();
            return new Gson().fromJson(feature, HardwareFeatures.class);
        } catch (ExecutionException | InterruptedException e) {
            Toast.makeText(this, "communication error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            throw e;
        }
    }

    private void getResult() {
        try {
            ReadingPubKeyDialogFragment dialog = dialogFragment.showReadingDialog();
            xpub = futureTask.get(40, TimeUnit.SECONDS).toString();
            dialog.dismiss();
            showConfirmPubDialog(this, R.layout.bixinkey_confirm, xpub);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            if ("com.chaquo.python.PyException: BaseException: (7, 'PIN invalid')".equals(e.getMessage())) {
                dialogFragment.showReadingFailedDialog(R.string.pin_wrong);
            } else {
                dialogFragment.showReadingFailedDialog(R.string.read_pk_failed);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction(); // get the action of the coming intent
        if (Objects.equals(action, NfcAdapter.ACTION_NDEF_DISCOVERED) // NDEF type
                || Objects.equals(action, NfcAdapter.ACTION_TECH_DISCOVERED)
                || Objects.requireNonNull(action).equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            isNFC = true;
            dealWithBusiness(intent);
        }

    }

    private void dealWithBusiness(Intent intent) {
        if (executable) {
            Tag tags = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            PyObject nfc = Global.py.getModule("trezorlib.transport.nfc");
            PyObject nfcHandler = nfc.get("NFCHandle");
            nfcHandler.put("device", tags);
            executable = false;
        }
        if (ready) {
            CommunicationModeSelector.customerUI.put("pin", pin);
            getResult();
            ready = false;
            return;
        }
        HardwareFeatures features;
        try {
            features = getFeatures();
        } catch (Exception e) {
            return;
        }
        boolean isInit = features.isInitialized();
        if (isInit) {
            boolean pinCached = features.isPinCached();
                futureTask = new FutureTask<>(() -> Daemon.commands.callAttr("get_xpub_from_hw"));
                executorService.submit(futureTask);
                if (pinCached) {
                   getResult();
                }
            } else {
                if (isActive) {
                    executorService.execute(
                            () -> {
                                try {
                                    Daemon.commands.callAttr("init");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
            } else {
                Intent intent1 = new Intent(this, WalletUnActivatedActivity.class);
                startActivityForResult(intent1, REQUEST_ACTIVE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CommunicationModeSelector.PIN_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) { // 激活、创建
                pin = data.getStringExtra("pin");
                int tag = data.getIntExtra("tag", 0);
                switch (tag) {
                    case CommunicationModeSelector.PIN_NEW_FIRST: // 激活
                        // ble 激活
                        if (CommunicationModeSelector.isActive) {
                            CommunicationModeSelector.customerUI.put("pin", pin);
                            CommunicationModeSelector.handler.sendEmptyMessage(CommunicationModeSelector.SHOW_PROCESSING);
                            CommunicationModeSelector.isActive = false;
                        } else if (isActive) {
                            // nfc 激活
                            CommunicationModeSelector.pin = pin;
                            CommunicationModeSelector.handler.sendEmptyMessage(CommunicationModeSelector.SHOW_PROCESSING);
                            isActive = false;
                        }
                        break;
                    case CommunicationModeSelector.PIN_CURRENT: // 创建
                           if (!isNFC) { // ble
                               CommunicationModeSelector.customerUI.put("pin", pin);
                               new Handler().postDelayed(this::getResult, (long) 0.2);
                           } else { // nfc
                              ready = true;
                           }
                           break;
                    default:
                }

            }
        } else if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                edit_sweep.setText(content);
            }
        } else if (requestCode == REQUEST_ACTIVE && resultCode == Activity.RESULT_OK) { // nfc 和 ble 激活
            if (data != null) {
                isActive = data.getBooleanExtra("isActive", false);
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
            if (s.length()>13){
                editWalletname.setTextSize(13);
            }else{
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
            switch (msg.what) {
                case 1:
                    String strWalletname = editWalletname.getText().toString();
                    strInditor1 = tvIndicator.getText().toString();
                    strInditor2 = tvIndicatorTwo.getText().toString();
                    try {
                        Daemon.commands.callAttr("create_multi_wallet", strWalletname);
                    } catch (Exception e) {
                        e.printStackTrace();
                        myDialog.dismiss();
                        String message = e.getMessage();
                        if ("BaseException: file already exists at path".equals(message)) {
                            mToast(getString(R.string.changewalletname));
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
                    break;
            }
        }
    };

}

