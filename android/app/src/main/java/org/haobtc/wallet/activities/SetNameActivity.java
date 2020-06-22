package org.haobtc.wallet.activities;

import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.aop.SingleClick;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class SetNameActivity extends BaseActivity {
    public static final String TAG = "org.haobtc.wallet.activities.SetNameActivity";
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.name_edit)
    EditText nameEdit;
    @BindView(R.id.next)
    Button next;
    @BindView(R.id.number)
    TextView number;

    @Override
    public int getLayoutId() {
        return R.layout.name_setting;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
    }

    @Override
    public void initData() {

    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finishAffinity();
                break;
            case R.id.next:
                Intent intent = new Intent(this, ActivatedProcessing.class);
                intent.putExtra("name", nameEdit.getText().toString());
                startActivity(intent);
                finish();
                break;
        }
    }

    @OnTextChanged(value = R.id.name_edit)
    public void onTextChanged(CharSequence text) {
        number.setText(String.format(Locale.ENGLISH, "%d/16", text.length()));
        if (text.length() == 16) {
            number.setTextColor(Color.RED);
        } else {
           number.setTextColor(Color.BLACK);
        }
    }
}
