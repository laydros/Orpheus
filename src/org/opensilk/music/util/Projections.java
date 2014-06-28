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

import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.andrew.apollo.provider.MusicStore;

/**
 * Created by drew on 2/22/14.
 */
public class Projections {

    private Projections() {
        //static
    }

    public static final String[] LOCAL_SONG;
    public static final String[] LOCAL_ALBUM;
    public static final String[] LOCAL_ARTIST;
    public static final String[] PLAYLIST_SONGS;
    public static final String[] RECENT_SONGS;

    static {
        LOCAL_SONG = new String[] {
                BaseColumns._ID,
                MediaStore.Audio.AudioColumns.TITLE,
                MediaStore.Audio.AudioColumns.ARTIST,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.AudioColumns.ALBUM_ID,
                MediaStore.Audio.AudioColumns.DURATION,
                MediaStore.Audio.AudioColumns.MIME_TYPE,
        };
        LOCAL_ALBUM = new String[] {
                BaseColumns._ID,
                MediaStore.Audio.AlbumColumns.ALBUM,
                MediaStore.Audio.AlbumColumns.ARTIST,
                MediaStore.Audio.AlbumColumns.NUMBER_OF_SONGS,
                MediaStore.Audio.AlbumColumns.FIRST_YEAR,
                MediaStore.Audio.AlbumColumns.LAST_YEAR,
        };
        LOCAL_ARTIST = new String[] {
                BaseColumns._ID,
                MediaStore.Audio.ArtistColumns.ARTIST,
                MediaStore.Audio.ArtistColumns.NUMBER_OF_ALBUMS,
                MediaStore.Audio.ArtistColumns.NUMBER_OF_TRACKS,
        };
        PLAYLIST_SONGS = new String[] {
                        /* 0 */
                //MediaStore.Audio.Playlists.Members._ID,
                        /* 1 */
                MediaStore.Audio.Playlists.Members.AUDIO_ID + " AS _id ",
                        /* 2 */
                MediaStore.Audio.AudioColumns.TITLE,
                        /* 3 */
                MediaStore.Audio.AudioColumns.ARTIST,
                        /* 4 */
                MediaStore.Audio.AudioColumns.ALBUM,
                        /* 4 */
                MediaStore.Audio.AudioColumns.ALBUM_ID,
                        /* 5 */
                MediaStore.Audio.AudioColumns.DURATION,
        };
        RECENT_SONGS = new String[] {
                MusicStore.Cols._ID,
                MusicStore.Cols.IDENTITY,
                MusicStore.Cols.NAME,
                MusicStore.Cols.ALBUM_NAME,
                MusicStore.Cols.ARTIST_NAME,
                MusicStore.Cols.ALBUM_ARTIST_NAME,
                MusicStore.Cols.ALBUM_IDENTITY,
                MusicStore.Cols.DURATION,
                MusicStore.Cols.DATA_URI,
                MusicStore.Cols.ARTWORK_URI,
                MusicStore.Cols.MIME_TYPE,
                MusicStore.Cols.ISLOCAL,
                MusicStore.Cols.PLAYCOUNT,
                MusicStore.Cols.LAST_PLAYED,
        };
    }

}
