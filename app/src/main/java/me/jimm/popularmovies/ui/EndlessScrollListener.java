package me.jimm.popularmovies.ui;

import android.util.Log;
import android.widget.AbsListView;

/**
 * Created by generaluser on 12/15/15.
 * see:
 * older: https://github.com/codepath/android_guides/wiki/Endless-Scrolling-with-AdapterViews-and-RecyclerView
 * replaced byL http://benjii.me/2010/08/endless-scrolling-listview-in-android/
 */
public abstract class EndlessScrollListener implements AbsListView.OnScrollListener {

    private static final String TAG = EndlessScrollListener.class.getSimpleName();

    private int mVisibleThreshold = 5;  // min number of item below scroll position before loading more


    private int mCurrentPage = 1; // current page number of data loaded
    private int mPriorTotalItemCount = 0;   // item count in dataset after last load
    private boolean mIsLoading = true;   // status while loading
    private int mStartPageIndex = 0;     // start page index


    public EndlessScrollListener() {}

    public EndlessScrollListener(int visibleThreshold) {
        this.mVisibleThreshold = visibleThreshold;
    }

    public EndlessScrollListener(int visibleThreshold, int startPage) {
        this.mVisibleThreshold = visibleThreshold;
        this.mStartPageIndex = startPage;
        this.mCurrentPage = startPage;
    }

    // this method is called repeatedly and needs to load more data as necessary.
    // It should check to only load data when the previous load is complete
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        // if the total count is zero and the prior item count is not, assume the list is invalid
        // and should be reset
        if (totalItemCount < mPriorTotalItemCount) {
            Log.d(TAG, "onScroll List invalid");
            this.mCurrentPage = this.mStartPageIndex;
            this.mPriorTotalItemCount = totalItemCount;
            if (totalItemCount == 0 ) {
                this.mIsLoading = true;
            }
        }

        // if loading, and the dataset count has changed, assume it has finished loading and
        // update the current page number and the total item count
        if (mIsLoading && (totalItemCount > mPriorTotalItemCount)) {
            Log.d(TAG, "onScroll - loading");
            mIsLoading = false;
            mPriorTotalItemCount = totalItemCount;
            mCurrentPage++;
        }

        // if not loading, and we have passes the threshold, we need to load more data.
        if (!mIsLoading && (totalItemCount - mVisibleThreshold) <= (firstVisibleItem + mVisibleThreshold)) {
            Log.d(TAG, "onscroll - Need more data");
            mIsLoading = onLoadMore(mCurrentPage + 1, totalItemCount);
        }
    }

    public abstract boolean onLoadMore(int page, int totalItemCount);

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState){
        // do nothing
    }

}
