/*
 * Copyright (C) 2012 Andrew Neal
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

package com.andrew.apollo;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.andrew.apollo.utils.PreferenceUtils;

import org.opensilk.music.artwork.ArtworkLoader;
import org.opensilk.music.artwork.ArtworkManager;
import org.opensilk.volley.RequestQueueManager;

/**
 * Use to initilaze singletons and global static variables that require context
 */
public class ApolloApplication extends Application {
    private static final boolean DEBUG = BuildConfig.DEBUG;

    /**
     * Maximum size for artwork, this will be smallest screen width
     */
    public static int sDefaultMaxImageWidthPx;

    /**
     * Largest size of any thumbnail displayed
     */
    public static int DEFAULT_THUMBNAIL_SIZE_DP = 200;

    /**
     * Largest size a thumbnail will be
     */
    public static int sDefaultThumbnailWidthPx;

    @Override
    public void onCreate() {
        /*
         * Init global static variables
         */
        sDefaultMaxImageWidthPx = getMinDisplayWidth(getApplicationContext());
        sDefaultThumbnailWidthPx = convertDpToPx(getApplicationContext(), DEFAULT_THUMBNAIL_SIZE_DP);

        /*
         * Init singletons
         */
        // preferences
//        PreferenceUtils.create(getApplicationContext());
//        // volley's request queue
//        RequestQueueManager.create(getApplicationContext());
//        // art loader
//        ArtworkLoader.create(getApplicationContext());
        ArtworkManager.create(getApplicationContext());

        /*
         * Debugging
         */

        // Enable strict mode logging
        enableStrictMode();
    }

    private void enableStrictMode() {
        if (DEBUG) {
            final StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder()
                    .detectAll().penaltyLog();
            final StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder()
                    .detectAll().penaltyLog();

            threadPolicyBuilder.penaltyFlashScreen();
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

    /**
     * Converts given dp value to density specific pixel value
     * @param context
     * @param dp
     * @return
     */
    public static int convertDpToPx(Context context, float dp) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return Math.round(dp * (metrics.densityDpi / 160f));
    }

    /**
     * Returns smallest screen dimension
     * @param context
     * @return
     */
    public static int getMinDisplayWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        Point size = new Point();
//        wm.getDefaultDisplay().getSize(size);
//        return Math.min(size.x, size.y);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return Math.min(metrics.widthPixels, metrics.heightPixels);
    }
}
