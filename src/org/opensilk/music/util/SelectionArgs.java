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

package org.opensilk.music.util;

/**
 * Created by drew on 6/24/14.
 */
public class SelectionArgs {

    public static final String[] LOCAL_SONG;
    public static String[] LOCAL_ALBUM_SONGS(final long albumId) {
        return new String[] {"1", "''", String.valueOf(albumId)};
    }
    public static String[] LAST_ADDED() {
        final int fourWeeks = 4 * 3600 * 24 * 7;
        return new String[] {"1", "''", String.valueOf(System.currentTimeMillis() / 1000 - fourWeeks)};
    }
    public static String[] LOCAL_ARTIST_SONGS(final long artistId) {
        return new String[] {"1", String.valueOf(artistId)};
    }
    public static final String[] GENRE;
    public static final String[] GENRE_MEMBER;
    public static final String[] PLAYLIST_MEMBER;
    public static final String[] PLAYLIST_SONGS;
    public static final String[] SONG_GROUP;
    public static final String[] LOCAL_ALBUM;
    public static final String[] LOCAL_ARTIST;

    static {
        LOCAL_SONG = new String[] {"1","''"};
        GENRE = new String[] {"''"};
        GENRE_MEMBER = LOCAL_SONG;
        PLAYLIST_MEMBER = LOCAL_SONG;
        PLAYLIST_SONGS = LOCAL_SONG;
        SONG_GROUP = LOCAL_SONG;
        LOCAL_ALBUM = new String[] {"''"};
        LOCAL_ARTIST = new String[] {"''"};
    }
}
