package com.d3coding.gmusicapi;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.View;
import android.widget.LinearLayout;

import com.d3coding.gmusicapi.items.MusicAdapter;
import com.d3coding.gmusicapi.items.MusicItem;
import com.github.felixgail.gplaymusic.api.GPlayMusic;
import com.github.felixgail.gplaymusic.model.enums.StreamQuality;
import com.github.felixgail.gplaymusic.util.TokenProvider;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import svarzee.gps.gpsoauth.AuthToken;

public class GMusicFile {

    private String MUSIC_CACHE_PATH;
    private String THUMB_CACHE_PATH;
    private String FILE_PATCH = Environment.getExternalStorageDirectory().toString() + "/Gmusicapi/";

    private Context context;

    private GMusicDB db;

    public GMusicFile(Context context) {
        this.context = context;
        db = new GMusicDB(context);

        MUSIC_CACHE_PATH = context.getApplicationInfo().dataDir + "/m_cache/";
        THUMB_CACHE_PATH = context.getApplicationInfo().dataDir + "/t_cache/";

        File music_cache_patch = new File(MUSIC_CACHE_PATH),
                thumb_cache_patch = new File(THUMB_CACHE_PATH), file_patch = new File(FILE_PATCH);

        if (!music_cache_patch.exists())
            music_cache_patch.mkdir();
        if (!thumb_cache_patch.exists())
            thumb_cache_patch.mkdir();
        if (!file_patch.exists())
            file_patch.mkdir();
    }

    public void addToQueue(String uuid, LinearLayout linearLayoutComplete, LinearLayout linearLayoutDownloading) {
        new Thread(() -> {
            if (!scan(uuid)) {
                GMusicNet.Chunk chunk = db.selectByUID(uuid);
                try {

                    Thread t = new Thread(() -> {
                        try {
                            AuthToken authToken = TokenProvider.provideToken(context.getSharedPreferences(context.getString(R.string.preferences_user)
                                    , Context.MODE_PRIVATE).getString(context.getString(R.string.token), ""));
                            new GPlayMusic.Builder().setAuthToken(authToken).build().getTrackApi()
                                    .getTrack(chunk.id).download(StreamQuality.HIGH, Paths.get(MUSIC_CACHE_PATH + chunk.id + ".mp3"));

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    t.start();

                    if (t.isAlive())
                        t.join();

                    Mp3File mp3file = new Mp3File(MUSIC_CACHE_PATH + chunk.id + ".mp3");
                    ID3v2 id3v2 = new ID3v24Tag();
                    id3v2.setTitle(chunk.title);
                    id3v2.setArtist(chunk.artist);
                    id3v2.setComposer(chunk.composer);
                    id3v2.setAlbum(chunk.album);
                    id3v2.setAlbumArtist(chunk.albumArtist);
                    id3v2.setYear(String.valueOf(chunk.year));
                    id3v2.setGenreDescription(chunk.genre);

                    if (existsThumbImagePath(uuid)) {
                        id3v2.setAlbumImage(Files.readAllBytes(Paths.get(THUMB_CACHE_PATH + chunk.id + ".jpg")),
                                Files.probeContentType(Paths.get(THUMB_CACHE_PATH + chunk.id + ".jpg")));
                    }

                    mp3file.setId3v2Tag(id3v2);
                    mp3file.save(FILE_PATCH + chunk.id + ".mp3");

                    // Success
                    new GMusicDB(context).updateDB(chunk.id, 1);
                    synchronized (this) {
                        ((Activity) context).runOnUiThread(() -> {

                            linearLayoutComplete.setVisibility(View.VISIBLE);
                            linearLayoutDownloading.setVisibility(View.GONE);
                        });
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    public boolean scan(String uuid) {
        File file = new File(FILE_PATCH + uuid + ".mp3");
        return file.exists();
    }

    public boolean existsThumbImagePath(String uuid) {
        File imgFile = new File(THUMB_CACHE_PATH + uuid + ".jpg");
        if (imgFile.exists())
            return true;
        else if (db.getThumbURL(uuid).equals("")) {
            return false;
        } else {
            try {
                return (!BitmapFactory.decodeFile(downloadThumbImage(uuid)).equals(""));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private String downloadThumbImage(String uuid) throws MalformedURLException {
        URL url = new URL(db.getThumbURL(uuid));
        try {
            File imgFile = new File(THUMB_CACHE_PATH + uuid + ".jpg");
            if (!imgFile.exists())
                Files.copy(url.openStream(), Paths.get(imgFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
            return imgFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public Bitmap getBitmapThumbImage(String uuid) {
        File imgFile = new File(THUMB_CACHE_PATH + uuid + ".jpg");
        if (imgFile.exists())
            return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        return null;
    }

    public Bitmap getDefaultThumbTemp(MusicAdapter musicAdapter, int x, MusicItem musicItems) {

        File imgFile = new File(THUMB_CACHE_PATH + musicItems.getUid() + ".jpg");
        if (musicItems.getAlbumArtUrl() != null) {
            try {
                URL url = new URL(musicItems.getAlbumArtUrl());
                new Thread(() -> {
                    try {
                        if (!imgFile.exists())
                            Files.copy(url.openStream(), Paths.get(imgFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);

                        synchronized (this) {
                            ((Activity) context).runOnUiThread(() -> {
                                musicAdapter.notifyItemChanged(x);
                            });
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } finally {
                return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            }
        }

        return BitmapFactory.decodeResource(context.getResources(), R.drawable.no_image);
    }

    public Bitmap getDefaultThumb() {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.no_image);
    }

}
