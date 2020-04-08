package org.haobtc.wallet.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import org.haobtc.wallet.R;
import org.haobtc.wallet.bean.HardwareFeatures;

import java.util.List;

public class BixinkeyManagerAdapter extends BaseQuickAdapter<HardwareFeatures, BaseViewHolder> {
    public BixinkeyManagerAdapter( @Nullable List<HardwareFeatures> data) {
        super(R.layout.bixinkey_check_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, HardwareFeatures item) {
        helper.setText(R.id.tet_keyName,item.getBleName());

    }
}
