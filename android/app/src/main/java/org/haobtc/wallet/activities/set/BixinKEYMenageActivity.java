package org.haobtc.wallet.activities.set;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.BixinkeyManagerAdapter;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BixinKEYMenageActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_Add)
    TextView tetAdd;
    @BindView(R.id.recl_bixinKey_list)
    RecyclerView reclBixinKeyList;

    @Override
    public int getLayoutId() {
        return R.layout.activity_bixin_keymenage;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {
        ArrayList<String> strings = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            strings.add("BixinKEY"+i);
        }
        BixinkeyManagerAdapter bixinkeyManagerAdapter = new BixinkeyManagerAdapter(strings);
        reclBixinKeyList.setAdapter(bixinkeyManagerAdapter);

        bixinkeyManagerAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                mIntent(SomemoreActivity.class);
            }
        });

    }

    @OnClick({R.id.img_back, R.id.tet_Add})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.tet_Add:

                break;
        }
    }
}







