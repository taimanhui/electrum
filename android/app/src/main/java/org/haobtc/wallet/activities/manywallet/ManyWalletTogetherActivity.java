package org.haobtc.wallet.activities.manywallet;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
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

import androidx.annotation.LayoutRes;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.IndicatorSeekBar;

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

    @Override
    public int getLayoutId() {
        return R.layout.activity_many_wallet_together;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

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

    }

    private void showSelectFeeDialogs(Context context, @LayoutRes int resource) {
        //set see view
        View view = View.inflate(context, resource, null);
        dialogBtom = new Dialog(context, R.style.dialog);

        view.findViewById(R.id.tet_handInput).setOnClickListener(v -> {
            showInputDialogs(ManyWalletTogetherActivity.this, R.layout.bixinkey_input);
            dialogBtom.cancel();
        });
        //cancel dialog
        view.findViewById(R.id.img_Cancle).setOnClickListener(v -> {
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
        dialogBtom = new Dialog(context, R.style.dialog);

        //cancel dialog
        view.findViewById(R.id.img_cancle).setOnClickListener(v -> {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
