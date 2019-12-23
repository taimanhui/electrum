package org.haobtc.wallet.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.haobtc.wallet.R;
import org.haobtc.wallet.event.SendMoreAddressEvent;

import java.util.List;

public class SendmoreAddressAdapter extends BaseQuickAdapter<SendMoreAddressEvent, BaseViewHolder> {
    public SendmoreAddressAdapter(@Nullable List<SendMoreAddressEvent> data) {
        super(R.layout.vout_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, SendMoreAddressEvent item) {
        helper.setText(R.id.address_to,item.getInputAddress()).setText(R.id.amount_item,item.getInputAmount()+" BTC");

    }
}
