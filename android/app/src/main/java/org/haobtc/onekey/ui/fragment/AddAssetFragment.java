package org.haobtc.onekey.ui.fragment;

import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import java.util.Arrays;
import org.haobtc.onekey.R;
import org.haobtc.onekey.constant.Vm;
import org.haobtc.onekey.ui.adapter.AssetAdapter;
import org.haobtc.onekey.ui.base.BaseFragment;

/** @author liyan */
public class AddAssetFragment extends BaseFragment {

    @BindView(R.id.asset_list)
    protected RecyclerView mAssetView;

    @Override
    public void init(View view) {
        AssetAdapter mAdapter = new AssetAdapter(getContext(), Arrays.asList(Vm.CoinType.values()));
        mAssetView.setLayoutManager(new LinearLayoutManager(getContext()));
        mAssetView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public int getContentViewId() {
        return R.layout.fragment_add_asset;
    }
}
