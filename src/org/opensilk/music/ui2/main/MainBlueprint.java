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

package org.opensilk.music.ui2.main;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.andrew.apollo.MusicPlaybackService;

import org.opensilk.common.flow.AppFlow;
import org.opensilk.music.ui2.ActivityBlueprint;
import org.opensilk.music.ui2.theme.Themer;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import flow.Flow;
import hugo.weaving.DebugLog;
import mortar.Blueprint;
import mortar.MortarScope;
import mortar.ViewPresenter;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscription;
import rx.android.events.OnClickEvent;
import rx.android.observables.AndroidObservable;
import rx.android.observables.ViewObservable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.observers.Observers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

/**
 * Created by drew on 10/5/14.
 */
public class MainBlueprint {

    @Singleton
    public static class Presenter extends ViewPresenter<MainView> {

        final MusicServiceConnection musicService;

        @Inject
        protected Presenter(MusicServiceConnection musicService) {
            Timber.v("new MainViewBlueprint.Presenter()");
            this.musicService = musicService;
        }

        @Override
        protected void onEnterScope(MortarScope scope) {
            Timber.v("onEnterScope(%s)", scope);
            super.onEnterScope(scope);
        }

        @Override
        protected void onExitScope() {
            Timber.v("onExitScope()");
            super.onExitScope();
        }

        @Override
        public void onLoad(Bundle savedInstanceState) {
            Timber.v("onLoad(%s)", savedInstanceState);
            super.onLoad(savedInstanceState);
            setupObservables();
            setupObservers();
            subscribeFabClicks();
            subscribeBroadcasts();
        }

        @Override
        public void onSave(Bundle outState) {
            Timber.v("onSave(%s)", outState);
            super.onSave(outState);
        }

        @Override
        public void dropView(MainView view) {
            super.dropView(view);
            unsubscribeFabClicks();
            unsubscribeBroadcasts();
        }

        void updateFabPlay(boolean playing) {
            MainView v = getView();
            if (v == null) return;
            v.fabPlay.setIcon(playing ? Themer.getPauseIcon(v.getContext(), true)
                    : Themer.getPlayIcon(v.getContext(), true));
        }

        @DebugLog
        void openQueue() {
            MainView  v = getView();
            if (v == null) return;
            Flow flow = AppFlow.get(v.getContext());
            if (flow.getBackstack().current().getScreen() instanceof QueueScreen) return;
            flow.goTo(new QueueScreen());
        }

        @DebugLog
        void closeQueue() {
            MainView  v = getView();
            if (v == null) return;
            Flow flow = AppFlow.get(v.getContext());
            if (flow.getBackstack().current().getScreen() instanceof QueueScreen) flow.goBack();
        }

        Subscription fabPlaySubscription;
        Subscription fabNextSubscription;
        Subscription fabPrevSubscription;

        void subscribeFabClicks() {
            MainView v = getView();
            if (v == null) return;
            fabPlaySubscription = ViewObservable.clicks(v.fabPlay).subscribe(new Action1<OnClickEvent>() {
                @Override
                public void call(OnClickEvent onClickEvent) {
                    musicService.playOrPause()
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new Action1<Boolean>() {
                                @Override
                                public void call(Boolean playing) {
                                    updateFabPlay(playing);
                                }
                            });
                }
            });
            fabNextSubscription = ViewObservable.clicks(v.fabNext).subscribe(new Action1<OnClickEvent>() {
                @Override
                public void call(OnClickEvent onClickEvent) {
                    musicService.next();
                }
            });
            fabPrevSubscription = ViewObservable.clicks(v.fabPrev).subscribe(new Action1<OnClickEvent>() {
                @Override
                public void call(OnClickEvent onClickEvent) {
                    musicService.prev();
                }
            });
        }

        void unsubscribeFabClicks() {
            if (!notSubscribed(fabPlaySubscription)) {
                fabPlaySubscription.unsubscribe();
                fabPlaySubscription = null;
            }
            if (!notSubscribed(fabNextSubscription)) {
                fabNextSubscription.unsubscribe();
                fabNextSubscription = null;
            }
            if (!notSubscribed(fabPrevSubscription)) {
                fabPrevSubscription.unsubscribe();
                fabPrevSubscription = null;
            }
        }

        Observable<Boolean> playStateObservable;
        Observable<Integer> repeatModeObservable;
        Observable<Intent> shuffleModeObservable;

        void setupObservables() {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(MusicPlaybackService.PLAYSTATE_CHANGED);
            intentFilter.addAction(MusicPlaybackService.REPEATMODE_CHANGED);
            intentFilter.addAction(MusicPlaybackService.SHUFFLEMODE_CHANGED);
            Scheduler scheduler = Schedulers.computation();
            // obr will call onNext on the main thread so we observeOn computation
            // so our chained operators will be called on computation instead of main.
            Observable<Intent> intentObservable = AndroidObservable.fromBroadcast(getView().getContext(), intentFilter).observeOn(scheduler);
            playStateObservable = intentObservable
                    // Filter for only PLAYSTATE_CHANGED actions
                    .filter(new Func1<Intent, Boolean>() {
                        // called on computation
                        @Override
                        public Boolean call(Intent intent) {
                            Timber.v("playstateSubscripion filter called on %s", Thread.currentThread().getName());
                            return intent.getAction() != null && intent.getAction().equals(MusicPlaybackService.PLAYSTATE_CHANGED);
                        }
                    })
                            // filter out repeats only taking most recent
                    .debounce(20, TimeUnit.MILLISECONDS, scheduler)
                            // flatMap the intent into a boolean by requesting the playstate
                            // XXX the intent contains the playstate as an extra but
                            //     it could be out of date
                    .flatMap(new Func1<Intent, Observable<Boolean>>() {
                        @Override
                        public Observable<Boolean> call(Intent intent) {
                            Timber.v("playstateSubscription flatMap called on %s", Thread.currentThread().getName());
                            return musicService.isPlaying();
                        }
                    })
                            // observe final result on main thread
                    .observeOn(AndroidSchedulers.mainThread());
        }

        Observer<Boolean> playStateObserver;
        Observer<Integer> repeatModeObserver;
        Observer<Integer> shuffleModeObserver;

        void setupObservers() {
            playStateObserver = Observers.create(new Action1<Boolean>() {
                @Override
                public void call(Boolean playing) {
                    updateFabPlay(playing);
                }
            });
        }

        CompositeSubscription broadcastSubscriptions;

        void subscribeBroadcasts() {
            if (notSubscribed(broadcastSubscriptions)) {
                broadcastSubscriptions = new CompositeSubscription(
                        playStateObservable.subscribe(playStateObserver)
                );
            }
        }

        void unsubscribeBroadcasts() {
            if (notSubscribed(broadcastSubscriptions)) return;
            broadcastSubscriptions.unsubscribe();
            broadcastSubscriptions = null;
        }

        static boolean notSubscribed(Subscription subscription) {
            return subscription == null || subscription.isUnsubscribed();
        }

    }

}