package net.notfab.lindsey.core.framework.models;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PlayListCursor extends Song {

    private int position;

    public static PlayListCursor fromSong(Song song) {
        PlayListCursor cursor = new PlayListCursor();
        cursor.setIdentifier(song.getIdentifier());
        cursor.setName(song.getName());
        cursor.setAuthor(song.getAuthor());
        cursor.setUrl(song.getUrl());
        cursor.setLength(song.getLength());
        cursor.setSource(song.getSource());
        cursor.setStream(song.isStream());
        return cursor;
    }

}
