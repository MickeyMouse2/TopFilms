package com.ugv.topfilms.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.util.*


class TopRatedMovies {

    @SerializedName("page")
    @Expose
    var page: Int? = null

    @SerializedName("results")
    @Expose
    var results: MutableList<Result?>? = ArrayList()

    @SerializedName("total_results")
    @Expose
    var totalResults: Int? = null

    @SerializedName("total_pages")
    @Expose
    var totalPages: Int? = null

}
