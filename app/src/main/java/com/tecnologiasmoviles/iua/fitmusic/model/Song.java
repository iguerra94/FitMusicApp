package com.tecnologiasmoviles.iua.fitmusic.model;

import java.io.Serializable;

public class Song implements Serializable {
    private int id;
    private String songTitle;
    private String artist;
    private int songAlbumId;
    private String songCoverUri;
    private String songUri;
    private long songDurationMs;

    public Song() {}

    public Song(int id, String songTitle, String artist, int songAlbumId, String songCoverUri, String songUri, long songDurationMs) {
        this.id = id;
        this.songTitle = songTitle;
        this.artist = artist;
        this.songAlbumId = songAlbumId;
        this.songCoverUri = songCoverUri;
        this.songUri = songUri;
        this.songDurationMs = songDurationMs;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getSongAlbumId() {
        return songAlbumId;
    }

    public void setSongAlbumId(int songAlbumId) {
        this.songAlbumId = songAlbumId;
    }

    public String getSongCoverUri() {
        return songCoverUri;
    }

    public void setSongCoverUri(String songCoverUri) {
        this.songCoverUri = songCoverUri;
    }

    public String getSongUri() {
        return songUri;
    }

    public void setSongUri(String songUri) {
        this.songUri = songUri;
    }

    public long getSongDurationMs() {
        return songDurationMs;
    }

    public void setSongDurationMs(long songDurationMs) {
        this.songDurationMs = songDurationMs;
    }

    @Override
    public String toString() {
        return "Song: (id = " + id +
                ", songTitle = " + songTitle +
                ", artist = '" + artist +
                ", songAlbumId = " + songAlbumId +
                ", songCoverUri = " + songCoverUri +
                ", songUri = " + songUri +
                ", songDurationMs = " + songDurationMs + ")";
    }
}