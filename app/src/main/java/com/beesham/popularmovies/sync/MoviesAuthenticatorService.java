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

package com.beesham.popularmovies.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Beesham on 10/7/2016.
 * The service that allows the sync adapter framework to access the authenticator
 */

public class MoviesAuthenticatorService extends Service {

    private com.beesham.popularmovies.sync.MoviesAuthenticator mMoviesAuthenticator;

    @Override
    public void onCreate() {
        mMoviesAuthenticator = new com.beesham.popularmovies.sync.MoviesAuthenticator(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMoviesAuthenticator.getIBinder();
    }
}
