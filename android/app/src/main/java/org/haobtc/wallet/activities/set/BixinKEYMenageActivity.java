package org.haobtc.wallet.activities.set;

import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.BixinkeyManagerAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    private Set<String> bixinKEYlist;

    @Override
    public int getLayoutId() {
        return R.layout.activity_bixin_keymenage;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        Set<String> set = new HashSet<>();
        set.add("BixinKEY-ORSPR");
        SharedPreferences.Editor edit = preferences.edit();
        edit.putStringSet("BixinKEYlist",set);
        edit.apply();
        bixinKEYlist = preferences.getStringSet("BixinKEYlist", null);


    }

    @Override
    public void initData() {
        if (bixinKEYlist!=null){
            List<String> keyList = new ArrayList<String>(bixinKEYlist);
            for(int i = 0 ; i < keyList.size() ; i++){
                Log.d("fetching values", "fetch value " + keyList.get(i));
            }
            BixinkeyManagerAdapter bixinkeyManagerAdapter = new BixinkeyManagerAdapter(keyList);
            reclBixinKeyList.setAdapter(bixinkeyManagerAdapter);
            bixinkeyManagerAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                    Intent intent = new Intent(BixinKEYMenageActivity.this, SomemoreActivity.class);
                    intent.putExtra("keyListItem",keyList.get(position));
                    startActivity(intent);
                }
            });
        }
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







