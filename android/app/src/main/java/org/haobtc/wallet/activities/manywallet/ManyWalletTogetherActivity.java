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

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.SendOne2OneMainPageActivity;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.IndicatorSeekBar;

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
    @BindView(R.id.rel_TwoNext)
    RelativeLayout relTwoNext;
    private Dialog dialogBtom;
    private RxPermissions rxPermissions;
    private static final int REQUEST_CODE = 0;
    private EditText edit_sweep;

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

    @OnClick({R.id.img_back, R.id.button, R.id.bn_add_key})
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
        }
    }

    private void mCreatWalletNext() {
        String strInditor1 = tvIndicator.getText().toString();
        String strInditor2 = tvIndicatorTwo.getText().toString();
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
        relTwoNext.setVisibility(View.VISIBLE);
        tetTrtwo.setTextColor(getResources().getColor(R.color.button_bk_disableok));

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
                if (!TextUtils.isEmpty(strLine)){
                    view.findViewById(R.id.btn_ConfirmAll).setVisibility(View.GONE);
                    view.findViewById(R.id.lin_ComfirmAll).setVisibility(View.VISIBLE);
                }else{
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

}
