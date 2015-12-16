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
    private static final String SORT_PREFERENCE = "sort_order"; // ListPreference key
    private static final String MOVIE_LIST = "movie_list";

    private SharedPreferences mPrefs;
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;
    private GridView mGridView;
    private MovieDbAdapter mMovieDbAdapter;
    private ArrayList<Movie> mMovies;



    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        // register this fragment as a listener for OnSharedPreferenceChangeListener
        mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        registerPreferenceListener();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View v =  inflater.inflate(R.layout.fragment_main, container, false);

        // starts service component that fetches data from the MovieDB API
        mMovies = new ArrayList<>();
        mMovieDbAdapter = new MovieDbAdapter(getActivity());
        //fetchMovieData(getActivity());


        mGridView = (GridView) v.findViewById(R.id.movies_grid_view);
        mGridView.setAdapter(mMovieDbAdapter);
        mGridView.setOnItemClickListener(this);

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        mReceiver.setReceiver(null); // clear receiver so are no leaks.
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (mReceiver != null ) {
            mReceiver.setReceiver(this); // reset the receiver
        }
        fetchMovieData(getActivity());
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
                mMovies = resultData.getParcelableArrayList("results");
                mMovieDbAdapter = new MovieDbAdapter(getActivity());
                //mMovieDbAdapter.clearAll();
                //mMovieDbAdapter.addAll(mMovies);
                mMovieDbAdapter.notifyDataSetChanged();
                mGridView.setAdapter(mMovieDbAdapter);
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

//    @Override
//    public void onSaveInstanceState(Bundle state) {
//        super.onSaveInstanceState(state);
//        Log.d(TAG, "onSaveInstanceState()");
//        state.putParcelableArrayList(MOVIE_LIST, mMovies);
//    }



    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Log.d(TAG, "onItemClick - Movie details at position: " + position);
        Movie m = mMovies.get(position);
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra("movie", m);
        startActivity(intent);
    }

    // Private Members /////////////////////////////////////////////
    private void fetchMovieData(Context context) {
        Log.d(TAG, "fetchMovieData");

        // start the service to interact with the MovieDB API
        // set a reference to the API Request response handler
        mReceiver = new MovieDbApiResponseReceiver(new Handler());
        mReceiver.setReceiver(this);

        final Intent intent = new Intent(Intent.ACTION_SYNC, null, context, MovieDbApiService.class);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("sort_by", getSortOrderPreference());
        intent.putExtra("command", "query");
        getActivity().startService(intent);
    }

    private String getSortOrderPreference() {
        Log.d(TAG, "getSortOrderPreference");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = sharedPreferences.getString(SORT_PREFERENCE, "");
        if (sortOrder.equals("POPULARITY"))  {
            return MovieDbApiService.MOVIE_API_PARAM_SORT_BY_POPULARITY;
        } else if (sortOrder.equals("RATING")) {
            return MovieDbApiService.MOVIE_API_PARAM_SORT_BY_RATING;
        }
        else {
            return null;
        }
    }

    private void registerPreferenceListener()
    {
        Log.d(TAG, "registerPreferenceListener");
        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                Log.d(TAG, "onSharedPreferenceChanged - Pref changed for: " + key + " pref: " +
                        prefs.getString(key, null));
                //fetchMovieData(getActivity());
          }
        };

        mPrefs.registerOnSharedPreferenceChangeListener(mListener);
    }

    private class MovieDbAdapter extends BaseAdapter{

        private final String TAG = MovieDbAdapter.class.getSimpleName();

        private Context mContext;


        public MovieDbAdapter(Context c) {
            Log.d(TAG, "MovieDbAdapter Constructed");
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
        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                // adjusts view ot fix the aspect ratio of the image
                imageView.setAdjustViewBounds(true);
                imageView.setPadding(4, 4, 4, 4);
            } else {
                imageView = (ImageView) convertView;
            }

            Picasso.with(mContext)
                    .load(mMovies.get(position).getPosterPath())
                    .into(imageView);

            return imageView;
        }

        public void clearAll() {
            mMovies.clear();
        }

        public void addAll(Collection<Movie> movies) {
            Log.d(TAG, "addAll");
            mMovies.addAll(movies);
            notifyDataSetChanged();
        }


    }

}