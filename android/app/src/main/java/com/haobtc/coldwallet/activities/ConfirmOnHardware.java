package com.haobtc.coldwallet.activities;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.haobtc.coldwallet.R;
import com.haobtc.coldwallet.fragment.ItemFragmentTransaction;
import com.haobtc.coldwallet.utils.CommonUtils;
public class ConfirmOnHardware extends AppCompatActivity implements View.OnClickListener {
    private Button button_confirm;
    private PopupWindow popupWindow;
    private View view, rootView;
    private ImageView imageViewCancle, imageViewConnect, imageViewPin, imageViewSigning;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.confirm_on_hardware);
        initView();

    }

    private void initView() {
        CommonUtils.enableToolBar(this, R.string.confirm_trans_d);
        ItemFragmentTransaction fragmentTransaction = new ItemFragmentTransaction();
        fragmentTransaction.setArguments(getIntent().getExtras());
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_trans_in_confirm, fragmentTransaction).commit();
        rootView = LayoutInflater.from(this).inflate(R.layout.confirm_on_hardware, null);
        button_confirm = findViewById(R.id.confirm_on_hardware);
        button_confirm.setOnClickListener(v -> {
            showPopupSignProcessing();
            setBackgroundAlpha(0.5f);

        });
    }
    private void showPopupSignProcessing() {
        view = LayoutInflater.from(this).inflate(R.layout.touch_process_popupwindow, null);
        imageViewCancle = view.findViewById(R.id.cancel_touch);
        imageViewConnect = view.findViewById(R.id.imageView_connect);
        imageViewPin = view.findViewById(R.id.imageView_pin);
        imageViewSigning = view.findViewById(R.id.imageView_signing);
        popupWindow = new PopupWindow();
        popupWindow.setBackgroundDrawable(new BitmapDrawable(null, ""));
        popupWindow.setContentView(view);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(() ->
                    {
                        Toast.makeText(ConfirmOnHardware.this, "PupWindow消失了！", Toast.LENGTH_SHORT).show();
                        setBackgroundAlpha(1f);
                    }
            );
        imageViewCancle.setOnClickListener(this);
    }

    private void showPopupSignFailed() {
        view = LayoutInflater.from(this).inflate(R.layout.signature_fail_popupwindow, null);
        imageViewCancle = view.findViewById(R.id.cancel_sign_fail);
        popupWindow = new PopupWindow();
        popupWindow.setBackgroundDrawable(new BitmapDrawable(null, ""));
        popupWindow.setContentView(view);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(() ->
                {
                    Toast.makeText(ConfirmOnHardware.this, "PupWindow消失了！", Toast.LENGTH_SHORT).show();
                    setBackgroundAlpha(1f);
                }
        );
        imageViewCancle.setOnClickListener(this);
    }
    private void showPopupSignTimeout() {
        Button button = findViewById(R.id.sign_again);
        view = LayoutInflater.from(this).inflate(R.layout.signature_timeout_popupwindow, null);
        imageViewCancle = view.findViewById(R.id.cancel_sign_timeout);
        popupWindow = new PopupWindow();
        popupWindow.setBackgroundDrawable(new BitmapDrawable(null, ""));
        popupWindow.setContentView(view);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(() ->
                {
                    Toast.makeText(ConfirmOnHardware.this, "PupWindow消失了！", Toast.LENGTH_SHORT).show();
                    setBackgroundAlpha(1f);
                }
        );
        imageViewCancle.setOnClickListener(this);
        button.setOnClickListener(this);
    }

    private void showPopupSignException() {
        view = LayoutInflater.from(this).inflate(R.layout.signature_exception_popupwindow, null);
        imageViewCancle = view.findViewById(R.id.cancel_sign_exception);
        popupWindow = new PopupWindow();
        popupWindow.setBackgroundDrawable(new BitmapDrawable(null, ""));
        popupWindow.setContentView(view);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setOnDismissListener(() ->
                {
                    Toast.makeText(ConfirmOnHardware.this, "PupWindow消失了！", Toast.LENGTH_SHORT).show();
                    setBackgroundAlpha(1f);
                }
        );
        imageViewCancle.setOnClickListener(this);
    }

    public void setBackgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp =  getWindow()
                .getAttributes();
        lp.alpha = bgAlpha;
        getWindow().setAttributes(lp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.sign_again:
                // todo:超时重新签名逻辑

            default:
                popupWindow.dismiss();
                finish();
        }

    }
}
