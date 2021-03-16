package org.haobtc.onekey.business.assetsLogo

import org.haobtc.onekey.R
import org.haobtc.onekey.activities.base.MyApplication
import org.haobtc.onekey.constant.Vm.CoinType

object AssetsLogo {

  @JvmStatic
  fun getLogoResources(coinType: CoinType?): Int {
    return when (coinType) {
      CoinType.ETH -> R.drawable.token_eth
      CoinType.BTC -> R.drawable.token_btc
      CoinType.BSC -> R.drawable.vector_token_bsc
      CoinType.HECO -> R.drawable.vector_token_heco
      else -> R.drawable.loco_round
    }
  }

  @JvmStatic
  fun getLogoDarkResources(coinType: CoinType?): Int {
    return when (coinType) {
      CoinType.ETH -> R.drawable.vector_dark_eth_icon
      CoinType.BTC -> R.drawable.vector_dark_btc_icon
      CoinType.BSC -> R.drawable.vector_dark_bsc_icon
      CoinType.HECO -> R.drawable.vector_dark_heco_icon
      else -> R.drawable.loco_round
    }
  }

  @JvmStatic
  fun getAssetDescribe(coinType: CoinType?): String {
    return when (coinType) {
      CoinType.BTC -> MyApplication.getInstance().getString(R.string.coin_btc)
      CoinType.ETH -> MyApplication.getInstance().getString(R.string.coin_eth)
      CoinType.HECO -> MyApplication.getInstance().getString(R.string.coin_heco)
      CoinType.BSC -> MyApplication.getInstance().getString(R.string.coin_bsc)
      else -> ""
    }
  }
}
