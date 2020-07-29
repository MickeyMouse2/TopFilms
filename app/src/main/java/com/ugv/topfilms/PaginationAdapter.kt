package com.ugv.topfilms

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.ugv.topfilms.models.Result
import com.ugv.topfilms.utils.PaginationAdapterCallback
import java.util.*


open class PaginationAdapter internal constructor(private var context: Context?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private var movieResults: MutableList<Result?>?
    private var isLoadingAdded = false
    private var retryPageLoad = false
    private var mCallback: PaginationAdapterCallback? = context as PaginationAdapterCallback?
    private var errorMsg: String? = null
    var movies: MutableList<Result?>?
        get() = movieResults
        set(movieResults) {
            this.movieResults = movieResults
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var viewHolder: RecyclerView.ViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)
        when (viewType) {
            ITEM -> {
                val viewItem =
                    inflater!!.inflate(R.layout.item_list, parent, false)
                viewHolder = MovieVH(viewItem)
            }
            LOADING -> {
                val viewLoading =
                    inflater!!.inflate(R.layout.item_progress, parent, false)
                viewHolder = LoadingVH(viewLoading)
            }
            HERO -> {
                val viewHero =
                    inflater!!.inflate(R.layout.item_hero, parent, false)
                viewHolder = HeroVH(viewHero)
            }
        }
        return viewHolder!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val result: Result? = movieResults!![position]
        when (getItemViewType(position)) {
            HERO -> {
                val heroVh = holder as HeroVH?
                heroVh!!.mMovieTitle.text = result!!.title
                heroVh.mYear.text = formatYearLabel(result)
                heroVh.mMovieDesc.text = result.overview
                loadImage(result.backdropPath!!)
                    .into(heroVh.mPosterImg)
            }
            ITEM -> {
                val movieVH = holder as MovieVH?
                movieVH!!.mMovieTitle.text = result!!.title
                movieVH.mYear.text = formatYearLabel(result)
                movieVH.mMovieDesc.text = result.overview

                loadImage(result.backdropPath!!)
                    .listener(object : RequestListener<Drawable?> {
                        override fun onLoadFailed(
                            @Nullable e: GlideException?,
                            model: Any?,
                            target: Target<Drawable?>?,
                            isFirstResource: Boolean
                        ): Boolean {

                            movieVH.mProgress.visibility = View.GONE
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable?>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            // image ready, hide progress now
                            movieVH.mProgress.visibility = View.GONE
                            return false
                        }
                    })
                    .into(movieVH.mPosterImg)
            }
            LOADING -> {
                val loadingVH = holder as LoadingVH?
                if (retryPageLoad) {
                    loadingVH!!.mErrorLayout.visibility = View.VISIBLE
                    loadingVH.mProgressBar.visibility = View.GONE
                    loadingVH.mErrorTxt.text =
                        if (errorMsg != null) errorMsg else context!!.getString(R.string.error_msg_unknown)
                } else {
                    loadingVH!!.mErrorLayout.visibility = View.GONE
                    loadingVH.mProgressBar.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun loadImage(@NonNull posterPath: String): RequestBuilder<Drawable> {
        return Glide
            .with(context!!)
            .load(BASE_URL_IMG + posterPath)
            .apply( RequestOptions()
                .centerCrop())
    }

    override fun getItemCount(): Int {
        return if (movieResults == null) 0 else movieResults!!.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            HERO
        } else {
            if (position == movieResults!!.size - 1 && isLoadingAdded) LOADING else ITEM
        }
    }

    private fun formatYearLabel(result: Result?): String? {
        return (result!!.releaseDate!!.substring(0, 4) + " | "
                + result.originalLanguage!!.toUpperCase(Locale.ROOT))
    }

    private fun add(r: Result?) {
        movieResults!!.add(r)
        notifyItemInserted(movieResults!!.size - 1)
    }

    fun addAll(moveResults: MutableList<Result?>?) {
        for (result in moveResults!!) {
            add(result)
        }
    }

    private fun remove(r: Result?) {
        val position = movieResults!!.indexOf(r)
        if (position > -1) {
            movieResults!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun clear() {
        isLoadingAdded = false
        while (itemCount > 0) {
            remove(getItem(0))
        }
    }

    val isEmpty: Boolean
        get() = itemCount == 0

    fun addLoadingFooter() {
        isLoadingAdded = true
        add(Result())
    }

    fun removeLoadingFooter() {
        isLoadingAdded = false
        val position = movieResults!!.size - 1
        val result: Result? = getItem(position)
        if (result != null) {
            movieResults!!.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun getItem(position: Int): Result? {
        return movieResults!![position]
    }

    /**
     * Displays Pagination retry footer view along with appropriate errorMsg
     *
     * @param show
     * @param errorMsg to display if page load fails
     */
    fun showRetry(show: Boolean, @Nullable errorMsg: String?) {
        retryPageLoad = show
        notifyItemChanged(movieResults!!.size - 1)
        if (errorMsg != null) this.errorMsg = errorMsg
    }
    /*
   View Holders
   _________________________________________________________________________________________________
    */
    /**
     * Header ViewHolder
     */
    private inner class HeroVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mMovieTitle: TextView = itemView.findViewById(R.id.movie_title)
        val mMovieDesc: TextView = itemView.findViewById(R.id.movie_desc)
        val mYear: TextView = itemView.findViewById(R.id.movie_year)
        val mPosterImg: ImageView = itemView.findViewById(R.id.movie_poster)

    }

    protected inner class MovieVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mMovieTitle: TextView = itemView.findViewById(R.id.movie_title)
        val mMovieDesc: TextView = itemView.findViewById(R.id.movie_desc)
        val mYear: TextView = itemView.findViewById(R.id.movie_year)
        val mPosterImg: ImageView = itemView.findViewById(R.id.movie_poster)
        val mProgress: ProgressBar = itemView.findViewById(R.id.movie_progress)

    }

    protected inner class LoadingVH(itemView: View) :
        RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        val mProgressBar: ProgressBar = itemView.findViewById(R.id.loadmore_progress)
        val mRetryBtn: ImageButton = itemView.findViewById(R.id.loadmore_retry)
        val mErrorTxt: TextView = itemView.findViewById(R.id.loadmore_errortxt)
        val mErrorLayout: LinearLayout = itemView.findViewById(R.id.loadmore_errorlayout)
        override fun onClick(view: View?) {
            when (view!!.id) {
                R.id.loadmore_retry, R.id.loadmore_errorlayout -> {
                    showRetry(false, null)
                    mCallback!!.retryPageLoad()
                }
            }
        }

        init {
            mRetryBtn.setOnClickListener(this)
            mErrorLayout.setOnClickListener(this)
        }
    }

    companion object {
        private const val ITEM = 0
        private const val LOADING = 1
        private const val HERO = 2
        private val BASE_URL_IMG: String? = "https://image.tmdb.org/t/p/w200"
    }

    init {
        movieResults = ArrayList()
    }
}
