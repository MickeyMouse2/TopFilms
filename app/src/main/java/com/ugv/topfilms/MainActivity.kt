package com.ugv.topfilms

import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ugv.topfilms.api.MovieApi
import com.ugv.topfilms.api.MovieService
import com.ugv.topfilms.models.Result
import com.ugv.topfilms.models.TopRatedMovies
import com.ugv.topfilms.utils.PaginationAdapterCallback
import com.ugv.topfilms.utils.PaginationScrollListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeoutException

class MainActivity : AppCompatActivity(), PaginationAdapterCallback {
    var adapter: PaginationAdapter? = null
    var linearLayoutManager: LinearLayoutManager? = null
    var rv: RecyclerView? = null
    var progressBar: ProgressBar? = null
    var errorLayout: LinearLayout? = null
    var btnRetry: Button? = null
    var txtError: TextView? = null
    var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var isLoading = false
    private var isLastPage = false
    private var currentPage = PAGE_START

    private var movieService: MovieService? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rv = findViewById(R.id.main_recycler)
        progressBar = findViewById(R.id.main_progress)
        errorLayout = findViewById(R.id.error_layout)
        btnRetry = findViewById(R.id.error_btn_retry)
        txtError = findViewById(R.id.error_txt_cause)
        swipeRefreshLayout = findViewById(R.id.main_swiperefresh)
        adapter = PaginationAdapter(this)
        linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rv!!.layoutManager = linearLayoutManager
        rv!!.itemAnimator = DefaultItemAnimator()
        rv!!.adapter = adapter
        rv!!.addOnScrollListener(object : PaginationScrollListener(linearLayoutManager!!) {
            override fun loadMoreItems() {
                isLoading = true
                currentPage += 1
                loadNextPage()
            }

            override fun totalPageCount(): Int {
                return TOTAL_PAGES
            }

            override fun isLastPage(): Boolean {
                return isLastPage
            }

            override fun isLoading(): Boolean {
                return isLoading
            }
        })

        movieService = MovieApi.getClient(this)!!.create(MovieService::class.java)
        loadFirstPage()
        btnRetry!!.setOnClickListener { loadFirstPage() }
        swipeRefreshLayout!!.setOnRefreshListener { doRefresh() }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
            R.id.menu_refresh -> {
                swipeRefreshLayout!!.setRefreshing(true)
                doRefresh()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun doRefresh() {
        progressBar!!.visibility = View.VISIBLE
        if (callTopRatedMoviesApi()!!.isExecuted) callTopRatedMoviesApi()!!.cancel()

        adapter!!.movies!!.clear()
        adapter!!.notifyDataSetChanged()
        loadFirstPage()
        swipeRefreshLayout!!.isRefreshing = false
    }

    private fun loadFirstPage() {
        Log.d(TAG, "loadFirstPage: ")

        hideErrorView()
        currentPage = PAGE_START
        callTopRatedMoviesApi()!!.enqueue(object : Callback<TopRatedMovies?> {
            override fun onResponse(call: Call<TopRatedMovies?>?, response: Response<TopRatedMovies?>?) {
                hideErrorView()

                val results: MutableList<Result?>? = fetchResults(response)
                progressBar!!.visibility = View.GONE
                adapter!!.addAll(results)
                if (currentPage <= TOTAL_PAGES) adapter!!.addLoadingFooter() else isLastPage = true
            }

            override fun onFailure(call: Call<TopRatedMovies?>?, t: Throwable?) {
                t!!.printStackTrace()
                showErrorView(t)
            }
        })
    }

    private fun fetchResults(response: Response<TopRatedMovies?>?): MutableList<Result?>? {
        val topRatedMovies: TopRatedMovies? = response!!.body()
        return topRatedMovies!!.results
    }

    private fun loadNextPage() {
        Log.d(TAG, "loadNextPage: $currentPage")
        callTopRatedMoviesApi()!!.enqueue(object : Callback<TopRatedMovies?>{
            override fun onResponse(call: Call<TopRatedMovies?>?, response: Response<TopRatedMovies?>?) {
                adapter!!.removeLoadingFooter()
                isLoading = false
                val results: MutableList<Result?>? = fetchResults(response)
                adapter!!.addAll(results)
                if (currentPage != TOTAL_PAGES) adapter!!.addLoadingFooter() else isLastPage = true
            }

            override fun onFailure(call: Call<TopRatedMovies?>?, t: Throwable?) {
                t!!.printStackTrace()
                adapter!!.showRetry(true, fetchErrorMessage(t))
            }
        })
    }


    private fun callTopRatedMoviesApi(): Call<TopRatedMovies?>? {
        return movieService!!.getTopRatedMovies(
                getString(R.string.my_api_key),
                "en_US",
                currentPage
        )
    }

    override fun retryPageLoad() {
        loadNextPage()
    }

    private fun showErrorView(throwable: Throwable?) {
        if (errorLayout!!.visibility == View.GONE) {
            errorLayout!!.visibility = View.VISIBLE
            progressBar!!.visibility = View.GONE
            txtError!!.text = fetchErrorMessage(throwable)
        }
    }

    private fun fetchErrorMessage(throwable: Throwable?): String? {
        var errorMsg: String = resources.getString(R.string.error_msg_unknown)
        if (!isNetworkConnected) {
            errorMsg = resources.getString(R.string.error_msg_no_internet)
        } else if (throwable is TimeoutException) {
            errorMsg = resources.getString(R.string.error_msg_timeout)
        }
        return errorMsg
    }

    private fun hideErrorView() {
        if (errorLayout!!.visibility == View.VISIBLE) {
            errorLayout!!.visibility = View.GONE
            progressBar!!.visibility = View.VISIBLE
        }
    }

    private val isNetworkConnected: Boolean
        private get() {
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            return cm!!.activeNetworkInfo != null
        }

    companion object {
        private val TAG: String? = "MainActivity"
        private const val PAGE_START = 1
        private const val TOTAL_PAGES = 1
    }
}