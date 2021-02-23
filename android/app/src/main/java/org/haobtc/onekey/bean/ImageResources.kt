package org.haobtc.onekey.bean

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide

interface ImageResources {
  fun intoTarget(imageView: ImageView)
}

class LocalImage(@DrawableRes val res: Int) : ImageResources {
  override fun intoTarget(imageView: ImageView) {
    imageView.setImageDrawable(ResourcesCompat.getDrawable(imageView.resources, res, null))
  }
}

class RemoteImage(val url: String) : ImageResources {
  override fun intoTarget(imageView: ImageView) {
    Glide.with(imageView.context).load(url).into(imageView)
  }
}
