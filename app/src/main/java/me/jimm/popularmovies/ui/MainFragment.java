package me.jimm.popularmovies.ui;

import android.content.*;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

import com.squareup.picasso.Picasso;
import me.jimm.popularmovies.MovieDbApiResponseReceiver;
import me.jimm.popularmovies.MovieDbApiService;
import me.jimm.popularmovies.R;
import me.jimm.popularmovies.model.Movie;


public class MainFragment extends Fragment implements
        AdapterView.OnItemClickListener,
        MovieDbApiResponseReceiver.Receiver {

    public MovieDbApiResponseReceiver mReceiver;

    private static final String TAG = MainFragment.class.getSimpleName();
    private static final String SORT_PREFERENCE = "sort_order_settings"; // ListPreference key
    private static final String PERSIST_MOVIE_LIST = "movie_list";
    private static final String PERSIST_SORT_ORDER = "current_sort_order";

    private SharedPreferences mPrefs;
    private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener;
    private GridView mGridView;
    private MovieDbAdapter mMovieDbAdapter;
    private ArrayList<Movie> mMovies;
    private String mCurrentSortOrder;
    private int mLastPage = 1;  // last page of data loaded, updated on OnScrollListener


    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PERSIST_MOVIE_LIST)) {
                mMovies = savedInstanceState.getParcelableArrayList(PERSIST_MOVIE_LIST);
            } else {
               mMovies = new ArrayList();
            }
            if (savedInstanceState.containsKey(PERSIST_SORT_ORDER)) {
                mCurrentSortOrder = savedInstanceState.getString(PERSIST_SORT_ORDER);
            } else {
                mCurrentSortOrder = getSortOrderPreference();
            }

        } else {
            mMovies = new ArrayList();
            mCurrentSortOrder = getSortOrderPreference();
            fetchMovieData(getActivity());
        }

        // register this fragment as a listener for OnSharedPreferenceChangeListener
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        registerSharedPreferenceListener();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View v =  inflater.inflate(R.layout.fragment_main, container, false);

        mMovieDbAdapter = new MovieDbAdapter(getActivity());
        mGridView = (GridView) v.findViewById(R.id.movies_grid_view);
        mGridView.setAdapter(mMovieDbAdapter);
        mGridView.setOnItemClickListener(this);

        // register gridview as a scroll listener
        mGridView.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemCount) {
                Log.d(TAG, "onLoadMore");
                mLastPage = page;
                fetchMovieData(getActivity());
                return true;
            }
        });

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        if (mReceiver != null) {
            mReceiver.setReceiver(null);
        }
        if (mPrefs != null) {
            mPrefs.unregisterOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
        }



            Log.d(TAG, "mCurrentSortOrder=" + mCurrentSortOrder);
            Log.d(TAG, "getSortOrderPreference()=" + getSortOrderPreference());

            //                    mMovies.clear();
            //                    mLastPage = 1;
            //                    fetchMovieData(getActivity());

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (mSharedPreferenceChangeListener != null) {
            mPrefs.registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
        }
        if (mReceiver != null ) {
            mReceiver.setReceiver(this); // reset the receiver
        }

        Log.d(TAG, "mCurrentSortOrder=" + mCurrentSortOrder);
        Log.d(TAG, "getSortOrderPreference()=" + getSortOrderPreference());

        if (!mCurrentSortOrder.equals(getSortOrderPreference())) {
            mCurrentSortOrder = getSortOrderPreference();
            mMovies.clear();
            mLastPage = 1;
            fetchMovieData(getActivity());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    public void onReceiveResponse(int resultCode, Bundle resultData) {
        Log.d(TAG, "onReceiveResponse - ResultCode:" + resultCode);
        switch (resultCode) {
            case MovieDbApiService.STATUS_RUNNING:
                //todo show progress
                break;
            case MovieDbApiService.STATUS_FINISHED:
                Log.d(TAG, "MovieDbApiService finished");
                ArrayList<Movie> m = resultData.getParcelableArrayList("results");
                mMovies.addAll(m);
                Log.d(TAG, "mMovies.size()=" + mMovies.size());
                mMovieDbAdapter.notifyDataSetChanged();
                // todo hide progress
                break;
            case MovieDbApiService.STATUS_ERROR:
                // handle the error;
                Log.e(TAG, resultData.getString(Intent.EXTRA_TEXT));
                Toast.makeText(getActivity(), "Error occurred while retrieving movie data." +
                        resultData.get(Intent.EXTRA_TEXT), Toast.LENGTH_SHORT).show();
                break;
            default:
                // todo nothing
                break;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        Log.d(TAG, "onSaveInstanceState()");
        bundle.putParcelableArrayList(PERSIST_MOVIE_LIST, mMovies);
        bundle.putString(PERSIST_SORT_ORDER, mCurrentSortOrder);

    }



    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Log.d(TAG, "onItemClick - Movie details at position: " + position);
        Movie m = mMovies.get(position);
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra("movie", m);
        startActivity(intent);
    }

    // Private Members /////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    private void fetchMovieData(Context context) {

        Log.d(TAG, "fetchMovieData");

        // start the service to interact with the MovieDB API
        // set a reference to the API Request response handler
        mReceiver = new MovieDbApiResponseReceiver(new Handler());
        mReceiver.setReceiver(this);

        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context, MovieDbApiService.class);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("page", mLastPage);
        intent.putExtra("sort_by", mCurrentSortOrder);
        intent.putExtra("command", "query");
        getActivity().startService(intent);
    }

    private String getSortOrderPreference() {
        Log.d(TAG, "getSortOrderPreference");
        // http://stackoverflow.com/questions/2767354/default-value-of-android-preference
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String defaultValue = getActivity().getResources().getString(R.string.pref_sort_default);
        String sortOrder = prefs.getString(SORT_PREFERENCE, defaultValue);
     //   Toast.makeText(getActivity(), "sortOrder = " + sortOrder, Toast.LENGTH_LONG).show();
        if (sortOrder.equals("POPULARITY"))  {
            return MovieDbApiService.MOVIE_API_PARAM_SORT_BY_POPULARITY;
        } else if (sortOrder.equals("RATING")) {
            return MovieDbApiService.MOVIE_API_PARAM_SORT_BY_RATING;
        }
        else {
            Log.e(TAG, "Sort Order undefined: sortOrder='" + sortOrder + "'");
            return null;
        }
    }

    public void registerSharedPreferenceListener()
    {
      //  Log.d(TAG, "registerSharedPreferenceListener");
        mSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                Log.d(TAG, "onSharedPreferenceChanged - Pref changed for: " + key + " pref: " +
                        prefs.getString(key, null));
                // reset to initial state
                mLastPage = 1;
          }
        };

        mPrefs.registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
    }

    private class MovieDbAdapter extends BaseAdapter{

        private final String TAG = MovieDbAdapter.class.getSimpleName();
        private Context mContext;

        public MovieDbAdapter(Context c) {
            Log.d(TAG, "MovieDbAdapter Constructor");
            mContext = c;
        }

        public int getCount() {
            return mMovies.size();
        }

        public Object getItem(int position) {
            if (mMovies != null) {
                return mMovies.get(position);
            } else {
                return null;
            }
        }

        public long getItemId(int position) {
            return mMovies.get(position).getMovieId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Movie movie = (Movie) getItem(position);

            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                // adjusts view to allow image to render with an adequate aspect ratio.
                imageView.setAdjustViewBounds(true);
                imageView.setPadding(4, 4, 4, 4);
            } else {
                imageView = (ImageView) convertView;
            }

            Picasso.with(mContext)
                    .load(movie.getPosterPath())
                    .placeholder(R.drawable.ic_photo_white_48dp)
                    .into(imageView);

            return imageView;
        }

        public void clearAll() {
            mMovies.clear();
        }

        public void add(Collection<Movie> movies) {
            mMovies.addAll(movies);
            notifyDataSetChanged();
        }


    }

}