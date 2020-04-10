package org.haobtc.wallet.activities.settings;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.QuetationChooseAdapter;
import org.haobtc.wallet.bean.CNYBean;
import org.haobtc.wallet.event.FirstEvent;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class BlockChooseActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.recl_Quetation)
    RecyclerView reclQuetation;
    private SharedPreferences.Editor edit;
    private int setBlock;


    @Override
    public int getLayoutId() {
        return R.layout.activity_block_choose;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        setBlock = preferences.getInt("setBlock", 0);
    }

    @Override
    public void initData() {
        String[] stringArray = getResources().getStringArray(R.array.blockline);
        ArrayList<CNYBean> blockList = new ArrayList<>();
        for (int i = 0; i < stringArray.length; i++) {
            CNYBean cnyBean = new CNYBean(stringArray[i], false);
            blockList.add(cnyBean);
        }
        QuetationChooseAdapter quetationChooseAdapter = new QuetationChooseAdapter(BlockChooseActivity.this,blockList,setBlock);
        reclQuetation.setAdapter(quetationChooseAdapter);
        quetationChooseAdapter.setOnLisennorClick(new QuetationChooseAdapter.onLisennorClick() {
            @Override
            public void ItemClick(int pos) {
                edit.putInt("setBlock",pos);
                edit.putString("blockServerLine",blockList.get(pos).getName());
                edit.apply();
                EventBus.getDefault().post(new FirstEvent("block_check"));
            }
        });
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





