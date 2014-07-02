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

package org.opensilk.music.ui.library;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.andrew.apollo.R;
import com.andrew.apollo.meta.LibraryInfo;
import com.andrew.apollo.utils.MusicUtils;

import org.opensilk.music.api.callback.Result;
import org.opensilk.music.api.model.Song;
import org.opensilk.music.util.RemoteLibraryUtil;

import java.util.List;

import hugo.weaving.DebugLog;

/**
 * Created by drew on 7/1/14.
 */
public class BackgroundFetcherFragment extends Fragment {

    public static String ARG_ACTION = "arg_action";
    public static String ARG_TAG = "argTAG";

    private static final boolean D = false;
    private static final int STEP = D ? 8 : 30;

    public enum Action {
        ADD_QUEUE,
        PLAY_ALL,
        SHUFFLE_ALL,
    }

    private LibraryInfo mLibraryInfo;
    private Action mAction;

    FetcherTask task;
    int numadded = 0;

    boolean isComplete = false;
    private CharSequence mMessage;
    private CharSequence mToastString;

    public interface CompleteListener {
        public void onComplete(CharSequence toastString);
        public void onMessageUpdated(CharSequence message);
    }

    private CompleteListener mListener;

    /**
     *
     * @param libraryInfo necessary info to make remote query
     * @param action query action to perform
     * @param tag tag of fragment implementing CompleteListener
     * @return
     */
    public static BackgroundFetcherFragment newInstance(LibraryInfo libraryInfo, Action action, String tag) {
        BackgroundFetcherFragment f = new BackgroundFetcherFragment();
        Bundle b = new Bundle(3);
        b.putParcelable(LibraryFragment.ARG_LIBRARY_INFO, libraryInfo);
        b.putString(ARG_ACTION, action.toString());
        b.putString(ARG_TAG, tag);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Fragment f = ((FragmentActivity) activity).getSupportFragmentManager().findFragmentByTag(getArguments().getString(ARG_TAG));
        if (f != null && f instanceof CompleteListener) {
            mListener = (CompleteListener) f;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mLibraryInfo = getArguments().getParcelable(LibraryFragment.ARG_LIBRARY_INFO);
        mAction = Action.valueOf(getArguments().getString(ARG_ACTION));

        mMessage = getString(R.string.fetching_song_list);

        task = new FetcherTask(mAction);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mListener != null) {
            mListener.onMessageUpdated(mMessage);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    class FetcherTask extends AsyncTask<Void, Void, Void> {

        final Action action;
        final Bundle bundle;
        final ListResult result;

        FetcherTask(Action action) {
            this(action, null);
        }

        FetcherTask(Action action, Bundle bundle) {
            this.action = action;
            this.bundle = bundle;
            this.result = new ListResult();
        }

        @Override
        @DebugLog
        protected Void doInBackground(Void... params) {
            if (D) {
                // artificially inflate execution time to test configuration changes
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                RemoteLibraryUtil.getService(mLibraryInfo.libraryComponent)
                        .listSongsInFolder(mLibraryInfo.libraryId, mLibraryInfo.currentFolderId, STEP, bundle, result);
                result.waitForComplete();
            } catch (RemoteException |InterruptedException e) {
                //pass
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (isCancelled()) {
                return;
            }
            if (result.songs == null || result.songs.length == 0) {
                if (numadded == 0) {
                    mToastString = getString(R.string.unable_to_fetch_songs);
                } else {
                    mToastString = getResources().getQuantityString(R.plurals.NNNtrackstoqueue, numadded, numadded);
                }
                isComplete = true;
            } else {
                switch (action) {
                    case PLAY_ALL:
                        MusicUtils.playAllSongs(getActivity(), result.songs, 0, false);
                        break;
                    case SHUFFLE_ALL:
                        MusicUtils.playAllSongs(getActivity(), result.songs, 0, true);
                        break;
                    case ADD_QUEUE:
                        MusicUtils.addSongsToQueueSilent(getActivity(), result.songs);
                        break;
                }
                numadded += result.songs.length;
                if (result.paginationBundle != null) {
                    Handler h = new Handler();
                    h.post(new Runnable() {
                        @Override
                        public void run() {
                            mMessage = getString(R.string.fetching_song_list)
                                    + " " + getResources().getQuantityString(R.plurals.Nsongs, numadded, numadded);
                            if (mListener != null) {
                                mListener.onMessageUpdated(mMessage);
                            }
                            task = new FetcherTask(Action.ADD_QUEUE, result.paginationBundle);
                            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    });
                } else {
                    mToastString = getResources().getQuantityString(R.plurals.NNNtrackstoqueue,
                            numadded, numadded);
                    isComplete = true;
                }
            }
            if (isComplete && mListener != null) {
                mListener.onComplete(mToastString);
            }
        }
    }

    class ListResult extends Result.Stub {

        Song[] songs;
        boolean done;
        Bundle paginationBundle;

        public synchronized void waitForComplete() throws InterruptedException {
            while (!done) {
                wait();
            }
        }

        @Override
        public synchronized void success(List<Bundle> items, Bundle paginationBundle) throws RemoteException {
            this.paginationBundle = paginationBundle;
            songs = new Song[items.size()];
            int ii=0;
            for (Bundle b : items) {
                try {
                    Song s = Song.BUNDLE_CREATOR.fromBundle(b);
                    songs[ii++] = s;
                } catch (IllegalArgumentException ignored) { }
            }
            done = true;
            notifyAll();
        }

        @Override
        public synchronized void failure(int code, String reason) throws RemoteException {
            done = true;
            notifyAll();
        }
    }

}
