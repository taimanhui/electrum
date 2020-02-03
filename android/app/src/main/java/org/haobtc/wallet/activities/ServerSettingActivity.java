package org.haobtc.wallet.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.jessyan.autosize.utils.ScreenUtils;

public class ServerSettingActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.switch_cynchronez)
    Switch switchCynchronez;
    @BindView(R.id.recl_nodeChose)
    RecyclerView reclNodeChose;
    @BindView(R.id.tet_addNode)
    TextView tetAddNode;

    public int getLayoutId() {
        return R.layout.server_setting;
    }

    public void initView() {
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_back, R.id.tet_addNode})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_addNode:
                dialogAddnode();
                break;
        }
    }

    private void dialogAddnode() {
        View view = LayoutInflater.from(this).inflate(R.layout.add_node_layout, null, false);
        AlertDialog alertDialog = new AlertDialog.Builder(this).setView(view).create();
        ImageView img_Cancle = view.findViewById(R.id.cancel_select_wallet);
        img_Cancle.setOnClickListener(v -> {
            alertDialog.dismiss();
        });
        alertDialog.show();

    }

}
