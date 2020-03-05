package org.haobtc.wallet.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.haobtc.wallet.R;
import org.haobtc.wallet.event.SendMoreAddressEvent;

import java.util.List;

public class HardwareAdapter extends BaseQuickAdapter<SendMoreAddressEvent, BaseViewHolder> {
    public HardwareAdapter(@Nullable List<SendMoreAddressEvent> data) {
        super(R.layout.hardware_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, SendMoreAddressEvent item) {
        helper.setText(R.id.tet_getAddress,item.getInputAddress()).setText(R.id.tet_getNum,item.getInputAmount());
    }
}
