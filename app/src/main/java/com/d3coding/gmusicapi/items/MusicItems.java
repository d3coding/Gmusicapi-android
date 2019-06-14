package com.d3coding.gmusicapi.items;

public class MusicItems {
    private String uid, albumArtUrl, title, artist, album, duration;
    private boolean downloadStatus;

    public MusicItems(String uid, String albumArtUrl, String title, String artist, String album, String duration, boolean downloadStatus) {
        this.uid = uid;
        this.albumArtUrl = albumArtUrl;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.downloadStatus = downloadStatus;
    }

    public String getUid() {
        return uid;
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

    public String getDuration() {
        return duration;
    }

    public String getTitle() {
        return title;
    }

    public boolean getDownloadStatus() {
        return downloadStatus;
    }
}