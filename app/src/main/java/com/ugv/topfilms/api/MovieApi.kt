package com.ugv.topfilms.api

import android.content.Context
import com.ugv.topfilms.utils.NetworkUtil
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object MovieApi {
    private var retrofit: Retrofit? = null
    private const val CACHE_SIZE = 10 * 1024 * 1024 // 10MB Cache size
        .toLong()

    private fun buildClient(context: Context): OkHttpClient {

        // Build interceptor
        val REWRITE_CACHE_CONTROL_INTERCEPTOR =
            label@ Interceptor { chain: Interceptor.Chain ->
                val originalResponse = chain.proceed(chain.request())
                if (NetworkUtil.hasNetwork(context)) {
                    val maxAge = 60 // read from cache for 1 minute
                    return@label originalResponse.newBuilder()
                        .header("Cache-Control", "public, max-age=$maxAge")
                        .build()
                } else {
                    val maxStale = 60 * 60 * 24 * 28 // tolerate 4-weeks stale
                    return@label originalResponse.newBuilder()
                        .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                        .build()
                }
            }

        // Create Cache
        val cache = Cache(context.cacheDir, CACHE_SIZE)
        return OkHttpClient.Builder()
            .addNetworkInterceptor(REWRITE_CACHE_CONTROL_INTERCEPTOR)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .cache(cache)
            .build()
    }

    fun getClient(context: Context): Retrofit? {
        if (retrofit == null) {
            retrofit = Retrofit.Builder()
                .client(buildClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl("https://api.themoviedb.org/3/")
                .build()
        }
        return retrofit
    }
}
