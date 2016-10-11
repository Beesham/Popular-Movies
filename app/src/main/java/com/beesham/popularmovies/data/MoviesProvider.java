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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.beesham.popularmovies.data.MoviesContract.MoviesEntry;

/**
 * Created by Beesham on 10/6/2016.
 */
public class MoviesProvider extends ContentProvider{

    private MoviesDbHelper mMoviesDbHelper;

    private static final int MOVIES = 100;
    private static final int MOVIES_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static{
        sUriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES, MOVIES);
        sUriMatcher.addURI(MoviesContract.CONTENT_AUTHORITY, MoviesContract.PATH_MOVIES + "/#", MOVIES_ID);
    }

    @Override
    public boolean onCreate() {
        mMoviesDbHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)){
            case MOVIES:
                retCursor = mMoviesDbHelper.getReadableDatabase().query(
                        MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder,
                        null,
                        null);
                break;

            case MOVIES_ID:
                selection = MoviesEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                retCursor = mMoviesDbHelper.getReadableDatabase().query(
                        MoviesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder,
                        null,
                        null);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch(sUriMatcher.match(uri)){
            case MOVIES:
                return MoviesContract.MoviesEntry.CONTENT_TYPE;
            case MOVIES_ID:
                return MoviesContract.MoviesEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        Uri returnUri = null;
        switch (sUriMatcher.match(uri)){
            case MOVIES:
                long _id = mMoviesDbHelper.getWritableDatabase().insert(
                        MoviesEntry.TABLE_NAME,
                        null,
                        contentValues);
                if(_id > 0) returnUri = ContentUris.withAppendedId(MoviesEntry.CONTENT_URI, _id);
                else throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = mMoviesDbHelper.getWritableDatabase();
        switch(sUriMatcher.match(uri)){
            case MOVIES:
                db.beginTransaction();
                int returnCount = 0;
                try{
                    for(ContentValues contentValues : values){
                        long _id = mMoviesDbHelper.getWritableDatabase().insert(
                                MoviesEntry.TABLE_NAME,
                                null,
                                contentValues);
                        if(_id != -1) returnCount++;
                    }
                    db.setTransactionSuccessful();
                }finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted = 0;

        //If selection is null, delete all rows
        if(selection == null) selection = "1";

        switch (sUriMatcher.match(uri)){
            case MOVIES:
                rowsDeleted = mMoviesDbHelper.getWritableDatabase().delete(
                        MoviesEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowsDeleted != 0) getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int rowsUpdated = 0;

        switch (sUriMatcher.match(uri)){
            case MOVIES:
                rowsUpdated = mMoviesDbHelper.getWritableDatabase().update(
                        MoviesEntry.TABLE_NAME,
                        contentValues,
                        selection,
                        selectionArgs);
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if(rowsUpdated != 0) getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }
}
