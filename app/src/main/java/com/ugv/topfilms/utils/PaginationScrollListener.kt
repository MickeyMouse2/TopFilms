package com.ugv.topfilms.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


abstract class PaginationScrollListener(linearLayoutManager: LinearLayoutManager) :
    RecyclerView.OnScrollListener() {

    private var layoutManager: LinearLayoutManager? = null

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val visibleItemCount: Int = layoutManager!!.childCount
        val totalItemCount: Int = layoutManager!!.itemCount
        val firstVisibleItemPosition: Int = layoutManager!!.findFirstVisibleItemPosition()
        if (!isLoading() && !isLastPage()) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                && firstVisibleItemPosition >= 0
            ) {
                loadMoreItems()
            }
        }
    }

    init {
        this.layoutManager = linearLayoutManager
    }

    abstract fun loadMoreItems()
    abstract fun totalPageCount(): Int
    abstract fun isLastPage(): Boolean
    abstract fun isLoading(): Boolean



}
