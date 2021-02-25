package org.haobtc.onekey.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import org.haobtc.onekey.R
import org.haobtc.onekey.bean.RemoteImage
import org.haobtc.onekey.bean.TokenList

/**
 *
 * @Description:     java类作用描述
 * @Author:         peter Qin
 *
 */
class SelectTokenAdapter(data: List<TokenList.ERCToken>) :
    BaseQuickAdapter<TokenList.ERCToken, BaseViewHolder>(R.layout.item_select_token,data) {


  override fun convert(helper: BaseViewHolder, item: TokenList.ERCToken) {
    helper.setText(R.id.name, item.symbol)
    RemoteImage(item.icon).intoTarget(helper.getView(R.id.icon))
  }
}
