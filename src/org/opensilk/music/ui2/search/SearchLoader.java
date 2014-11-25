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

package org.opensilk.music.ui2.search;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import com.andrew.apollo.model.LocalAlbum;
import com.andrew.apollo.model.LocalArtist;
import com.andrew.apollo.model.LocalSong;

import org.opensilk.common.dagger.qualifier.ForApplication;
import org.opensilk.music.ui2.loader.RxCursorLoader;
import org.opensilk.music.util.CursorHelpers;

import javax.inject.Inject;

/**
 * Created by drew on 11/24/14.
 */
public class SearchLoader extends RxCursorLoader<Object> {

    // From MediaProvider data1 and data2 only have values for artists
    // apparently, this is kind of annoying, we might look into
    // basic search in the future but its projection is beyond me.
    static final String[] SEARCH_COLS_FANCY = new String[] {
            android.provider.BaseColumns._ID,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Artists.ARTIST,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Media.TITLE,
            "data1",
            "data2",
    };

    @Inject
    public SearchLoader(@ForApplication Context context) {
        super(context);
        setProjection(SEARCH_COLS_FANCY);
    }

    @Override
    protected Object makeFromCursor(Cursor c) {
        // Get the MIME type
        final String mimetype =
                c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
        if (mimetype.equals("artist")) {
            // get id
            final long id = c.getLong(c.getColumnIndexOrThrow(BaseColumns._ID));
            // Get the artist name
            final String name = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST));
            // Get the album count
            final int albumCount = c.getInt(c.getColumnIndexOrThrow("data1"));
            // Get the song count
            final int songCount = c.getInt(c.getColumnIndexOrThrow("data2"));
            // Build artist
            return new LocalArtist(id, name, songCount, albumCount);
        } else if (mimetype.equals("album")) {
            // Get the Id of the album
            final long id = c.getLong(c.getColumnIndexOrThrow(BaseColumns._ID));
            // Get the album name
            final String name = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM));
            // Get the artist nam
            final String artist = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST));
            // generate artwork uri
            final Uri artworkUri = CursorHelpers.generateArtworkUri(id);
            // Build the album as best we can
            return new LocalAlbum(id, name, artist, 0, null, artworkUri);
        } else { // audio
            // get id
            final long id = c.getLong(c.getColumnIndexOrThrow(BaseColumns._ID));
            // Get the track name
            final String name = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
            // Get the album name
            final String album = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
            // get artist name
            final String artist = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
            // build the song as best we can
            return new LocalSong(id, name, album, artist, null, 0, 0, CursorHelpers.generateDataUri(id), null, mimetype);
        }
    }

    public SearchLoader setFilter(String filter) {
        final Uri uri = Uri.parse("content://media/external/audio/search/fancy/" + Uri.encode(filter));
        setUri(uri);
        reset();
        return this;
    }

}
