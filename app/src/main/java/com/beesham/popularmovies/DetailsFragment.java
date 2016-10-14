/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.beesham.popularmovies;


import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.beesham.popularmovies.data.MoviesContract;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 * Displays details for the selected movie
 */
public class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = DetailsFragment.class.getSimpleName();

    private Uri mUri;
    private Cursor mCursor;

    static final String DETAIL_URI = "URI";
    private static final int MOVIE_LOADER = 1;
    private static final int MOVIE_FAVORITE_LOADER = 2;

    @BindView(R.id.title_textview) TextView titleTextView;
    @BindView(R.id.poster_imageview) ImageView posterImageView;
    @BindView(R.id.release_date_textview) TextView releaseDateTextView;
    @BindView(R.id.overview_textview) TextView overviewTextView;
    @BindView(R.id.rating_textview) TextView ratingsTextView;
    @BindView(R.id.favorite_button) Button favoriteButton;
    @BindView(R.id.list_trailers) ListView trailersListView;
    @BindView(R.id.empty_trailers_textview) TextView emptyTrailersTextView;
    @BindView(R.id.list_reviews) ListView reviewsListView;
    @BindView(R.id.empty_reviews_textview) TextView emptyReviewsTextView;


    private String mReviewJSONStr;
    private String mTrailersJSONStr;
    private ArrayList mTrailerList;
    private TrailersAdapter mTrailersAdapter;
    private ReviewAdapter mReviewAdapter;
    private ArrayList mReviewsList;


    class Trailer{
        String trailerName;
        String trailerKey;

        public Trailer(String trailerName, String trailerKey){
            this.trailerName = trailerName;
            this.trailerKey = trailerKey;
        }

        public String getTrailerName() {
            return trailerName;
        }

        public String getTrailerKey() {
            return trailerKey;
        }
    }

    class Reviews{
        String author;
        String url;

        public Reviews(String author, String url) {
            this.author = author;
            this.url = url;
        }

        public String getAuthor() {
            return author;
        }

        public String getUrl() {
            return url;
        }
    }

    public interface Callback{
        public void onItemSelected(Uri title);
    }

    public DetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details_view, container, false);
        ButterKnife.bind(this, rootView);

        mTrailerList = new ArrayList();
        mReviewsList = new ArrayList();

        mReviewAdapter = new ReviewAdapter(getActivity(), mReviewsList);
        reviewsListView.setAdapter(mReviewAdapter);
        reviewsListView.setEmptyView(emptyReviewsTextView);

        mTrailersAdapter = new TrailersAdapter(getActivity(), mTrailerList);
        trailersListView.setAdapter(mTrailersAdapter);
        trailersListView.setEmptyView(emptyTrailersTextView);
        trailersListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(getString(R.string.movies_base_youtube_url, ((Trailer) mTrailerList.get(i)).getTrailerKey())));
                        if(intent.resolveActivity(getActivity().getPackageManager()) != null){
                            startActivity(intent);
                        }
                    }
                }
        );

        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkForFavorite()){
                    removeFavorite();
                    favoriteButton.setText(R.string.mark_favorite);
                }else {
                    insertFavoriteMovie();
                    favoriteButton.setText("Poop");
                }
            }
        });

        Bundle args = getArguments();
        if(args != null){
            mUri = args.getParcelable(DetailsFragment.DETAIL_URI);
        }

        return rootView;
    }

    private boolean checkForFavorite(){

        Cursor c = getContext().getContentResolver().query(MoviesContract.MoviesFavoriteEntry.CONTENT_URI,
                new String[] {MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_ID},
                MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_ID + "=?",
                new String[] {mCursor.getString(mCursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID))},
                null);

        if(c.moveToFirst()){
            c.close();
            return true;
        }else{
            return false;
        }
    }

    private void insertFavoriteMovie(){
        String title = mCursor.getString(mCursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE));
        String id = mCursor.getString(mCursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID));
        String overview = mCursor.getString(mCursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_SYNOPSIS));
        String posterPath = mCursor.getString(mCursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_POSTER));
        String rating = mCursor.getString(mCursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_USER_RATING));
        String release_date = mCursor.getString(mCursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_RELEASE_DATE));
        String trailers = mCursor.getString(mCursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_TRAILERS));
        String reviews = mCursor.getString(mCursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_REVIEWS));

        ContentValues movieValues = new ContentValues();

        movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID, id);
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE, title);
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_SYNOPSIS, overview);
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_POSTER, posterPath);
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_USER_RATING, rating);
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_RELEASE_DATE, release_date);
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_TRAILERS, trailers);
        movieValues.put(MoviesContract.MoviesEntry.COLUMN_MOVIE_REVIEWS, reviews);

        getActivity().getContentResolver().insert(MoviesContract.MoviesFavoriteEntry.CONTENT_URI,
                movieValues);
    }

    private void removeFavorite(){
        String selection = MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_ID + "=?";
        String[] selectionArgs = new String[] {mCursor.getString(mCursor.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_ID))};
        getActivity().getContentResolver().delete(MoviesContract.MoviesFavoriteEntry.CONTENT_URI,
                selection,
                selectionArgs);
    }


    private void parseTrailers(){
        try {
            JSONObject trailersJSONObject = new JSONObject(mTrailersJSONStr);
            JSONArray trailersListJSON = trailersJSONObject.getJSONArray("results");
            for(int i = 0; i < trailersListJSON.length(); i++){
                JSONObject trailerJSON = trailersListJSON.getJSONObject(i);
                mTrailerList.add(new Trailer(trailerJSON.getString("name"), trailerJSON.getString("key")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTrailersAdapter.notifyDataSetChanged();
    }

    /**
    * Parses the author and the review url from the JSON object based on the position
    * in the listview
    */
    private void parseReview(){
        try {
            if(mReviewJSONStr != null) {
                JSONObject reviewJSONObject = new JSONObject(mReviewJSONStr);
                JSONArray reviewsListJSON = reviewJSONObject.getJSONArray("results");
                for (int i = 0; i < reviewsListJSON.length(); i++) {
                    JSONObject trailerJSON = reviewsListJSON.getJSONObject(i);
                    mReviewsList.add(new Reviews(trailerJSON.getString("author"), trailerJSON.getString("url")));
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        mReviewAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_by = prefs.getString(getContext().getString(R.string.pref_sort_key),
                getContext().getString(R.string.pref_sort_default));

        if(sort_by.equals("favorites")){
            getLoaderManager().initLoader(MOVIE_FAVORITE_LOADER, null, this);
        }else {
            getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = null;
        switch(id){
            case 1:
                projection = new String[]{
                        MoviesContract.MoviesEntry._ID,
                        MoviesContract.MoviesEntry.COLUMN_MOVIE_ID,
                        MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE,
                        MoviesContract.MoviesEntry.COLUMN_MOVIE_SYNOPSIS,
                        MoviesContract.MoviesEntry.COLUMN_MOVIE_POSTER,
                        MoviesContract.MoviesEntry.COLUMN_MOVIE_RELEASE_DATE,
                        MoviesContract.MoviesEntry.COLUMN_MOVIE_USER_RATING,
                        MoviesContract.MoviesEntry.COLUMN_MOVIE_TRAILERS,
                        MoviesContract.MoviesEntry.COLUMN_MOVIE_REVIEWS
                };

                if(mUri != null){
                    String selection = MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE + "=?";
                    String[] selectionArgs = {mUri.getPathSegments().get(1)};

                    return new CursorLoader(getActivity(),
                            MoviesContract.MoviesEntry.CONTENT_URI,
                            projection,
                            selection,
                            selectionArgs,
                            null);
                }


            case 2:
                projection = new String[]{
                        MoviesContract.MoviesFavoriteEntry._ID,
                        MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_ID,
                        MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_TITLE,
                        MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_SYNOPSIS,
                        MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_POSTER,
                        MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_RELEASE_DATE,
                        MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_USER_RATING,
                        MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_TRAILERS,
                        MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_REVIEWS
                };

                if(mUri != null){
                    String selection = MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_TITLE + "=?";
                    String[] selectionArgs = {mUri.getPathSegments().get(1)};

                    return new CursorLoader(getActivity(),
                            MoviesContract.MoviesFavoriteEntry.CONTENT_URI,
                            projection,
                            selection,
                            selectionArgs,
                            null);
                }
                break;
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(!data.moveToFirst()) {
            return;
        }

        switch (loader.getId()){
            case 1:
                titleTextView.setText(data.getString(data.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE)));
                Picasso.with(getContext()).load(data.getString(data.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_POSTER))).into(posterImageView);
                overviewTextView.setText(data.getString(data.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_SYNOPSIS)));
                ratingsTextView.setText(getString(R.string.rating, data.getString(data.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_USER_RATING))));
                releaseDateTextView.setText(data.getString(data.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_RELEASE_DATE)));
                mTrailersJSONStr = data.getString(data.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_TRAILERS));
                mReviewJSONStr = data.getString(data.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_REVIEWS));
                break;

            case 2:
                titleTextView.setText(data.getString(data.getColumnIndex(MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_TITLE)));
                Picasso.with(getContext()).load(data.getString(data.getColumnIndex(MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_POSTER))).into(posterImageView);
                overviewTextView.setText(data.getString(data.getColumnIndex(MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_SYNOPSIS)));
                ratingsTextView.setText(getString(R.string.rating, data.getString(data.getColumnIndex(MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_USER_RATING))));
                releaseDateTextView.setText(data.getString(data.getColumnIndex(MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_RELEASE_DATE)));
                mTrailersJSONStr = data.getString(data.getColumnIndex(MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_TRAILERS));
                mReviewJSONStr = data.getString(data.getColumnIndex(MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_REVIEWS));
                break;
        }


        mTrailerList.clear();
        mReviewsList.clear();
        parseTrailers();
        parseReview();
        mCursor = data;

        if(checkForFavorite()) {
            favoriteButton.setText("Poop");
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


}
