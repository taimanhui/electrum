package org.haobtc.wallet.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.haobtc.wallet.R;

import java.util.List;

public class BixinkeyManagerAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public BixinkeyManagerAdapter( @Nullable List<String> data) {
        super(R.layout.bixinkey_check_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.tet_keyName,item);

    }
}
