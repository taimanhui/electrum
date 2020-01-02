package org.haobtc.wallet.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.MyDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateWalletPageActivity extends BaseActivity {
    @BindView(R.id.lin_Cosinger)
    LinearLayout linCosinger;
    @BindView(R.id.lin_sigNumseting)
    LinearLayout linSigNumseting;
    @BindView(R.id.img_backCreat)
    ImageView imgBackCreat;
    @BindView(R.id.wallet_name_setting)
    EditText editTextWalletName;
    @BindView(R.id.cosigner_setting)
    TextView textViewCosigner;
    @BindView(R.id.sig_num_setting)
    TextView textViewSigNum;
    @BindView(R.id.bn_create_multi_next)
    Button bnCreateMultiNext;
    @BindView(R.id.rel_hideKey)
    RelativeLayout relHideKey;
    private NumberPicker numberPicker;
    public static final String WALLET_NAME = "org.haobtc.wallet.activities.walletName";
    public static final String COSIGNER_NUM = "org.haobtc.wallet.activities.cosignerNum";
    public static final String SIGNUM_REQUIRE = "org.haobtc.wallet.activities.sigNumRequire";
    private Dialog dialogBtom;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    break;
                case 2:

                    break;
            }
        }
    };
    private MyDialog myDialog;
    private int consigerValue = 0;
    private int signumValue = 0;

    @Override
    public int getLayoutId() {
        return R.layout.create_wallet_page;
    }

    @Override
    public void initView() {
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        myDialog = MyDialog.showDialog(CreateWalletPageActivity.this);

    }


    @Override
    public void initData() {

    }

    private void showPopupCoSigner(Context context, @LayoutRes int resource) {
        //set view
        View view = View.inflate(context, resource, null);
        dialogBtom = new Dialog(context, R.style.dialog);
        numberPicker = view.findViewById(R.id.np_cosigner);
        CommonUtils.setNumberPickerDividerColor(this, numberPicker);
        numberPicker.setMinValue(2);
        numberPicker.setMaxValue(15);
        NumberPicker.OnValueChangeListener onValueChangeListener =
                (np, i, i1) -> {
           /* Toast.makeText(CreateWalletPageActivity.this,
                        "selected number " + np.getValue(), Toast.LENGTH_SHORT).show();*/
                };
        numberPicker.setOnValueChangedListener(onValueChangeListener);
        view.findViewById(R.id.cancel_cosigner).setOnClickListener(v -> {
            dialogBtom.cancel();
        });
        view.findViewById(R.id.confirm_cosigner).setOnClickListener(v -> {
            consigerValue = numberPicker.getValue();
            if (signumValue != 0) {
                if (signumValue > consigerValue) {
                    Toast.makeText(this, R.string.dontsmall, Toast.LENGTH_SHORT).show();
                } else {
                    textViewCosigner.setText(String.valueOf(consigerValue));
                    dialogBtom.cancel();
                }
            } else {
                textViewCosigner.setText(String.valueOf(consigerValue));
                dialogBtom.cancel();
            }
        });

        dialogBtom.setContentView(view);
        Window window = dialogBtom.getWindow();
        //Set pop-up size
        window.setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        //set locate
        window.setGravity(Gravity.BOTTOM);
        //set animal
        window.setWindowAnimations(R.style.AnimBottom);
        dialogBtom.show();

    }

    private void showPopupSigNum(Context context, @LayoutRes int resource) {
        //set view
        View view = View.inflate(context, resource, null);
        dialogBtom = new Dialog(context, R.style.dialog);
        numberPicker = view.findViewById(R.id.np_sig_num);
        CommonUtils.setNumberPickerDividerColor(this, numberPicker);
        numberPicker.setMinValue(2);
        numberPicker.setMaxValue(15);
        NumberPicker.OnValueChangeListener onValueChangeListener =
                (np, i, i1) -> {
                       /* Toast.makeText(CreateWalletPageActivity.this,
                            "selected number "+np.getValue(), Toast.LENGTH_SHORT).show();*/
                    //   numberPicker.setValue(i1);
                };
        numberPicker.setOnValueChangedListener(onValueChangeListener);
        view.findViewById(R.id.cancel_sig_num).setOnClickListener(v -> {
            dialogBtom.cancel();
        });
        view.findViewById(R.id.confirm_sig_num).setOnClickListener(v -> {
            signumValue = numberPicker.getValue();
            if (consigerValue != 0) {
                if (signumValue > consigerValue) {
                    Toast.makeText(this, R.string.dontbig, Toast.LENGTH_SHORT).show();
                } else {
                    textViewSigNum.setText(String.valueOf(signumValue));
                    dialogBtom.cancel();
                }
            } else {
                textViewSigNum.setText(String.valueOf(signumValue));
                dialogBtom.cancel();
            }
        });
        dialogBtom.setContentView(view);
        Window window = dialogBtom.getWindow();
        //Set pop-up size
        window.setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        //set locate
        window.setGravity(Gravity.BOTTOM);
        //set animal
        window.setWindowAnimations(R.style.AnimBottom);
        dialogBtom.show();

    }


    //creatwallet
    private void creatWalletjson(String name, int cosinerNum, int sigNum) {
        myDialog.show();
        try {
            Daemon.commands.callAttr("set_multi_wallet_info", name, cosinerNum, sigNum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        myDialog.dismiss();
        Intent intent = new Intent(this, CoSignerAddActivity.class);
        intent.putExtra(WALLET_NAME, name);
        intent.putExtra(COSIGNER_NUM, cosinerNum);
        intent.putExtra(SIGNUM_REQUIRE, sigNum);
        startActivity(intent);
    }


    @OnClick({R.id.lin_Cosinger, R.id.lin_sigNumseting, R.id.img_backCreat, R.id.bn_create_multi_next,R.id.rel_hideKey})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.lin_Cosinger:
                showPopupCoSigner(CreateWalletPageActivity.this, R.layout.select_cosigner_num_popwindow);
                break;
            case R.id.lin_sigNumseting:
                showPopupSigNum(CreateWalletPageActivity.this, R.layout.addsig_num_popwindow);
                break;
            case R.id.bn_create_multi_next:
                String name = editTextWalletName.getText().toString();
                String tetCosinger = textViewCosigner.getText().toString();
                String tetSingnum = textViewSigNum.getText().toString();

                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(tetCosinger) || TextUtils.isEmpty(tetSingnum) || tetCosinger.equals("请选择") || tetSingnum.equals("请选择")) {
                    Toast.makeText(this, R.string.dongt_isnull, Toast.LENGTH_SHORT).show();

                } else {
                    int cosinerNum = Integer.valueOf(tetCosinger);
                    int sigNum = Integer.valueOf(tetSingnum);
                    if (sigNum < 2 || cosinerNum < 2) {
                        Toast.makeText(this, "联署人数、签名人数均不可小于2", Toast.LENGTH_SHORT).show();
                    } else {
                        if ((sigNum <= cosinerNum)) {
                            creatWalletjson(name, cosinerNum, sigNum);

                        } else {
                            Toast.makeText(CreateWalletPageActivity.this, R.string.format_iswrong, Toast.LENGTH_SHORT).show();
                        }
                    }

                }

                break;
            case R.id.img_backCreat:
                finish();
                break;
            case R.id.rel_hideKey:
                hideKeyboard();
                break;
        }
    }
    //hideKeyboard
    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

}
