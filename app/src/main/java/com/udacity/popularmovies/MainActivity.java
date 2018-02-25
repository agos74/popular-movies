package com.udacity.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.utilities.NetworkUtils;
import com.udacity.popularmovies.utilities.TheMovieDBJsonUtils;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    public static final String MOST_POPULAR_ORDER_KEY = "p";
    public static final String TOP_RATED_ORDER_KEY = "t";

    private static final String MOST_POPULAR_TITLE = "Most Popular Movies";
    private static final String TOP_RATED_TITLE = "Top Rated Movies";

    private RecyclerView mRecyclerView;

    private MovieAdapter mMovieAdapter;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    private static final int MOVIES_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posters);

        mRecyclerView = findViewById(R.id.recyclerview_main);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = findViewById(R.id.tv_error_message_display);

         /*
         * GridLayoutManager to show the movie posters in grid of 2 columns
         */
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        /* Set the layoutManager on mRecyclerView */
        mRecyclerView.setLayoutManager(layoutManager);

         /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        // set mMovieAdapter equal to a new MovieAdapter
        mMovieAdapter = new MovieAdapter(this);

        /* attaches adapter to the RecyclerView in layout. */
        mRecyclerView.setAdapter(mMovieAdapter);

         /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         */
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

        /* Once all of our views are setup, we can load the movies. */
        loadMovies(MOST_POPULAR_ORDER_KEY);

    }

    /**
     * This method will get the user's preferred order for movies, and then tell some
     * background method to get the movies data in the background.
     */
    private void loadMovies(String orderType) {
        showMoviesDataView();

        //String orderType = PopularMoviePreferences.getPreferredOrderType(this);
        new FetchMoviesTask().execute(orderType);

        //Set Activity Title
        String title;
        switch (orderType) {
            case MainActivity.MOST_POPULAR_ORDER_KEY:
                title = MOST_POPULAR_TITLE;
                break;
            case MainActivity.TOP_RATED_ORDER_KEY:
                title = TOP_RATED_TITLE;
                break;
            default:
                title = getString(R.string.app_name);
        }
        this.setTitle(title);
    }

    /**
     * This method is overridden by our MainActivity class in order to handle RecyclerView item
     * clicks.
     *
     * @param movie The movie for the poster that was clicked
     */
    @Override
    public void onClick(Movie movie) {
        Context context = this;
        // Launch the DetailActivity using an explicit Intent
        Class destinationClass = DetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        intentToStartDetailActivity.putExtra("Movie", movie);
        startActivity(intentToStartDetailActivity);
    }


    /**
     * This method will make the View for the movies data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showMoviesDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        // Show mRecyclerView, not mMovieImageView
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the movies
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        // Hide mRecyclerView, not mMovieImageView
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected Movie[] doInBackground(String... params) {

//          If there's no orderType, there's nothing to look up. */
            if (params.length == 0) {
                return null;
            }

            String orderType = params[0];
            URL moviesRequestUrl = NetworkUtils.buildUrl(orderType);

            try {
                String jsonMoviesResponse = NetworkUtils
                        .getResponseFromHttpUrl(moviesRequestUrl);

                Movie[] moviesData = TheMovieDBJsonUtils.parseMoviesJson(jsonMoviesResponse);

                return moviesData;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(Movie[] moviesData) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (moviesData != null) {
                showMoviesDataView();
                // Instead of iterating through every movie, use mMovieAdapter.setMoviesData and pass in the movies data
                mMovieAdapter.setMoviesData(moviesData);
            } else {
                showErrorMessage();
            }
        }
    }

    /**
     * This method is used when we are resetting data, so that at one point in time during a
     * refresh of our data, you can see that there is no data showing.
     */
    private void invalidateData() {
        mMovieAdapter.setMoviesData(null);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.main, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort_order_popular) {
            invalidateData();
            loadMovies(MOST_POPULAR_ORDER_KEY);
            return true;
        }

        if (id == R.id.action_sort_order_rated) {
            invalidateData();
            loadMovies(TOP_RATED_ORDER_KEY);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
