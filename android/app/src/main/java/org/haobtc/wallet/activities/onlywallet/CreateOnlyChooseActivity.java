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
        tetPersonalNum.setText(String.format("%s(0/%d)", getResources().getString(R.string.creat_personal),sigNum));
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
                showSelectFeeDialogs(CreateOnlyChooseActivity.this, R.layout.bluetooce_nfc);
                break;
            case R.id.bn_complete_add_cosigner:
                Intent intent = new Intent(CreateOnlyChooseActivity.this, CreatFinishPersonalActivity.class);
                startActivity(intent);
                break;
        }
    }
    private void showSelectFeeDialogs(Context context, @LayoutRes int resource) {
        //set see view
        View view = View.inflate(context, resource, null);
        dialogBtom = new Dialog(context, R.style.dialog);

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

}
