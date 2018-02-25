package com.udacity.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.udacity.popularmovies.model.Movie;
import com.udacity.popularmovies.utilities.TheMovieDBJsonUtils;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        if (intent == null) {
            closeOnError();
        }

        Movie movie = intent.getParcelableExtra("Movie");

        if (movie == null) {
            // Movie data not found in intent
            closeOnError();
            return;
        }

        populateUI(movie);

    }

    private void populateUI(Movie movie) {

        ImageView mPosterIv = findViewById(R.id.poster_iv);
        TextView mTitleTv = findViewById(R.id.title_tv);
        TextView mReleaseDateTv = findViewById(R.id.release_date_tv);
        TextView mRatingTv = findViewById(R.id.rating_tv);
        TextView mPlotSynopsisTv = findViewById(R.id.plot_synopsis_tv);

        //display Movie detail data
        Uri imgUri = Uri.parse(TheMovieDBJsonUtils.TMDB_POSTER_PATH).buildUpon()
                .appendEncodedPath(TheMovieDBJsonUtils.TMDB_POSTER_WIDTH_MEDIUM)
                .appendEncodedPath(movie.getPoster())
                .build();

        Picasso.with(mPosterIv.getContext()).load(imgUri).into(mPosterIv);
        mPosterIv.setContentDescription(movie.getTitle());

        mTitleTv.setText(movie.getTitle());
        mReleaseDateTv.setText(movie.getReleaseDate());
        String rating = movie.getRating().concat("/10");
        mRatingTv.setText(rating);
        mPlotSynopsisTv.setText(movie.getPlotSynopsis());

    }

    private void closeOnError() {
        finish();
        Toast.makeText(this, R.string.detail_error_message, Toast.LENGTH_SHORT).show();
    }

}
