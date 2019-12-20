package org.haobtc.wallet.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.haobtc.wallet.R;
import org.haobtc.wallet.bean.InputOutputAddressEvent;

import java.util.List;

public class SinatrayPersonAdapetr extends BaseQuickAdapter<InputOutputAddressEvent, BaseViewHolder> {
    public SinatrayPersonAdapetr(@Nullable List<InputOutputAddressEvent> data) {
        super(R.layout.sinatroy_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, InputOutputAddressEvent item) {
        helper.setText(R.id.tet_sinatroyName,item.getNum()).setText(R.id.tet_sinatroy,item.getAddress());

    }
}
