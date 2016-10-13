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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.resource;

/**
 * Created by Beesham on 10/11/2016.
 */

public class TrailersAdapter extends ArrayAdapter<DetailsFragment.Trailer> {

    @BindView(R.id.trailer_name_textview) TextView trailerName;

    public TrailersAdapter(Context context, List list) {
        super(context, 0, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DetailsFragment.Trailer trailer = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_trailer, parent, false);
        }
        ButterKnife.bind(this, convertView);

        trailerName.setText(trailer.getTrailerName());

        return convertView;
    }
}
