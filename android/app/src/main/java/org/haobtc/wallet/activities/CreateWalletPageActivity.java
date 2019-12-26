package org.haobtc.wallet.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.MyDialog;

import java.util.ArrayList;

public class CreateWalletPageActivity extends BaseActivity implements View.OnClickListener {
    private EditText editTextWalletName;
    private TextView textViewCosigner, textViewSigNum;
    private View rootView;
    private TextView textViewCancel, textViewConfirm;
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
    private ImageView imgBack;
    private Button buttonNext;

    @Override
    public int getLayoutId() {
        return R.layout.create_wallet_page;
    }

    @Override
    public void initView() {
        myDialog = MyDialog.showDialog(CreateWalletPageActivity.this);
        editTextWalletName = findViewById(R.id.wallet_name_setting);
        textViewCosigner = findViewById(R.id.cosigner_setting);
        textViewSigNum = findViewById(R.id.sig_num_setting);
        imgBack = findViewById(R.id.img_backCreat);
        buttonNext = findViewById(R.id.bn_create_multi_next);
        rootView = LayoutInflater.from(this).inflate(R.layout.create_wallet_page, null);
        textViewCosigner.setOnClickListener(this);
        textViewSigNum.setOnClickListener(this);
        buttonNext.setOnClickListener(this);
        imgBack.setOnClickListener(this);
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
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(20);
        NumberPicker.OnValueChangeListener onValueChangeListener =
                (np, i, i1) -> {
           /* Toast.makeText(CreateWalletPageActivity.this,
                        "selected number " + np.getValue(), Toast.LENGTH_SHORT).show();*/
                };
        numberPicker.setOnValueChangedListener(onValueChangeListener);
        textViewCancel = view.findViewById(R.id.cancel_cosigner);
        textViewConfirm = view.findViewById(R.id.confirm_cosigner);
        textViewCancel.setOnClickListener(this);
        textViewConfirm.setOnClickListener(this);
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
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(15);
        NumberPicker.OnValueChangeListener onValueChangeListener =
                (np, i, i1) -> {
                       /* Toast.makeText(CreateWalletPageActivity.this,
                            "selected number "+np.getValue(), Toast.LENGTH_SHORT).show();*/
                    //   numberPicker.setValue(i1);
                };
        numberPicker.setOnValueChangedListener(onValueChangeListener);
        textViewCancel = view.findViewById(R.id.cancel_sig_num);
        textViewConfirm = view.findViewById(R.id.confirm_sig_num);
        textViewCancel.setOnClickListener(this);
        textViewConfirm.setOnClickListener(this);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
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
            case R.id.cosigner_setting:
                showPopupCoSigner(CreateWalletPageActivity.this, R.layout.select_cosigner_num_popwindow);
                break;
            case R.id.sig_num_setting:
                showPopupSigNum(CreateWalletPageActivity.this, R.layout.addsig_num_popwindow);
                break;
            case R.id.cancel_sig_num:
                dialogBtom.cancel();
            case R.id.confirm_sig_num:
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

        }
    }

    //creatwallet
    private void creatWalletjson(String name, int cosinerNum, int sigNum) {
        myDialog.show();
        Daemon.commands.callAttr("set_multi_wallet_info", name, cosinerNum, sigNum);
        myDialog.dismiss();
        Intent intent = new Intent(this, CoSignerAddActivity.class);
        intent.putExtra(WALLET_NAME, name);
        intent.putExtra(COSIGNER_NUM, cosinerNum);
        intent.putExtra(SIGNUM_REQUIRE, sigNum);
        startActivity(intent);
    }

}
