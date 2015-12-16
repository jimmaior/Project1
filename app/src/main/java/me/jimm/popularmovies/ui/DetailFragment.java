package me.jimm.popularmovies.ui;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import me.jimm.popularmovies.R;
import me.jimm.popularmovies.model.Movie;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment {

    private static final String TAG = DetailFragment.class.getSimpleName();

    // members
    private Movie mMovie;
    private TextView mTvTitle;
    private ImageView mIvPoster;
    private TextView mTvReleaseDate;
    private TextView mTvRating;
    private TextView mTvOverview;


    public DetailFragment() {
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getArguments();
        mMovie = b.getParcelable("movie");

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_detail, container, false);

        mTvTitle = (TextView) v.findViewById(R.id.tv_title);
        mTvTitle.setText(mMovie.getTitle());

        mIvPoster = (ImageView) v.findViewById(R.id.iv_poster);
        Picasso.with(getActivity())
                .load(mMovie.getPosterPath())
                .into(mIvPoster);

        mTvReleaseDate = (TextView) v.findViewById(R.id.tv_release_date);
        mTvReleaseDate.setText(mMovie.getReleaseDate());

        mTvRating = (TextView) v.findViewById(R.id.tv_user_rating);
        mTvRating.setText( Double.toString(mMovie.getUserRating()));

        mTvOverview = (TextView) v.findViewById(R.id.tv_overview);
        mTvOverview.setText(mMovie.getOverview());

        return v;
    }
}
