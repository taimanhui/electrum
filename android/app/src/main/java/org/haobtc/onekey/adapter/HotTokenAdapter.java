package org.haobtc.onekey.adapter;

import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import androidx.annotation.Nullable;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import java.util.List;
import java.util.Locale;
import org.haobtc.onekey.R;
import org.haobtc.onekey.bean.TokenList;

/** @Description: 热门代币的适配器 @Author: peter Qin */
public class HotTokenAdapter extends BaseQuickAdapter<TokenList.ERCToken, BaseViewHolder> {

    private onHotSwitchClick mOnHotSwitchClick;

    public HotTokenAdapter(
            @Nullable List<TokenList.ERCToken> data, onHotSwitchClick mOnHotSwitchClick) {
        super(R.layout.item_token_list, data);
        this.mOnHotSwitchClick = mOnHotSwitchClick;
    }

    @Override
    protected void convert(BaseViewHolder helper, TokenList.ERCToken item) {
        RelativeLayout backGround = helper.getView(R.id.back_layout);
        Switch switchBtn = helper.getView(R.id.switch_btn);
        helper.setText(R.id.token_name, item.symbol);
        String start = item.address.substring(0, 8);
        String end = item.address.substring(item.address.length() - 8);
        helper.setText(
                R.id.token_address, String.format(Locale.getDefault(), "%s...%s", start, end));
        setAddData(item, backGround);
        switchBtn.setChecked(item.isAdd == 0);
        switchBtn.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            item.isAdd = 0;
                        } else {
                            item.isAdd = -1;
                        }
                        setAddData(item, backGround);
                        mOnHotSwitchClick.onHotCheckedListener(
                                item, isChecked, helper.getAdapterPosition());
                    }
                });
    }

    private void setAddData(TokenList.ERCToken item, RelativeLayout backGround) {
        if (item.isAdd == 0) {
            backGround.setBackgroundColor(mContext.getColor(R.color.color_F9F9FB));
        } else {
            backGround.setBackground(null);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public interface onHotSwitchClick {
        void onHotCheckedListener(TokenList.ERCToken item, boolean isChecked, int position);
    }
}
