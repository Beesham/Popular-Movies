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


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

    static final String DETAIL_URI = "URI";
    private static final int MOVIE_LOADER = 1;

    @BindView(R.id.title_textview) TextView titleTextView;
    @BindView(R.id.poster_imageview) ImageView posterImageView;
    @BindView(R.id.release_date_textview) TextView releaseDateTextView;
    @BindView(R.id.overview_textview) TextView overviewTextView;
    @BindView(R.id.rating_textview) TextView ratingsTextView;
    @BindView(R.id.list_trailers) ListView trailersListView;
    @BindView(R.id.empty_trailers_textview) TextView emptyTrailersTextView;
    @BindView(R.id.list_reviews) ListView reviewsListView;
    @BindView(R.id.empty_reviews_textview) TextView emptyReviewsTextView;


    private String mReviewJSONStr;
    private String mTrailersStr;
    private ArrayList mTrailerAndReviewsList;
    private TrailersAdapter mTrailersAdapter;
    private ReviewAdapter mReviewAdapter;

    class Trailer{
        String trailerName;
        String trailerKey;
        String reviewJSONStr;

        public Trailer(String trailerName, String trailerKey, String reviewJSONStr){
            this.trailerName = trailerName;
            this.trailerKey = trailerKey;
            this.reviewJSONStr = reviewJSONStr;
        }

        public String getTrailerName() {
            return trailerName;
        }

        public String getTrailerKey() {
            return trailerKey;
        }

        public String getReviewJSONStr() {
            return reviewJSONStr;
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

        mTrailerAndReviewsList = new ArrayList();

        mReviewAdapter = new ReviewAdapter(getActivity(), mTrailerAndReviewsList);
        reviewsListView.setAdapter(mReviewAdapter);
        reviewsListView.setEmptyView(emptyReviewsTextView);

        mTrailersAdapter = new TrailersAdapter(getActivity(), mTrailerAndReviewsList);
        trailersListView.setAdapter(mTrailersAdapter);
        trailersListView.setEmptyView(emptyTrailersTextView);
        trailersListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(getString(R.string.movies_base_youtube_url, ((Trailer) mTrailerAndReviewsList.get(i)).getTrailerKey())));
                        if(intent.resolveActivity(getActivity().getPackageManager()) != null){
                            startActivity(intent);
                        }
                    }
                }
        );

        Bundle args = getArguments();
        if(args != null){
            mUri = args.getParcelable(DetailsFragment.DETAIL_URI);
        }

        return rootView;
    }

    private void parseTrailersAndSetReview(){
        try {
            JSONObject trailersJSON = new JSONObject(mTrailersStr);
            JSONArray trailersListJSON = trailersJSON.getJSONArray("results");
            for(int i = 0; i < trailersListJSON.length(); i++){
                JSONObject trailerJSON = trailersListJSON.getJSONObject(i);
                mTrailerAndReviewsList.add(new Trailer(trailerJSON.getString("name"), trailerJSON.getString("key"), mReviewJSONStr));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mTrailersAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                MoviesContract.MoviesEntry._ID,
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
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(!data.moveToFirst()) return;
        titleTextView.setText(data.getString(data.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE)));
        Picasso.with(getContext()).load(data.getString(data.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_POSTER))).into(posterImageView);
        overviewTextView.setText(data.getString(data.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_SYNOPSIS)));
        ratingsTextView.setText(getString(R.string.rating, data.getString(data.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_USER_RATING))));
        releaseDateTextView.setText(data.getString(data.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_RELEASE_DATE)));
        mTrailersStr = data.getString(data.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_TRAILERS));
        mReviewJSONStr = data.getString(data.getColumnIndex(MoviesContract.MoviesEntry.COLUMN_MOVIE_REVIEWS));

        parseTrailersAndSetReview();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


}
