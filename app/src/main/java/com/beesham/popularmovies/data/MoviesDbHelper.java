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

package com.beesham.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.beesham.popularmovies.data.MoviesContract.MoviesEntry;


/**
 * Created by Beesham on 10/6/2016.
 * Manages a local database for movie data
 */
public class MoviesDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "movies.db";

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesEntry.TABLE_NAME + "("
                + MoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + MoviesEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, "
                + MoviesEntry.COLUMN_MOVIE_SYNOPSIS + " TEXT NOT NULL, "
                + MoviesEntry.COLUMN_MOVIE_POSTER + " TEXT NOT NULL, "
                + MoviesEntry.COLUMN_MOVIE_RELEASE_DATE + " INTEGER NOT NULL, "
                + MoviesEntry.COLUMN_MOVIE_USER_RATING + " TEXT NOT NULL, "
                + MoviesEntry.COLUMN_MOVIE_TRAILERS + " TEXT NOT NULL,"
                + MoviesEntry.COLUMN_MOVIE_REVIEWS + " TEXT NOT NULL"
                + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS" + MoviesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
