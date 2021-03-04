package org.haobtc.onekey.onekeys.homepage.mindmenu

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.res.ResourcesCompat
import org.haobtc.onekey.R
import org.haobtc.onekey.databinding.ActivityAddNewBinding
import org.haobtc.onekey.ui.base.BaseActivity

class AddNewTokenActivity : BaseActivity() {


  companion object {

    @JvmStatic
    fun start(context: Context) {
      context.startActivity(Intent(context, AddNewTokenActivity::class.java))
    }
  }

  val mBinding by lazy {
    ActivityAddNewBinding.inflate(layoutInflater)
  }

  override fun getContentViewId() = 0

  override fun init() {
    ResourcesCompat.getColor(resources, R.color.button_bk_light_grey, null).apply {
      setStatusBarColor(this)
      toolbar.background = ColorDrawable(this)
    }
    setLeftTitle(R.string.add_token)
  }

  override fun enableViewBinding() = true

  override fun getLayoutView(): View? {
    return mBinding.root
  }

  override fun showToolBar(): Boolean {
    return true
  }


}
