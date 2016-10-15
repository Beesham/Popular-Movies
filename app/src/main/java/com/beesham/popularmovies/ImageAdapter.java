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
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.beesham.popularmovies.data.MoviesContract.MoviesEntry;
import com.squareup.picasso.Picasso;

/**
 * Created by Beesham on 10/6/2016.
 * Adapter to display movie posters
 */
public class ImageAdapter extends CursorAdapter {

    private Context mContext;

    public ImageAdapter(Context context) {
        super(context, null, 0);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

        ImageView imageView = new ImageView(context);
        imageView.setAdjustViewBounds(true);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setBackground(context.getDrawable(R.drawable.touch_selector));


        return imageView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Picasso.with(mContext).load(cursor.getString(cursor.getColumnIndex(MoviesEntry.COLUMN_MOVIE_POSTER))).into((ImageView) view);
    }
}
