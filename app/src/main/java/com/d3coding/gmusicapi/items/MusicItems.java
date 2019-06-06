package com.d3coding.gmusicapi.items;

public class MusicItems {
    private String albumArtUrl, title, artist, album, time;

    public MusicItems(String albumArtUrl, String title, String artist, String album, String time) {
        this.albumArtUrl = albumArtUrl;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.time = time;
    }

    public String getAlbum() {
        return album;
    }

    public String getAlbumArtUrl() {
        return albumArtUrl;
    }

    public String getArtist() {
        return artist;
    }

    public String getTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }
}