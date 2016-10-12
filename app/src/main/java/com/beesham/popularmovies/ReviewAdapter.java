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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.R.attr.start;

/**
 * Created by Beesham on 10/11/2016.
 */

public class ReviewAdapter extends ArrayAdapter<DetailsFragment.Trailer> {
    public ReviewAdapter(Context context, List list) {
        super(context, 0, list);
    }

    @BindView(R.id.review_author_textview)  TextView reviewAuthorTextView;
    @BindView(R.id.read_review_button)  Button readReviewButton;

    private String author;
    private String reviewUrl;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final DetailsFragment.Trailer trailer = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_review, parent, false);
        }
        ButterKnife.bind(this, convertView);

        parseReview(trailer.getReviewJSONStr(), position);

        reviewAuthorTextView.setText(author);
        readReviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(reviewUrl));
                if(intent.resolveActivity(getContext().getPackageManager()) != null){
                    getContext().startActivity(intent);
                }
            }
        });

        return convertView;
    }

    /**
     * Parses the author and the review url from the JSON object based on the position
     * in the listview
     * @param reviewJSONStr
     * @param position
     */
    private void parseReview(String reviewJSONStr, int position){
        try {
            JSONObject reviewJSONObject = new JSONObject(reviewJSONStr);
            JSONArray reviewsListJSON = reviewJSONObject.getJSONArray("results");
            author = ((JSONObject) reviewsListJSON.get(position)).getString("author");
            reviewUrl = ((JSONObject) reviewsListJSON.get(position)).getString("url");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
