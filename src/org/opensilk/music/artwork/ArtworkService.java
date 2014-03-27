/*
 * Copyright (C) 2014 OpenSilk Productions LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensilk.music.artwork;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.andrew.apollo.BuildConfig;

/**
 * Proxy for remote processes to access the ArtworkManager
 *
 * As of now the easiest way i know of to implement an IBinder
 * is to just use a service, Ergo this class exists
 *
 * Created by drew on 3/23/14.
 */
public class ArtworkService extends Service {
    private static final String TAG = ArtworkService.class.getSimpleName();
    private static final boolean D = BuildConfig.DEBUG;

    IArtworkServiceImpl mRemoteBinder;
    ArtworkManager mManager;

    @Override
    public IBinder onBind(Intent intent) {
        return mRemoteBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mRemoteBinder = new IArtworkServiceImpl(this);
        mManager = ArtworkManager.getInstance(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        throw new UnsupportedOperationException("ArtworkService is bind only");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRemoteBinder = null;
        mManager = null;
        ArtworkManager.destroy();
    }

}