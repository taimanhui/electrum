package org.haobtc.onekey.adapter

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.orhanobut.logger.Logger
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import org.haobtc.onekey.R
import org.haobtc.onekey.bean.BalanceCoinInfo
import org.haobtc.onekey.bean.BalanceInfoDTO
import org.haobtc.onekey.constant.Vm
import org.haobtc.onekey.onekeys.homepage.process.TransactionDetailWalletActivity
import org.haobtc.onekey.viewmodel.AppWalletViewModel

class WalletAssetAccountAdapter(context: Context, appViewModel: AppWalletViewModel, decoration: HorizontalDividerItemDecoration, data: List<BalanceInfoDTO?>?) : BaseQuickAdapter<BalanceInfoDTO, BaseViewHolder>(R.layout.item_all_asset_account, data) {


  val model = appViewModel
  val itemDerection = decoration

  override fun convert(helper: BaseViewHolder, item: BalanceInfoDTO) {
    helper.setText(R.id.account_name, item.name)
    val recyclerView = helper.getView<RecyclerView>(R.id.account_recyclerview)
    Logger.d("")
    val adapter = HdWalletAssetAdapter(mContext, item.wallets)
    recyclerView.adapter = adapter
    recyclerView.addItemDecoration(itemDerection)
    adapter.setOnItemClickListener { adapter, view, position ->
      val balance = adapter.data[position] as BalanceCoinInfo
      model.changeCurrentWallet(item.label)
      TransactionDetailWalletActivity.start(mContext,item.label)
    }
  }

}
