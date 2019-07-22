package com.d3coding.gmusicapi.items;

public class PlaylistItem {
    private String title, UUID;

    public PlaylistItem(String title, String UUID) {
        this.title = title;
        this.UUID = UUID;
    }

    public String getTitle() {
        return title;
    }

    public String getUUID() {
        return UUID;
    }
}
