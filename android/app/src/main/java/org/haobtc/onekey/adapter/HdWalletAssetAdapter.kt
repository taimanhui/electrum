package org.haobtc.onekey.adapter

import android.content.Context
import android.text.TextUtils
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.google.common.base.Strings
import org.haobtc.onekey.R
import org.haobtc.onekey.bean.BalanceCoinInfo
import org.haobtc.onekey.bean.RemoteImage
import org.haobtc.onekey.business.wallet.SystemConfigManager
import org.haobtc.onekey.constant.Vm
import java.math.BigDecimal
import java.math.RoundingMode

class HdWalletAssetAdapter(context: Context, data: List<BalanceCoinInfo?>?) : BaseQuickAdapter<BalanceCoinInfo, BaseViewHolder>(R.layout.all_assets_item, data) {
  private val mSystemConfigManager by lazy {
    SystemConfigManager(context.applicationContext)
  }

  override fun convert(helper: BaseViewHolder, item: BalanceCoinInfo) {
    when {
      item.coin.equals(Vm.CoinType.BTC.callFlag, true) -> {
        helper.setImageDrawable(R.id.imageView, ResourcesCompat.getDrawable(helper.itemView.resources, R.drawable.token_btc, null))
      }
      item.coin.equals(Vm.CoinType.ETH.callFlag, true) -> {
        helper.setImageDrawable(R.id.imageView, ResourcesCompat.getDrawable(helper.itemView.resources, R.drawable.token_eth, null))
      }
      else -> {
        RemoteImage(item.icon).intoTarget(helper.getView(R.id.imageView))
      }
    }

    val balance = if (!TextUtils.isEmpty(item.balance) && !item.balance.equals("0")) {
      BigDecimal(item.balance)
          .setScale(8, RoundingMode.DOWN)
          .stripTrailingZeros().toPlainString()
    } else {
      "0"
    }
    helper.setText(R.id.text_wallet_name, item.coin).setText(R.id.text_balance, balance)

    val strFiat = if (!TextUtils.isEmpty(item.fiat) && item.fiat == "0") {
      "0.00"
    } else {
      item.fiat.substring(0, item.fiat.indexOf(" "))
    }
    if (!Strings.isNullOrEmpty(strFiat)) {
      helper.setText(R.id.text_fiat, mSystemConfigManager.currentFiatSymbol + " " + strFiat)
    }

  }

}
