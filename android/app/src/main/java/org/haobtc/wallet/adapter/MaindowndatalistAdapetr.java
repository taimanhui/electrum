package org.haobtc.wallet.adapter;

import androidx.annotation.Nullable;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.haobtc.wallet.R;

import java.util.List;

public class MaindowndatalistAdapetr extends BaseQuickAdapter<String,BaseViewHolder> {


    public MaindowndatalistAdapetr(@Nullable List<String> data) {
        super(R.layout.fragment_item_trans, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.tet_address,item);
    }
}
