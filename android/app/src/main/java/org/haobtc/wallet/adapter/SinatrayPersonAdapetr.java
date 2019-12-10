package org.haobtc.wallet.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.haobtc.wallet.R;

import java.util.List;

public class SinatrayPersonAdapetr extends BaseQuickAdapter<String, BaseViewHolder> {
    public SinatrayPersonAdapetr(@Nullable List<String> data) {
        super(R.layout.sinatroy_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.tet_sinatroyName,item);
    }
}
