package org.haobtc.onekey.business.assetsLogo

import org.haobtc.onekey.R
import org.haobtc.onekey.constant.Vm.CoinType

class AssetsLogo {
  fun getLogoResources(coinType: CoinType?): Int {
    return when (coinType) {
      CoinType.ETH -> R.drawable.token_eth
      CoinType.BTC -> R.drawable.token_btc
      else -> R.drawable.loco_round
    }
  }
}
