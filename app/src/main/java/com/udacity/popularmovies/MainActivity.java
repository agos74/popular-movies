package com.udacity.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.utilities.NetworkUtils;
import com.udacity.popularmovies.utilities.TheMovieDBJsonUtils;

import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    public static final String MOST_POPULAR_ORDER_KEY = "p";
    public static final String TOP_RATED_ORDER_KEY = "t";

    private static final String MOST_POPULAR_TITLE = "Most Popular Movies";
    private static final String TOP_RATED_TITLE = "Top Rated Movies";

    private RecyclerView mRecyclerView;

    private MovieAdapter mMovieAdapter;

    private LinearLayout mErrorLayout;

    private ProgressBar mLoadingIndicator;

    private static final String ORDER_TYPE_TEXT_KEY = "order_type";

    private String mOrderType = MOST_POPULAR_ORDER_KEY;

    private static final int MOVIES_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posters);

        mRecyclerView = findViewById(R.id.recyclerview_main);

        /* This Layout with TextView and Button is used to display errors and will be hidden if there are no errors */
        mErrorLayout = findViewById(R.id.layout_error);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         */
        mLoadingIndicator = findViewById(R.id.pb_loading_indicator);

         /*
         * GridLayoutManager to show the movie posters in grid of 2 or 4 columns
         */
        GridLayoutManager layoutManager;
        if (super.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            layoutManager = new GridLayoutManager(this, 2);
        } else {
            layoutManager = new GridLayoutManager(this, 4);
        }

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


        //If savedInstanceState is not null and contains ORDER_TYPE_TEXT_KEY, set mOrderType with the value
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(ORDER_TYPE_TEXT_KEY)) {
                String orderTypeSaved = savedInstanceState
                        .getString(ORDER_TYPE_TEXT_KEY);
                mOrderType = orderTypeSaved;
            }
        }

        final Button button = findViewById(R.id.retry_button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                loadMovies(mOrderType);
            }
        });


        /* Once all of our views are setup, we can load the movies. */
        loadMovies(mOrderType);

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
        mErrorLayout.setVisibility(View.INVISIBLE);
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
        mErrorLayout.setVisibility(View.VISIBLE);
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, List<Movie>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Movie> doInBackground(String... params) {

//          If there's no orderType, there's nothing to look up. */
            if (params.length == 0) {
                return null;
            }

            String orderType = params[0];
            URL moviesRequestUrl = NetworkUtils.buildUrl(orderType);

            try {
                String jsonMoviesResponse = NetworkUtils
                        .getResponseFromHttpUrl(moviesRequestUrl);

                List<Movie> moviesList = TheMovieDBJsonUtils.parseMoviesJson(jsonMoviesResponse);

                return moviesList;

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

        }

        @Override
        protected void onPostExecute(List<Movie> moviesList) {
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if (moviesList != null) {
                showMoviesDataView();
                // Instead of iterating through every movie, use mMovieAdapter.setMoviesList and pass in the movies List
                mMovieAdapter.setMoviesList(moviesList);
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
        mMovieAdapter.setMoviesList(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Put the order type in the outState bundle
        outState.putString(ORDER_TYPE_TEXT_KEY, mOrderType);

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

        switch (id) {
            case R.id.action_sort_order_popular:
                mOrderType = MOST_POPULAR_ORDER_KEY;
                break;
            case R.id.action_sort_order_rated:
                mOrderType = TOP_RATED_ORDER_KEY;
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        invalidateData();
        loadMovies(mOrderType);
        return true;

    }


}
