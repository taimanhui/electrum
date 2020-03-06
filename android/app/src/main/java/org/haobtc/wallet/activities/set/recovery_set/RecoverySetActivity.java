package org.haobtc.wallet.activities.set.recovery_set;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class RecoverySetActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {


    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_backups)
    TextView tetBackups;
    @BindView(R.id.btn_setPin)
    Button btnSetPin;
    @BindView(R.id.checkbox_Know)
    CheckBox checkboxKnow;

    @Override
    public int getLayoutId() {
        return R.layout.activity_recovery_set;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {
        checkboxKnow.setOnCheckedChangeListener(this);

    }

    @OnClick({R.id.img_back, R.id.tet_backups, R.id.btn_setPin})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_backups:
                mIntent(Backup_recoveryActivity.class);
                break;
            case R.id.btn_setPin:
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked){
            btnSetPin.setBackground(getDrawable(R.drawable.button_bk));
            btnSetPin.setEnabled(true);
        }else{
            btnSetPin.setBackground(getDrawable(R.drawable.button_bk_grey));
            btnSetPin.setEnabled(false);
        }
    }
}
