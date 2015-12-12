package me.jimm.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by generaluser on 11/30/15.
 * This class is responsible for retrieving data from the cloud and populating the ImageAdapter
 * and then updating the user interface (IPopularMovies subclasses)
 */
public class PopularMoviesPresenter {
    public static final String TAG = PopularMoviesPresenter.class.getSimpleName();


    PopularMoviesPresenter(){
        Log.d(TAG, "Starting Presenter");

    }

    public void getPopularMovies() {
        Log.d(TAG, "initialize");

        // get the popular movies and populate the list with the results

        // need to be declared outside of the try so they can be closed in the finally block
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String popularMoviesJsonString = null;


        try {
            // construct the URL
            final String BASE_URL = "http://api.themoviedb.org/3/discover/movie?";
            final String SORT_BY_PARAM = "sort_by";
            final String API_KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                    .appendQueryParameter(SORT_BY_PARAM, "popularity.desc")
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.MOVIE_DB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            // create the request to MovieDB
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");  // adding a new line character for debug readability
            }

            if (buffer.length() == 0) {
                // stream was empty, no point in parsing
                return;
            } else {
                // get structured data from JSON String
                popularMoviesJsonString = buffer.toString();
                getStructuredDataFromJson(popularMoviesJsonString);
            }
        }
        catch (IOException ioe) {
            Log.e(TAG, "Error attempting to get the movie data" + ioe.getMessage(), ioe);
        }
        catch (JSONException jsone) {
            Log.e(TAG, "Error parsing the JSON String" + jsone.getMessage(), jsone);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try{
                    reader.close();
                } catch (final IOException e) {
                    Log.e(TAG, "Error closing stream" + e.getMessage(), e);
                }
            }
        }


    }

    private void getStructuredDataFromJson(String jsonString) throws JSONException {
        Log.d(TAG, "getStructuredDataFromJson");

        Log.i(TAG, jsonString);

    }


}
