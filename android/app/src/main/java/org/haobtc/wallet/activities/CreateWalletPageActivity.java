package org.haobtc.wallet.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.haobtc.wallet.R;
import org.haobtc.wallet.utils.CommonUtils;

public class CreateWalletPageActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editTextWalletName;
    private TextView textViewCosigner, textViewSigNum;
    private PopupWindow popupWindow;
    private View rootView;
    private TextView textViewCancel, textViewConfirm;
    private NumberPicker numberPicker;
    public static final String WALLET_NAME = "org.haobtc.coldwallet.activities.walletName";
    public static final String COSIGNER_NUM = "org.haobtc.coldwallet.activities.cosignerNum";
    public static final String SIGNUM_REQUIRE = "org.haobtc.coldwallet.activities.sigNumRequire";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_wallet_page);
        initView();
    }

    private void initView() {
        CommonUtils.enableToolBar(this, R.string.create);
        editTextWalletName = findViewById(R.id.wallet_name_setting);
        textViewCosigner = findViewById(R.id.cosigner_setting);
        textViewSigNum = findViewById(R.id.sig_num_setting);
        Button buttonNext = findViewById(R.id.bn_create_multi_next);
        rootView = LayoutInflater.from(this).inflate(R.layout.create_wallet_page, null);
        textViewCosigner.setOnClickListener(this);
        textViewSigNum.setOnClickListener(this);
        buttonNext.setOnClickListener(this);

    }
    // close qwer if present
    private void closeSoftInputFrom() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }

    }

    private void showPopupCoSigner() {

        View view = LayoutInflater.from(this).inflate(R.layout.select_cosigner_num_popwindow, null);//PopupWindow对象
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
        popupWindow = new PopupWindow(this);//初始化PopupWindow对象
        popupWindow.setBackgroundDrawable(new BitmapDrawable(null,"")); // 必须写在showAtLocation前面
        popupWindow.setContentView(view);//设置PopupWindow布局文件
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);//设置PopupWindow宽
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);//设置PopupWindow高
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(() ->
                {
                   // Toast.makeText(CreateWalletPageActivity.this, "PupWindow消失了！", Toast.LENGTH_SHORT).show();
                    setBackgroundAlpha(1f);
                }
        );
    }
    private void showPopupSigNum() {


        View view = LayoutInflater.from(this).inflate(R.layout.addsig_num_popwindow, null);//PopupWindow对象
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
        popupWindow = new PopupWindow(this);//初始化PopupWindow对象
        popupWindow.setBackgroundDrawable(new BitmapDrawable(null,"")); // 必须写在showAtLocation前面
        popupWindow.setContentView(view);//设置PopupWindow布局文件
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);//设置PopupWindow宽
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);//设置PopupWindow高
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(() ->
                {
                   // Toast.makeText(CreateWalletPageActivity.this, "PupWindow消失了！", Toast.LENGTH_SHORT).show();
                    setBackgroundAlpha(1f);

                }
        );
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_create_multi_next:
                String name = editTextWalletName.getText().toString();
                int cosinerNum;
                int sigNum;
                try {
                    cosinerNum = Integer.valueOf(textViewCosigner.getText().toString());
                    sigNum = Integer.valueOf(textViewSigNum.getText().toString());
                }   catch (NumberFormatException e) {
                    Toast.makeText(CreateWalletPageActivity.this, "联署人数量、所需签名数量均不能为空", Toast.LENGTH_SHORT).show();
                    break;
                }
                if (name.length() > 0 && name.length() < 6 && (sigNum <= cosinerNum)) {
                    Intent intent = new Intent(this, CoSignerAddActivity.class);
                    intent.putExtra(WALLET_NAME, name);
                    intent.putExtra(COSIGNER_NUM, cosinerNum);
                    intent.putExtra(SIGNUM_REQUIRE, sigNum);
                    startActivity(intent);
                } else {
                    Toast.makeText(CreateWalletPageActivity.this, "名字不能为空、所需签名数量不能大于联署人数量", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.cosigner_setting:
                closeSoftInputFrom();
                showPopupCoSigner();
                setBackgroundAlpha(0.5f);
                break;
            case R.id.sig_num_setting:
                closeSoftInputFrom();
                showPopupSigNum();
                setBackgroundAlpha(0.5f);
                break;
            case R.id.confirm_cosigner:
                textViewCosigner.setText(String.valueOf(numberPicker.getValue()));
                popupWindow.dismiss();
                break;
            case R.id.confirm_sig_num:
                textViewSigNum.setText(String.valueOf(numberPicker.getValue()));
                popupWindow.dismiss();
            default:
                popupWindow.dismiss();

        }
    }
    public void setBackgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp =  getWindow()
                .getAttributes();
        lp.alpha = bgAlpha;
        getWindow().setAttributes(lp);
    }
}
