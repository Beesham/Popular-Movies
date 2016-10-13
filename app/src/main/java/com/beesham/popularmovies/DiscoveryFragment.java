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
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import com.beesham.popularmovies.data.MoviesContract;
import com.beesham.popularmovies.data.MoviesContract.MoviesEntry;
import com.beesham.popularmovies.sync.MoviesSyncAdapter;

import org.json.JSONObject;

import static android.R.attr.data;


/**
 * A simple {@link Fragment} subclass.
 * Displays movies based on popularity or highest rated
 */
public class DiscoveryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MOVIES_LOADER = 1;
    private static final int MOVIES_FAVORITE_LOADER = 2;

    ImageAdapter mImageAdapter;

    public DiscoveryFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_discovery, container, false);

        GridView moviesGridView = (GridView) rootView.findViewById(R.id.movies_gridview);
        mImageAdapter = new ImageAdapter(getContext());

        moviesGridView.setEmptyView(rootView.findViewById(R.id.empty_view));
        moviesGridView.setAdapter(mImageAdapter);
        moviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String sort_by = prefs.getString(getContext().getString(R.string.pref_sort_key),
                        getContext().getString(R.string.pref_sort_default));

                if(sort_by.equals("favorites")){
                    Cursor c = (Cursor) adapterView.getItemAtPosition(i);
                    if (c != null) {
                        ((DetailsFragment.Callback) getActivity()).onItemSelected(
                                MoviesContract.MoviesFavoriteEntry.CONTENT_URI
                                        .buildUpon()
                                        .appendPath(c.getString(c.getColumnIndex(MoviesContract.MoviesFavoriteEntry.COLUMN_MOVIE_TITLE))).build());
                    }
                }else {
                    Cursor c = (Cursor) adapterView.getItemAtPosition(i);
                    if (c != null) {
                        ((DetailsFragment.Callback) getActivity()).onItemSelected(
                                MoviesEntry.CONTENT_URI
                                        .buildUpon()
                                        .appendPath(c.getString(c.getColumnIndex(MoviesEntry.COLUMN_MOVIE_TITLE))).build());
                    }
                }
            }
        });


        return rootView;
    }

    void onSortChanged(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_by = prefs.getString(getContext().getString(R.string.pref_sort_key),
                getContext().getString(R.string.pref_sort_default));

        if(sort_by.equals("favorites")) {
            getLoaderManager().restartLoader(MOVIES_FAVORITE_LOADER, null, this);
        }else{
            MoviesSyncAdapter.syncImmediately(getActivity());
            getLoaderManager().restartLoader(MOVIES_LOADER, null, this);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.discoveryfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                MoviesContract.MoviesEntry._ID,
                MoviesContract.MoviesEntry.COLUMN_MOVIE_TITLE,
                MoviesContract.MoviesEntry.COLUMN_MOVIE_SYNOPSIS,
                MoviesContract.MoviesEntry.COLUMN_MOVIE_POSTER,
                MoviesContract.MoviesEntry.COLUMN_MOVIE_RELEASE_DATE,
                MoviesContract.MoviesEntry.COLUMN_MOVIE_USER_RATING
        };

        CursorLoader loader = null;
        switch(id) {
            case 1:
                loader = new CursorLoader(getActivity(),
                        MoviesEntry.CONTENT_URI,
                        projection,
                        null,
                        null,
                        null);
                break;

            case 2:
                loader = new CursorLoader(getActivity(),
                        MoviesContract.MoviesFavoriteEntry.CONTENT_URI,
                        projection,
                        null,
                        null,
                        null);
                break;
        }
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mImageAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mImageAdapter.swapCursor(null);
    }
}
