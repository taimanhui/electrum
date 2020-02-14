package org.haobtc.wallet.activities.onlywallet;

import android.view.View;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.HelpWordAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VerificationMnemonicWordActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.recl_helpWord)
    RecyclerView reclHelpWord;

    @Override
    public int getLayoutId() {
        return R.layout.activity_verification_mnemonic_word;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            strings.add("mine");
        }
        reclHelpWord.setLayoutManager(new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL));
        HelpWordAdapter helpWordAdapter = new HelpWordAdapter(strings);
        reclHelpWord.setAdapter(helpWordAdapter);
        helpWordAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mIntent(AppWalletCreateFinishActivity.class);
            }
        });

    }

    @Override
    public void initData() {

    }


    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
        }
    }
}
