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
import android.content.ComponentName;
import android.os.Bundle;
import android.view.View;

import com.andrew.apollo.R;

import org.opensilk.music.ui.library.adapter.FolderListArrayAdapter;
import org.opensilk.music.ui.library.adapter.LibraryLoaderCallback;
import org.opensilk.silkdagger.DaggerInjector;

/**
 * Created by drew on 6/14/14.
 */
public class FolderListFragment extends CardListFragment implements
        LibraryLoaderCallback {

    private ComponentName mLibraryComponentName;
    private String mLibraryIdentity;
    private String mFolderIdentity;

    protected FolderListArrayAdapter mAdapter;

    public static FolderListFragment newInstance(String libraryIdentity, ComponentName libraryComponentName, String folderId) {
        FolderListFragment f = new FolderListFragment();
        Bundle b = new Bundle(3);
        b.putString(LibraryFragment.ARG_IDENTITY, libraryIdentity);
        b.putParcelable(LibraryFragment.ARG_COMPONENT, libraryComponentName);
        b.putString(LibraryFragment.ARG_FOLDER_ID, folderId);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((DaggerInjector) getParentFragment()).inject(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() == null) {
            throw new RuntimeException("Null args");
        }
        mLibraryComponentName = getArguments().getParcelable(LibraryFragment.ARG_COMPONENT);
        mLibraryIdentity = getArguments().getString(LibraryFragment.ARG_IDENTITY);
        mFolderIdentity = getArguments().getString(LibraryFragment.ARG_FOLDER_ID);

        mAdapter = new FolderListArrayAdapter(getActivity(), mLibraryIdentity, mLibraryComponentName, this, (DaggerInjector)getParentFragment());
        if (savedInstanceState != null) {
            mAdapter.restoreInstanceState(savedInstanceState);
        } else {
            mAdapter.startLoad(mFolderIdentity);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setListAdapter(mAdapter);
        if (mAdapter.isOnFirstLoad()) {
            setListShown(false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mAdapter.saveInstanceState(outState);
    }

    /*
     * AbsLibraryArrayAdapter.LoaderCallback
     */

    @Override
    //@DebugLog
    public void onFirstLoadComplete() {
        setListShown(true);
    }

    /*
     * Abstract methods
     */

    @Override
    public int getListViewLayout() {
        return R.layout.card_listview_fastscroll2;
    }
}
