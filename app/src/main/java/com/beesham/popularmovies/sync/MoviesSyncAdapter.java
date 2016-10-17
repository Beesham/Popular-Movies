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

package com.beesham.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.beesham.popularmovies.R;
import com.beesham.popularmovies.data.MoviesContract;
import com.beesham.popularmovies.data.MoviesContract.MoviesEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import static android.R.attr.y;
import static android.R.transition.move;
import static android.os.Build.VERSION_CODES.M;

/**
 * Created by Beesham on 10/7/2016.
 */

public class MoviesSyncAdapter extends AbstractThreadedSyncAdapter{

    private final String LOG_TAG = MoviesSyncAdapter.class.getSimpleName();
    private static final String API_KEY = "678a4b86323ad301d4fb79d39d7ed2ea";   //TODO: place API key here

    public MoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle bundle, String s, ContentProviderClient contentProviderClient, SyncResult syncResult) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String moviesJsonStr = null;

        try{
            final String MOVIES_BASE_URL = getContext().getString(R.string.movies_base_url);
            final String API_KEY_PARAM = "api_key";

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
            String sort_by = prefs.getString(getContext().getString(R.string.pref_sort_key),
                    getContext().getString(R.string.pref_sort_default));

            Uri builtUri = Uri.parse(MOVIES_BASE_URL).buildUpon()
                    .appendPath(sort_by)
                    .appendQueryParameter(API_KEY_PARAM, API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if(inputStream == null) return;

            reader = new BufferedReader((new InputStreamReader(inputStream)));
            String line;
            while((line = reader.readLine()) != null){
                buffer.append(line + "\n");
            }

            if(buffer.length() == 0) return;

            moviesJsonStr = buffer.toString();
            getMovieDataFromJson(moviesJsonStr);

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if(urlConnection != null) urlConnection.disconnect();
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        }
    }

    private String getTrailersOrReviews(String movieId, int flag){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String movieJsonStr = null;
        String MOVIE_QUERY_URL = null;

        try{
            switch (flag){
                case 0:
                    MOVIE_QUERY_URL = getContext().getString(R.string.movies_base_trailers_url, movieId);
                    break;
                case 1:
                    MOVIE_QUERY_URL = getContext().getString(R.string.movies_base_reviews_url, movieId);
                    break;
            }
            final String API_KEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(MOVIE_QUERY_URL).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if(inputStream == null) return null;

            reader = new BufferedReader((new InputStreamReader(inputStream)));
            String line;
            while((line = reader.readLine()) != null){
                buffer.append(line + "\n");
            }

            if(buffer.length() == 0) return null;

            movieJsonStr = buffer.toString();

        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        } finally {
            if(urlConnection != null) urlConnection.disconnect();
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        }

        return movieJsonStr;
    }

    /**
     * Parses the movie data from a JSON string
     * and saves it in the database
     * @param moviesJsonStr
     */
    private void getMovieDataFromJson(String moviesJsonStr) throws JSONException {
        final String BASE_IMAGE_URL =  getContext().getString(R.string.movies_base_image_url);

        JSONObject moviesJSON = new JSONObject(moviesJsonStr);
        JSONArray moviesListJSON = moviesJSON.getJSONArray("results");

        Vector<ContentValues> contentValuesVector = new Vector<>(moviesListJSON.length());

        for(int i = 0; i < moviesListJSON.length(); i++ ) {
            String title;
            String id;
            String overview;
            String posterPath;
            double rating;
            String release_date;
            String trailers;
            String reviews;

            JSONObject movieJSON = moviesListJSON.getJSONObject(i);

            title = movieJSON.getString("title");
            id = movieJSON.getString("id");
            overview = movieJSON.getString("overview");
            posterPath = movieJSON.getString("poster_path");
            rating = movieJSON.getDouble("vote_average");
            release_date = movieJSON.getString("release_date");
            trailers = getTrailersOrReviews(id, 0);
            reviews = getTrailersOrReviews(id, 1);

            ContentValues movieValues = new ContentValues();

            movieValues.put(MoviesEntry.COLUMN_MOVIE_ID, id);
            movieValues.put(MoviesEntry.COLUMN_MOVIE_TITLE, title);
            movieValues.put(MoviesEntry.COLUMN_MOVIE_SYNOPSIS, overview);
            movieValues.put(MoviesEntry.COLUMN_MOVIE_POSTER, BASE_IMAGE_URL + posterPath);
            movieValues.put(MoviesEntry.COLUMN_MOVIE_USER_RATING, rating);
            movieValues.put(MoviesEntry.COLUMN_MOVIE_RELEASE_DATE, release_date);
            movieValues.put(MoviesEntry.COLUMN_MOVIE_TRAILERS, trailers);
            movieValues.put(MoviesEntry.COLUMN_MOVIE_REVIEWS, reviews);

            contentValuesVector.add(movieValues);
        }

        int inserted = 0;
        if(contentValuesVector.size() > 0){
            ContentValues[] contentValuesArray = new ContentValues[contentValuesVector.size()];
            contentValuesVector.toArray(contentValuesArray);

            getContext().getContentResolver().delete(MoviesEntry.CONTENT_URI, null, null);
            inserted = getContext().getContentResolver().bulkInsert(MoviesEntry.CONTENT_URI, contentValuesArray);
        }

    }

    public static void initializeSyncAdapter(Context context){
        getSyncAccount(context);
    }

    private static Account getSyncAccount(Context context) {
        AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        if(accountManager.getPassword(newAccount) == null){
            if(!accountManager.addAccountExplicitly(newAccount, "", null)){
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        syncImmediately(context);
    }

    public static void syncImmediately(Context context) {
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), b);
    }

}
