package org.haobtc.onekey.bean

import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import org.haobtc.onekey.R

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
    Glide.with(imageView.context).load(url).placeholder(R.mipmap.ic_bi)
        .apply(RequestOptions.bitmapTransform(CircleCrop())).into(imageView)
  }
}
