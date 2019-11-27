package com.haobtc.coldwallet.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.haobtc.coldwallet.R;
import com.haobtc.coldwallet.utils.CommonUtils;

import java.util.Locale;

public class SendOne2OneMainPageActivity extends AppCompatActivity implements View.OnClickListener {
    private LinearLayout selectSend, selectSigNum;
    private PopupWindow popupWindow;
    private View rootView;
    private EditText editTextComments, editAddress;
    private TextView bytesCount;
    private Button buttonCreate, buttonSweep, buttonPaste;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.send_one2one_main_page);
        initView();

    }

    private void initView() {

        CommonUtils.enableToolBar(this, R.string.send);
        selectSend = findViewById(R.id.llt_select_wallet);
        selectSend.setOnClickListener(v -> {
            showPopupSelectWallet();
            setBackgroundAlpha(0.5f);

        });
        selectSigNum = findViewById(R.id.fee_select);
        selectSigNum.setOnClickListener(v -> {
            showPopupSelectFee();
            setBackgroundAlpha(0.5f);
        });
        TextView textView = findViewById(R.id.tv_send2many);
        textView.setOnClickListener(v -> {
            Intent intent = new Intent(this, Send2ManyActivity.class);
            startActivity(intent);
        });
        rootView = LayoutInflater.from(this).inflate(R.layout.send_one2one_main_page, null);//父布局
        editTextComments = findViewById(R.id.comment_edit);
        editTextComments.addTextChangedListener(new MyTextWatcher(this, editTextComments, 20));
        editAddress = findViewById(R.id.edit_address_one2one);
        bytesCount = findViewById(R.id.byte_count);
        buttonCreate = findViewById(R.id.create_trans_one2one);
        buttonSweep = findViewById(R.id.bn_sweep_one2noe);
        buttonPaste = findViewById(R.id.bn_paste_one2one);
        buttonCreate.setOnClickListener(v -> {
            // todo: 创建交易，页面跳转
        });
        buttonSweep.setOnClickListener(v -> {
            // todo 扫描
        });
        buttonPaste.setOnClickListener(v -> {
            // todo:将粘贴板的数据粘贴到editText  editAddress.setText();

        });

    }
    private class MyTextWatcher implements TextWatcher {
        private Context context;

        private EditText editText;

        private int len; //允许输入的字节长度(一个中文占3个字节)

        MyTextWatcher(Context context, EditText editText, int len) {
            this.context = context;
            this.editText = editText;
            this.len = len;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

            String inputStr = editable.toString().trim();
            byte[] bytes = inputStr.getBytes();
            if (bytes.length < len) {
                bytesCount.setTextColor(getResources().getColor(R.color.text_color1));
            }
            bytesCount.setText(String.format(Locale.CHINA,"%d/20", bytes.length));
            if (bytes.length > len) {
                bytesCount.setTextColor(getResources().getColor(R.color.text_red));
                Toast.makeText(context, "超过规定字符数", Toast.LENGTH_SHORT).show();
                //取前20个字节
                byte[] newBytes = new byte[len];
                for (int i = 0; i < len; i++) {
                    newBytes[i] = bytes[i];  // todo:输入中文时有bug
                }
                String newStr = new String(newBytes);
                editText.setText(newStr);
                //将光标定位到最后
               Selection.setSelection(editText.getEditableText(), newStr.length());
            }

        }
    }
    public void setBackgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp =  getWindow()
                .getAttributes();
        lp.alpha = bgAlpha;
        getWindow().setAttributes(lp);
    }

    private void showPopupSelectFee() {
        Button button;
        ImageView imageView;
        AppCompatSeekBar seekBar;
        View view = LayoutInflater.from(this).inflate(R.layout.select_fee_popwindow, null);//PopupWindow对象
        button = view.findViewById(R.id.bn_fee);
        imageView = view.findViewById(R.id.cancel_select_fee);
        seekBar = view.findViewById(R.id.seek_bar_fee);
        TextView textViewFee;
        textViewFee = view.findViewById(R.id.fee);
        seekBar.setOnSeekBarChangeListener(new AppCompatSeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                   textViewFee.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast.makeText(SendOne2OneMainPageActivity.this, "触碰SeekBar", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(SendOne2OneMainPageActivity.this, "放开SeekBar", Toast.LENGTH_SHORT).show();

            }
        });
        popupWindow = new PopupWindow(this);//初始化PopupWindow对象
        popupWindow.setBackgroundDrawable(new BitmapDrawable(null,"")); // 必须写在showAtLocation前面
        popupWindow.setContentView(view);//设置PopupWindow布局文件
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);//设置PopupWindow宽
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);//设置PopupWindow高
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        popupWindow.setOutsideTouchable(true);
        button.setOnClickListener(this);//注册点击监听
        imageView.setOnClickListener(this);//注册点击监听
        popupWindow.setOnDismissListener(() ->
                {
                Toast.makeText(SendOne2OneMainPageActivity.this, "PupWindow消失了！", Toast.LENGTH_SHORT).show();
                setBackgroundAlpha(1f);
                }
        );
    }
    private void showPopupSelectWallet() {
        Button button;
        ImageView imageView;
        View view = LayoutInflater.from(this).inflate(R.layout.select_send_wallet_popwindow, null);//PopupWindow对象
        button = view.findViewById(R.id.bn_select_wallet);
        imageView = view.findViewById(R.id.cancel_select_wallet);
        popupWindow = new PopupWindow(this);//初始化PopupWindow对象
        popupWindow.setBackgroundDrawable(new BitmapDrawable(null,"")); // 必须写在showAtLocation前面
        popupWindow.setContentView(view);//设置PopupWindow布局文件
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);//设置PopupWindow宽
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);//设置PopupWindow高
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        popupWindow.setOutsideTouchable(true);
        button.setOnClickListener(this);//注册点击监听
        imageView.setOnClickListener(this);//注册点击监听
        popupWindow.setOnDismissListener(() ->
                {
                Toast.makeText(SendOne2OneMainPageActivity.this, "PupWindow消失了！", Toast.LENGTH_SHORT).show();
                setBackgroundAlpha(1f);
                }
        );
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_select_wallet:
                popupWindow.dismiss();
                finish();//调用Activity的finish方法退出应用程序
                break;
            case R.id.bn_fee:
                popupWindow.dismiss();//关闭PopupWindow
            default:
                popupWindow.dismiss();
        }
    }
}
