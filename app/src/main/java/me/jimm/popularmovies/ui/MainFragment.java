package me.jimm.popularmovies.ui;

import android.content.*;
import android.net.Uri;
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
import java.util.List;

import com.squareup.picasso.Picasso;
import me.jimm.popularmovies.MovieDbApiResponseReceiver;
import me.jimm.popularmovies.MovieDbApiService;
import me.jimm.popularmovies.R;
import me.jimm.popularmovies.model.Movie;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment implements AdapterView.OnItemClickListener, MovieDbApiResponseReceiver.Receiver {

    public MovieDbApiResponseReceiver mReceiver;

    private static final String TAG = MainFragment.class.getSimpleName();
    private static final String SORT_PREFERENCE = "preferences_sort_order"; // ListPreference key

    private GridView mGridView;
    private MovieDbAdapter mMovieDbAdapter;
    private List<Movie> mMovies;


    // constructors and methods

    public MainFragment() {
        // Required empty public constructor
    }

    // getter and setter are required for RetainedFragment
    public void setMovieList(List data) { this.mMovies = data; }
    public List getMovieList() { return mMovies; }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        // set a reference to the API Request response handler
        mReceiver = new MovieDbApiResponseReceiver(new Handler());
        mReceiver.setReceiver(this);

        // start the service to interact with the MovieDB API
        final Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(), MovieDbApiService.class);
        intent.putExtra("receiver", mReceiver);
        intent.putExtra("command", "query");
        getActivity().startService(intent);

        // save state when the parent activity is shut down
        setRetainInstance(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_main, container, false);

        mGridView = (GridView) v.findViewById(R.id.movies_grid_view);
        mGridView.setOnItemClickListener(this);

        return v;
    }

    public void onPause() {
        super.onPause();
        mReceiver.setReceiver(null); // clear receiver so no leaks.
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortOrder = sharedPrefs.getString(SORT_PREFERENCE, "");

        Log.d(TAG, "onResume - sortOrder:" + sortOrder);

    }

    public void onReceiveResponse(int resultCode, Bundle resultData) {
        Log.d(TAG, "onReceiveResponse - RersultCode:" + resultCode);
        switch (resultCode) {
            case MovieDbApiService.STATUS_RUNNING:
                //show progress
                break;
            case MovieDbApiService.STATUS_FINISHED:
                Log.d(TAG, "do something interesting with the results");
                mMovies = new ArrayList<>();
                mMovies = resultData.getParcelableArrayList("results");
                mMovieDbAdapter = new MovieDbAdapter(getActivity(), mMovies);
                mGridView.setAdapter(mMovieDbAdapter);
                // hide progress
                break;
            case MovieDbApiService.STATUS_ERROR:
                // handle the error;
                Log.e(TAG, resultData.getString(Intent.EXTRA_TEXT));
                break;
            default:
                // nothing
                break;
        }
    }

    // Implements the AdapterView.OnItemClickListener
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        Toast.makeText(getActivity(), "Show movie " + position + " details", Toast.LENGTH_SHORT).show();
        //Intent intent = new Intent(getActivity(), ImageDetailActivity.class);
    }


    private class MovieDbAdapter extends ArrayAdapter<Movie> {
        private final String TAG = MovieDbAdapter.class.getSimpleName();
        private Context mContext;

        public MovieDbAdapter(Context context, List<Movie> movies) {
            super(context, 0, movies);
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(TAG, "getView");
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
    }

}