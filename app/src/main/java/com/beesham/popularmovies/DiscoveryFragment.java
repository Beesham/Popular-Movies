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
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.beesham.popularmovies.data.MoviesContract;
import com.beesham.popularmovies.data.MoviesContract.MoviesFavoriteEntry;
import com.beesham.popularmovies.data.MoviesContract.MoviesEntry;
import com.beesham.popularmovies.sync.MoviesSyncAdapter;

import static android.os.Build.VERSION_CODES.M;
import static android.support.v4.view.MenuItemCompat.getActionView;


/**
 * A simple {@link Fragment} subclass.
 * Displays movies based on popularity or highest rated
 */
public class DiscoveryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MOVIES_LOADER = 1;
    private static final int MOVIES_FAVORITE_LOADER = 2;

    private GridView mMoviesGridView;
    private ImageAdapter mImageAdapter;
    private boolean mTwoPane = false;

    private String SELECTED_KEY = "selected_position";
    private int mPosition = GridView.INVALID_POSITION;

    public interface Callback{
        void setActionBarTitle(String title);
    }

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

        mMoviesGridView = (GridView) rootView.findViewById(R.id.movies_gridview);
        mImageAdapter = new ImageAdapter(getContext());

        mMoviesGridView.setEmptyView(rootView.findViewById(R.id.empty_view));

        mMoviesGridView.setDrawSelectorOnTop(true);
        mMoviesGridView.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

        mMoviesGridView.setAdapter(mImageAdapter);
        mMoviesGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String sort_by = prefs.getString(getContext().getString(R.string.pref_sort_key),
                        getContext().getString(R.string.pref_sort_default));
                Cursor c = (Cursor) adapterView.getItemAtPosition(position);
                if (sort_by.equals(getString(R.string.pref_sort_favorite))) {
                    if (c != null) {
                        ((DetailsFragment.Callback) getActivity()).onItemSelected(
                                MoviesContract.MoviesFavoriteEntry.CONTENT_URI
                                        .buildUpon()
                                        .appendPath(c.getString(c.getColumnIndex(MoviesFavoriteEntry.COLUMN_MOVIE_TITLE))).build());
                        mMoviesGridView.setSelection(position);
                    }
                } else {
                    if (c != null) {
                        ((DetailsFragment.Callback) getActivity()).onItemSelected(
                                MoviesEntry.CONTENT_URI
                                        .buildUpon()
                                        .appendPath(c.getString(c.getColumnIndex(MoviesEntry.COLUMN_MOVIE_TITLE))).build());
                        mMoviesGridView.setSelection(position);
                    }
                }
                mPosition = position;
            }
        });

        if(savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)){
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        return rootView;
    }

    public void setUseFirstItem(boolean twoPane){
        if(twoPane){
            mTwoPane = true;
        }
    }

    void onSortChanged(){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_by = prefs.getString(getContext().getString(R.string.pref_sort_key),
                getContext().getString(R.string.pref_sort_default));

        if(sort_by.equals(getString(R.string.pref_sort_favorite))) {
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
            case R.id.action_popular:
                changePrefs(getString(R.string.pref_sort_popular));
                ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.pref_sort_popular));
                return true;
            case R.id.action_top_rated:
                changePrefs(getString(R.string.pref_sort_top_rated));
                ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.pref_sort_top_rated));
                return true;
            case R.id.action_favorites:
                changePrefs(getString(R.string.pref_sort_favorite));
                ((MainActivity) getActivity()).setActionBarTitle(getString(R.string.pref_sort_favorite));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void changePrefs(String pref_value){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(getString(R.string.pref_sort_key), pref_value);
        editor.commit();
        onSortChanged();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(mPosition != GridView.INVALID_POSITION){
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                MoviesEntry._ID,
                MoviesEntry.COLUMN_MOVIE_TITLE,
                MoviesEntry.COLUMN_MOVIE_SYNOPSIS,
                MoviesEntry.COLUMN_MOVIE_POSTER,
                MoviesEntry.COLUMN_MOVIE_RELEASE_DATE,
                MoviesEntry.COLUMN_MOVIE_USER_RATING
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
                        MoviesFavoriteEntry.CONTENT_URI,
                        projection,
                        null,
                        null,
                        null);
                break;
        }
        return loader;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
        mImageAdapter.swapCursor(data);

        if(mPosition != GridView.INVALID_POSITION){
            mMoviesGridView.smoothScrollToPosition(mPosition);
            mMoviesGridView.setItemChecked(mPosition, true);
        }
        if(mTwoPane &&  data.moveToFirst()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    ((DetailsFragment.Callback) getActivity()).onItemSelected(
                            MoviesEntry.CONTENT_URI
                                    .buildUpon()
                                    .appendPath(data.getString(data.getColumnIndex(MoviesEntry.COLUMN_MOVIE_TITLE))).build());
                    mMoviesGridView.setItemChecked(0, true);
                }
            });
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mImageAdapter.swapCursor(null);
    }
}
