package org.haobtc.wallet.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.haobtc.wallet.R;

import java.util.List;

public class SendmoreAddressAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    public SendmoreAddressAdapter(@Nullable List<String> data) {
        super(R.layout.vout_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.address_to,item);

    }
}
