package com.ugv.topfilms.utils

import android.content.Context
import androidx.annotation.NonNull
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions


@GlideModule
class PaginationGlideModule : AppGlideModule() {
    override fun applyOptions(
        @NonNull context: Context,
        @NonNull builder: GlideBuilder
    ) {
        super.applyOptions(context, builder)
        builder.setDefaultRequestOptions(
            RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL) // cache both original & resized image
        )
    }
}
