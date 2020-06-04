package org.haobtc.wallet.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.haobtc.wallet.R;

import java.util.List;

public class CustomElectrumAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public CustomElectrumAdapter(@Nullable List<String> data) {
        super(R.layout.only_node, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.tet_electrumName, item);
        helper.addOnClickListener(R.id.linear_delete);
    }
}
