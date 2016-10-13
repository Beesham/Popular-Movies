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
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.start;

/**
 * Created by Beesham on 10/11/2016.
 */

public class ReviewAdapter extends ArrayAdapter<DetailsFragment.Reviews> {
    public ReviewAdapter(Context context, List list) {
        super(context, 0, list);
    }

    @BindView(R.id.review_author_textview)  TextView reviewAuthorTextView;
    @BindView(R.id.read_review_button)  Button readReviewButton;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DetailsFragment.Reviews reviews = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_review, parent, false);
        }
        ButterKnife.bind(this, convertView);

        reviewAuthorTextView.setText(reviews.getAuthor());

        readReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(reviews.getUrl()));
                if(intent.resolveActivity(getContext().getPackageManager()) != null){
                    getContext().startActivity(intent);
                }
            }
        });

        return convertView;
    }


}
