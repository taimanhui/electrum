package org.haobtc.onekey.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.haobtc.onekey.R
import org.haobtc.onekey.bean.Assets

/**
 *
 * @Description:     java类作用描述
 * @Author:         peter Qin
 *
 */
class SelectTokenAdapter(data: List<Assets>) :
    BaseQuickAdapter<Assets, BaseViewHolder>(R.layout.item_select_token, data) {


  override fun convert(helper: BaseViewHolder, item: Assets) {
    helper.setText(R.id.name, item.name)
    item.logo.let {
      item.logo.intoTarget(helper.getView(R.id.icon))
    }
    item.balanceFiat.let {
      helper.setText(R.id.fait_num, String.format("%s %s", item.balanceFiat.symbol, item.balanceFiat.balanceFormat))
    }
    item.balance.let {
      helper.setText(R.id.balance_num, item.balance.balance.toPlainString())
    }
  }
}
