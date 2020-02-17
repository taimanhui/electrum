package org.haobtc.wallet.activities.manywallet;

import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chaquo.python.PyObject;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.AddBixinKeyAdapter;
import org.haobtc.wallet.event.AddBixinKeyEvent;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.IndicatorSeekBar;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ManyWalletTogetherActivity extends BaseActivity {

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
    private Dialog dialogBtom;
    private RxPermissions rxPermissions;
    private static final int REQUEST_CODE = 0;
    private EditText edit_sweep;
    private ArrayList<AddBixinKeyEvent> addEventsDatas;
    private PyObject is_valiad_xpub;
    private String strInditor2;
    private String strInditor1;

    @Override
    public int getLayoutId() {
        return R.layout.activity_many_wallet_together;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        rxPermissions = new RxPermissions(this);

    }


    @Override
    public void initData() {
        addEventsDatas = new ArrayList<>();

        seekbarLatoutup();
        seekbarLatoutdown();
    }

    private void seekbarLatoutup() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) tvIndicator.getLayoutParams();
        seekBarFee.setOnSeekBarChangeListener(new IndicatorSeekBar.OnIndicatorSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, float indicatorOffset) {
                String indicatorText = String.valueOf(progress);
                tvIndicator.setText(indicatorText);
                params.leftMargin = (int) indicatorOffset;
                tvIndicator.setLayoutParams(params);
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
                String indicatorText = String.valueOf(progress);
                tvIndicatorTwo.setText(indicatorText);
                paramsTwo.leftMargin = (int) indicatorOffset;
                tvIndicatorTwo.setLayoutParams(paramsTwo);
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

    @OnClick({R.id.img_back, R.id.button, R.id.bn_add_key, R.id.rel_Finish, R.id.bn_complete_add_cosigner})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.button:
                mCreatWalletNext();
                break;
            case R.id.bn_add_key:
                showSelectFeeDialogs(ManyWalletTogetherActivity.this, R.layout.bluetooce_nfc);
                break;
            case R.id.rel_Finish:

                break;
            case R.id.bn_complete_add_cosigner:
                String strWalletname = editWalletname.getText().toString();
                strInditor1 = tvIndicator.getText().toString();
                strInditor2 = tvIndicatorTwo.getText().toString();
                cardViewOne.setVisibility(View.GONE);
                button.setVisibility(View.GONE);
                imgProgree1.setVisibility(View.GONE);
                imgProgree2.setVisibility(View.GONE);
                imgProgree3.setVisibility(View.VISIBLE);
                reclBinxinKey.setVisibility(View.GONE);
                bnAddKey.setVisibility(View.GONE);
                relTwoNext1.setVisibility(View.GONE);
                relFinish.setVisibility(View.VISIBLE);
                tetTrthree.setTextColor(getResources().getColor(R.color.button_bk_disableok));
                cardViewThree.setVisibility(View.VISIBLE);
                relFinish.setVisibility(View.VISIBLE);
                cardThreePublic.setVisibility(View.VISIBLE);
                tetWhoWallet.setText(String.format("%s  （%s/%s）", strWalletname, strInditor1, strInditor2));

                break;
        }
    }

    private void mCreatWalletNext() {
        strInditor1 = tvIndicator.getText().toString();
        strInditor2 = tvIndicatorTwo.getText().toString();
        String strWalletname = editWalletname.getText().toString();
        int strUp1 = Integer.parseInt(strInditor1);
        int strUp2 = Integer.parseInt(strInditor2);
        if (TextUtils.isEmpty(strWalletname)) {
            mToast(getResources().getString(R.string.set_wallet));
            return;
        }
        if (strUp1 == 0) {
            mToast(getResources().getString(R.string.set_public_num));
            return;
        }
        if (strUp2 == 0) {
            mToast(getResources().getString(R.string.set_sign_num));
            return;
        }
        if (strUp1 < 2) {
            mToast(getResources().getString(R.string.public_person_2));
            return;
        }
        if (strUp2 > strUp1) {
            mToast(getResources().getString(R.string.signnum_dongt_public));
            return;
        }

        try {
            Daemon.commands.callAttr("set_multi_wallet_info", strWalletname, strUp1, strUp2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cardViewOne.setVisibility(View.GONE);
        button.setVisibility(View.GONE);
        imgProgree1.setVisibility(View.GONE);
        imgProgree2.setVisibility(View.VISIBLE);
        imgProgree3.setVisibility(View.GONE);
        reclBinxinKey.setVisibility(View.VISIBLE);
        bnAddKey.setVisibility(View.VISIBLE);
        relTwoNext1.setVisibility(View.VISIBLE);
        tetTrtwo.setTextColor(getResources().getColor(R.color.button_bk_disableok));
        bnCompleteAddCosigner.setText(String.format("0-%s", strInditor2));
        bnCompleteAddCosigner.setEnabled(false);

    }

    private void showSelectFeeDialogs(Context context, @LayoutRes int resource) {
        //set see view
        View view = View.inflate(context, resource, null);
        dialogBtom = new Dialog(context, R.style.dialog);

        view.findViewById(R.id.tet_handInput).setOnClickListener(v -> {
            dialogBtom.cancel();
            showInputDialogs(ManyWalletTogetherActivity.this, R.layout.bixinkey_input);

        });


        //cancel dialog
        view.findViewById(R.id.img1_Cancle).setOnClickListener(v -> {
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
                    Toast.makeText(ManyWalletTogetherActivity.this, R.string.moreinput_text, Toast.LENGTH_SHORT).show();
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

//                String strRaw = edit_sweep.getText().toString();
//                Log.i("CharSequence", "------------ " + strRaw);
//                if (!TextUtils.isEmpty(strRaw)) {
//                    try {
//                        try {
//                            is_valiad_xpub = Daemon.commands.callAttr("is_valiad_xpub", strRaw);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            return;
//                        }
//                        if (is_valiad_xpub != null) {
//                            String strValiad = is_valiad_xpub.toString();
//                            if (strValiad.equals("False")) {
//                                view.findViewById(R.id.tet_Error).setVisibility(View.VISIBLE);
//                            } else {
//                                view.findViewById(R.id.tet_Error).setVisibility(View.INVISIBLE);
//                            }
//
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        view.findViewById(R.id.tet_Error).setVisibility(View.VISIBLE);
//                    }
//
//                } else {
//                    view.findViewById(R.id.tet_Error).setVisibility(View.INVISIBLE);
//                }
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
                mToast(getResources().getString(R.string.input_name));
                return;
            }
            if (TextUtils.isEmpty(strSweep)) {
                mToast(getResources().getString(R.string.input_public_address));
                return;
            }
        });
        view.findViewById(R.id.btn_Confirm).setOnClickListener(v -> {
            String strBixinname = edit_bixinName.getText().toString();
            String strSweep = edit_sweep.getText().toString();
            if (TextUtils.isEmpty(strBixinname)) {
                mToast(getResources().getString(R.string.input_name));
                return;
            }
            if (TextUtils.isEmpty(strSweep)) {
                mToast(getResources().getString(R.string.input_public_address));
                return;
            }
            try {
                //add
                Daemon.commands.callAttr("add_xpub", strSweep);
            } catch (Exception e) {
                Toast.makeText(this, R.string.changeaddress, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            AddBixinKeyEvent addBixinKeyEvent = new AddBixinKeyEvent();
            addBixinKeyEvent.setKeyname(strBixinname);
            addBixinKeyEvent.setKeyaddress(strSweep);
            addEventsDatas.add(addBixinKeyEvent);
            AddBixinKeyAdapter addBixinKeyAdapter = new AddBixinKeyAdapter(addEventsDatas);
            reclBinxinKey.setAdapter(addBixinKeyAdapter);
            int cosignerNum = Integer.parseInt(strInditor2);
            bnCompleteAddCosigner.setText(String.format(Locale.CHINA, getResources().getString(R.string.finish) + "（%d-%d)", addEventsDatas.size(), cosignerNum));
            if (addEventsDatas.size() == cosignerNum) {
                bnCompleteAddCosigner.setEnabled(true);
                bnCompleteAddCosigner.setBackground(getResources().getDrawable(R.drawable.little_radio_blue));
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
                            bnCompleteAddCosigner.setText(String.format(Locale.CHINA, getResources().getString(R.string.finish) + "（%d-%d)", addEventsDatas.size(), cosignerNum));
                            bnCompleteAddCosigner.setBackground(getResources().getDrawable(R.drawable.little_radio_qian));
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Scan QR code / barcode return
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                edit_sweep.setText(content);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
