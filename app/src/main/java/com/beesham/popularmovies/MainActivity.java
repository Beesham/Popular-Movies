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
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.beesham.popularmovies.sync.MoviesSyncAdapter;

public class MainActivity extends AppCompatActivity implements DetailsFragment.Callback, DiscoveryFragment.Callback {

    private String mSortBy;
    private boolean mTwoPane;
    private static final String DETAIL_FRAGMENT_TAG = "DFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(findViewById(R.id.movie_details_container) != null){
            mTwoPane = true;
            if(savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_details_container, new DetailsFragment())
                        .commit();
            }
        }else{
            mTwoPane = false;
        }

        DiscoveryFragment discoveryFragment = ((DiscoveryFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_discovery));
        discoveryFragment.setTwoPane(mTwoPane);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sort_by = prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));

        setActionBarTitle(sort_by);

        MoviesSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if(mTwoPane){
            Bundle args = new Bundle();
            args.putBoolean("twoPane", mTwoPane);
            args.putParcelable(DetailsFragment.DETAIL_URI, contentUri);

            DetailsFragment detailsFragment = new DetailsFragment();
            detailsFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_details_container, detailsFragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        }else {
            Intent intent = new Intent(this, DetailsActivity.class).setData(contentUri);
            startActivity(intent);
        }
    }

    public void removeDetailsFragment(){
        getSupportFragmentManager().beginTransaction()
                .remove(getSupportFragmentManager().findFragmentByTag(DETAIL_FRAGMENT_TAG))
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String sortBy = prefs.getString(getString(R.string.pref_sort_key),
                getString(R.string.pref_sort_default));

        if(sortBy != null && !sortBy.equals(mSortBy)){
            DiscoveryFragment discoveryFragment = (DiscoveryFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_discovery);
            if(discoveryFragment != null) discoveryFragment.onSortChanged();
            mSortBy = sortBy;
        }
    }

    @Override
    public void setActionBarTitle(String title) {
        ActionBar actionBar =  getSupportActionBar();

        if(title.equals(getString(R.string.pref_sort_popular)))
            actionBar.setTitle(R.string.label_popular);
        else if(title.equals(getString(R.string.pref_sort_top_rated)))
            actionBar.setTitle(R.string.label_top_rated);
        else actionBar.setTitle(R.string.label_favorites);
    }
}
