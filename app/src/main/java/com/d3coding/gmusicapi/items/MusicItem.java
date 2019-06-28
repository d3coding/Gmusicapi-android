package com.d3coding.gmusicapi.items;

public class MusicItem {
    private String uuid, albumArtUrl, title, artist, album, duration;
    private boolean downloadStatus;

    public MusicItem(String uuid, String albumArtUrl, String title, String artist, String album, String duration, boolean downloadStatus) {
        this.uuid = uuid;
        this.albumArtUrl = albumArtUrl;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.downloadStatus = downloadStatus;
    }

    public String getUUID() {
        return uuid;
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