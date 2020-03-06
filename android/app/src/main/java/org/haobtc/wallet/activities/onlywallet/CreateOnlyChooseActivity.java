package org.haobtc.wallet.activities.onlywallet;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.recyclerview.widget.RecyclerView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.SendOne2OneMainPageActivity;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.manywallet.CustomerDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CreateOnlyChooseActivity extends BaseActivity {

    @BindView(R.id.img_backCreat)
    ImageView imgBackCreat;
    @BindView(R.id.tet_personalNum)
    TextView tetPersonalNum;
    @BindView(R.id.recl_BinxinKey)
    RecyclerView reclBinxinKey;
    @BindView(R.id.bn_add_key)
    LinearLayout bnAddKey;
    @BindView(R.id.bn_complete_add_cosigner)
    Button bnCompleteAddCosigner;
    private Dialog dialogBtom;
    private int sigNum;

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_only_choose;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        Intent intent = getIntent();
        sigNum = intent.getIntExtra("sigNum", 0);
        init();

    }

    @SuppressLint("DefaultLocale")
    private void init() {
        tetPersonalNum.setText(String.format("%s(0/%d)", getResources().getString(R.string.creat_personal), sigNum));
    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_backCreat, R.id.bn_add_key, R.id.bn_complete_add_cosigner})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_backCreat:
                finish();
                break;
            case R.id.bn_add_key:
                // new version code
                showPopupAddCosigner1();
                break;
            case R.id.bn_complete_add_cosigner:
                Intent intent = new Intent(CreateOnlyChooseActivity.this, CreatFinishPersonalActivity.class);
                startActivity(intent);
                break;
        }
    }

    private void showPopupAddCosigner1() {
        CustomerDialogFragment dialogFragment = new CustomerDialogFragment("", null, "");
        dialogFragment.show(getSupportFragmentManager(), "");
    }

}
