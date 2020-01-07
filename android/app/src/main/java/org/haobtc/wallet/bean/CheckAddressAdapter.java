package org.haobtc.wallet.bean;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.haobtc.wallet.R;
import org.haobtc.wallet.event.SendMoreAddressEvent;

import java.util.List;

public class CheckAddressAdapter extends BaseQuickAdapter<SendMoreAddressEvent, BaseViewHolder> {

    public CheckAddressAdapter(@Nullable List<SendMoreAddressEvent> data) {
        super(R.layout.moreaddress_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, SendMoreAddressEvent item) {
        helper.setText(R.id.tet_moreaddress,item.getInputAddress()).setText(R.id.tet_payNum,item.getInputAmount());

    }
}
