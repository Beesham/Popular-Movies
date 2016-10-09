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


import android.database.Cursor;
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
import android.widget.GridView;

import com.beesham.popularmovies.data.MoviesContract.MoviesEntry;


/**
 * A simple {@link Fragment} subclass.
 */
public class DiscoveryFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MOVIES_LOADER = 1;

    ImageAdapter mImageAdapter;

    public DiscoveryFragment() {
        // Required empty public constructor
    }

    public static DiscoveryFragment newInstance(String param1, String param2) {
        DiscoveryFragment fragment = new DiscoveryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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
                //TODO: launch details view per specific movies
            }
        });


        return rootView;
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

        return new CursorLoader(getActivity(),
                MoviesEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
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
