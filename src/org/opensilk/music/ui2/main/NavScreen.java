/*
 * Copyright (c) 2014 OpenSilk Productions LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.opensilk.music.ui2.main;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import org.opensilk.music.R;
import com.andrew.apollo.utils.NavUtils;

import org.opensilk.music.api.meta.PluginInfo;
import org.opensilk.music.loader.AsyncLoader;
import org.opensilk.music.loader.PluginInfoLoader;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import flow.Flow;
import flow.Layout;
import mortar.Blueprint;
import mortar.ViewPresenter;

/**
 * Note to self: we arent embedding this listview in order to obtain the GodView's flow
 *      so we make sure to only add this screen after the GodView is inflated
 *
 * Created by drew on 10/4/14.
 */
@Layout(R.layout.drawer_list)
public class NavScreen implements Blueprint {

    @Override
    public String getMortarScopeName() {
        return getClass().getName();
    }

    @Override
    public Object getDaggerModule() {
        return new Module();
    }

    @dagger.Module(
            injects = NavView.class,
            addsTo = MainScreen.Module.class
    )
    public static class Module {

    }

    @Singleton
    public static class Presenter extends ViewPresenter<NavView> implements AsyncLoader.Callback<PluginInfo> {

        final Flow flow;
        final DrawerPresenter drawerPresenter;
        final PluginInfoLoader loader;

        @Inject
        public Presenter(Flow flow, DrawerPresenter drawerPresenter, PluginInfoLoader loader) {
            this.flow = flow;
            this.drawerPresenter = drawerPresenter;
            this.loader = loader;
        }

        @Override
        protected void onLoad(Bundle savedInstanceState) {
            super.onLoad(savedInstanceState);
            getView().setup();
            loader.loadAsync(this);
        }

        @Override
        protected void onSave(Bundle outState) {
            super.onSave(outState);
        }

        @Override
        public void onDataFetched(List<PluginInfo> items) {
            NavView v = getView();
            if (v != null) {
                v.getAdapter().clear();
                v.getAdapter().loadPlugins(items);
            }
        }

        public void go(Blueprint screen) {
            if (screen == null) return;
//            Observable<Blueprint> oo = Observable.just(screen);
//            Observable<Action0> galleryNfolders = oo.filter(new Func1<Blueprint, Boolean>() {
//                @Override
//                public Boolean call(Blueprint blueprint) {
//                    return (blueprint instanceof FolderScreen) || (blueprint instanceof GalleryScreen);
//                }
//            }).map(new Func1<Blueprint, Action0>() {
//                @Override
//                public Action0 call(final Blueprint blueprint) {
//                    return new Action0() {
//                        @Override
//                        public void call() {
//                            flow.replaceTo(blueprint);
//                        }
//                    };
//                }
//            });
//            Observable<Action0> plugins = oo.filter(new Func1<Blueprint, Boolean>() {
//                @Override
//                public Boolean call(Blueprint blueprint) {
//                    return (blueprint instanceof LibraryScreen);
//                }
//            }).cast(LibraryScreen.class).map(new Func1<LibraryScreen, Action0>() {
//                @Override
//                public Action0 call(LibraryScreen libraryScreen) {
//                    return new Action0() {
//                        @Override
//                        public void call() {
//
//                        }
//                    };
//                }
//            });

//            Observable.merge(galleryNfolders, plugins).subscribe(new Action1<Action0>() {
//                @Override
//                public void call(Action0 action0) {
//                    drawerPresenter.closeDrawer();
//                    action0.call();
//                }
//            });
            drawerPresenter.closeDrawer();
            flow.replaceTo(screen);
        }

        public void openSettings(Context context) {
            NavUtils.openSettings((Activity)context);
        }
    }
}
