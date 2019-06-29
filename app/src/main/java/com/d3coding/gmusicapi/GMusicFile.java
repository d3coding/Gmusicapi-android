package com.d3coding.gmusicapi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.github.felixgail.gplaymusic.api.GPlayMusic;
import com.github.felixgail.gplaymusic.model.enums.StreamQuality;
import com.github.felixgail.gplaymusic.util.TokenProvider;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;

import java.io.File;
import java.io.FileNotFoundException;
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


        MUSIC_CACHE_PATH = context.getCacheDir() + "/m_cache/";
        THUMB_CACHE_PATH = context.getCacheDir() + "/t_cache/";

        File music_cache_patch = new File(MUSIC_CACHE_PATH),
                thumb_cache_patch = new File(THUMB_CACHE_PATH), file_patch = new File(FILE_PATCH);

        if (!music_cache_patch.exists())
            music_cache_patch.mkdir();
        if (!thumb_cache_patch.exists())
            thumb_cache_patch.mkdir();
        if (!file_patch.exists())
            file_patch.mkdir();
    }

    int getQueue(String uuid) {
        if (!scan(uuid)) {
            GMusicNet.Chunk chunk = db.selectByUID(uuid);
            try {
                AuthToken authToken = TokenProvider.provideToken(context.getSharedPreferences(context.getString(R.string.preferences_user)
                        , Context.MODE_PRIVATE).getString(context.getString(R.string.token), ""));
                new GPlayMusic.Builder().setAuthToken(authToken).build().getTrackApi()
                        .getTrack(chunk.id).download(StreamQuality.HIGH, Paths.get(getPathMP3(chunk.id)));

                Mp3File mp3file = new Mp3File(getPathMP3(chunk.id));
                ID3v2 id3v2 = new ID3v24Tag();
                id3v2.setTitle(chunk.title);
                id3v2.setArtist(chunk.artist);
                id3v2.setComposer(chunk.composer);
                id3v2.setAlbum(chunk.album);
                id3v2.setAlbumArtist(chunk.albumArtist);
                id3v2.setYear(String.valueOf(chunk.year));
                id3v2.setGenreDescription(chunk.genre);

                if (existsThumbImagePath(uuid))
                    id3v2.setAlbumImage(Files.readAllBytes(Paths.get(getPathJPG(chunk.id))),
                            Files.probeContentType(Paths.get(getPathJPG(chunk.id))));

                mp3file.setId3v2Tag(id3v2);
                mp3file.save(FILE_PATCH + chunk.id + ".mp3");

                // Success
                new GMusicDB(context).updateDB(chunk.id, 1);
                return 1;

            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            } catch (Exception e) {
                e.printStackTrace();
                return 0;
            }
        } else
            return 1;
    }

    private boolean scan(String uuid) {
        return new File(FILE_PATCH + uuid + ".mp3").exists();
    }

    private boolean existsThumbImagePath(String uuid) {
        File imgFile = new File(getPathJPG(uuid));
        if (imgFile.exists())
            return true;
        else if (db.getThumbURL(uuid).equals("")) {
            return false;
        } else {
            try {
                return (downloadThumbImage(uuid).equals(""));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private String downloadThumbImage(String uuid) throws MalformedURLException {
        URL url = new URL(db.getThumbURL(uuid));
        try {
            File imgFile = new File(getPathJPG(uuid));
            if (!imgFile.exists())
                Files.copy(url.openStream(), Paths.get(imgFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
            return imgFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public Bitmap getBitmapThumbImage(String uuid) {
        File imgFile = new File(getPathJPG(uuid));
        if (imgFile.exists())
            return BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        return null;
    }

    public int tryDownloadThumb(String uuid, String albumArtUrl) {
        File imgFile = new File(getPathJPG(uuid));
        if (!albumArtUrl.equals("")) {
            try {
                URL url = new URL(albumArtUrl);

                if (!imgFile.exists())
                    Files.copy(url.openStream(), Paths.get(imgFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);

            } catch (MalformedURLException e) {
                e.printStackTrace();
                new GMusicDB(context).removeThumbLink(uuid);
                return 2;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                new GMusicDB(context).removeThumbLink(uuid);
                return 2;
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
            return 1;
        } else
            return 0;
    }

    public Bitmap getDefaultThumb() {
        return BitmapFactory.decodeResource(context.getResources(), R.drawable.no_image);
    }

    public String getPathJPG(String uuid) {
        return THUMB_CACHE_PATH + uuid + ".jpg";
    }

    private String getPathMP3(String uuid) {
        return MUSIC_CACHE_PATH + uuid + ".mp3";
    }

}
