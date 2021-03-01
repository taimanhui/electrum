package org.haobtc.onekey.onekeys

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import com.alibaba.fastjson.JSON
import com.scwang.smartrefresh.layout.util.SmartUtil
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration
import org.haobtc.onekey.R
import org.haobtc.onekey.activities.base.MyApplication
import org.haobtc.onekey.adapter.SelectTokenAdapter
import org.haobtc.onekey.bean.TokenList
import org.haobtc.onekey.card.utils.JsonParseUtils
import org.haobtc.onekey.databinding.ActivitySelectTokenBinding
import org.haobtc.onekey.onekeys.homepage.process.SendEthActivity
import org.haobtc.onekey.ui.base.BaseActivity
import org.haobtc.onekey.viewmodel.AppWalletViewModel

/**
 *  转账 选择代币页面
 */
class SelectTokenActivity : BaseActivity() {


  companion object {

    @JvmField val ASSET_JSON ="assetJson"

    @JvmStatic
    fun start(context: Context) {
      val intent = Intent()
      intent.setClass(context, SelectTokenActivity::class.java)
      context.startActivity(intent)
    }
  }

  private val mAppViewModel by lazy {
    ViewModelProvider(MyApplication.getInstance()).get(AppWalletViewModel::class.java)
  }

  private val mBinding by lazy {
    ActivitySelectTokenBinding.inflate(layoutInflater)
  }


  private val mToken: TokenList by lazy {
    val json = JsonParseUtils.getJsonStr(mContext, "eth_token_list.json")
    JSON.parseObject(json, TokenList::class.java)
  }

  private val mAdapter: SelectTokenAdapter by lazy {
    SelectTokenAdapter(data = mToken.tokens)
  }


  override fun init() {
    ResourcesCompat.getColor(resources, R.color.button_bk_light_grey, null).apply {
      setStatusBarColor(this)
      toolbar.background = ColorDrawable(this)
    }
    val build = HorizontalDividerItemDecoration.Builder(this)
        .color(ResourcesCompat.getColor(resources, R.color.color_select_wallet_divider, theme))
        .sizeResId(R.dimen.line_hight)
        .margin(SmartUtil.dp2px(12F), 0)
        .build()

    setLeftTitle(R.string.select_token)
//    var list: MutableList<Assets> = ArrayList()
//    mAppViewModel.currentWalletAssetsList.observe(
//        this,
//        Observer { assets ->
//          list.clear()
//          list.addAll(assets)
//        })
    var adapter = SelectTokenAdapter(data = mToken.tokens)
    mBinding.recyclerView.adapter = mAdapter
    mBinding.recyclerView.addItemDecoration(build)
    mAdapter.setOnItemClickListener { adapter, view, position ->
      val intent = Intent()
      intent.setClass(mContext, SendEthActivity::class.java)
      val json = JSON.toJSONString(adapter.data[position])
      intent.putExtra(ASSET_JSON, json)
      setResult(100, intent)
      finish()
    }
  }


  override fun getContentViewId() = 0

  override fun getLayoutView() = mBinding.root


  override fun showToolBar(): Boolean {
    return true
  }

  override fun enableViewBinding(): Boolean {
    return true
  }
}
