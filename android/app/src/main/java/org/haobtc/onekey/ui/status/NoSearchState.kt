package org.haobtc.onekey.ui.status

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.zy.multistatepage.MultiState
import com.zy.multistatepage.MultiStateContainer

class NoSearchState : MultiState() {

  override fun onCreateMultiStateView(context: Context, inflater: LayoutInflater, container: MultiStateContainer): View {
    return inflater.inflate(org.haobtc.onekey.R.layout.search_empty_view, container, false)
  }

  override fun onMultiStateViewCreate(view: View) {

  }


}
