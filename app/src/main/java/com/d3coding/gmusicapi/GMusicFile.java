package com.d3coding.gmusicapi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

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
import java.util.ArrayList;
import java.util.List;

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


    private List<Progress> ProgressList = new ArrayList<>();

    int getQueue(String uuid) {
        if (scan(uuid)) {

            new GMusicDB(context).insertUUIDbyDownloads(uuid);
            Log.i("Download ALREADY completed:", uuid);
            return 1;

        } else {
            boolean t = false;
            for (Progress progress : ProgressList)
                if (progress.UUID.equals(uuid)) {
                    t = true;
                    Log.i("Download ALREADY in queue:", uuid);
                    break;
                }

            if (t) {
                new GMusicDB(context).insertUUIDbyDownloads(uuid);
                return 1;
            } else {
                GMusicDB.TrackMetadata trackMetadata = db.selectByUUID(uuid);

                int taskId = ProgressList.size();
                ProgressList.add(new Progress(taskId));

                ProgressList.get(taskId).UUID = uuid;
                ProgressList.get(taskId).doing = Doing.stopped;
                ProgressList.get(taskId).percentage = 0.0f;

                try {
                    Log.i("Download STARTED:", uuid);
                    ProgressList.get(taskId).doing = Doing.inProgress;

                    AuthToken authToken = TokenProvider.provideToken(context.getSharedPreferences(context.getString(R.string.preferences_user)
                            , Context.MODE_PRIVATE).getString(context.getString(R.string.token), ""));
                    new GPlayMusic.Builder().setAuthToken(authToken).build().getTrackApi()
                            .getTrack(trackMetadata.uuid).download(StreamQuality.HIGH, Paths.get(getPathMP3(trackMetadata.uuid)));

                    Mp3File mp3file = new Mp3File(getPathMP3(trackMetadata.uuid));
                    ID3v2 id3v2 = new ID3v24Tag();
                    id3v2.setTitle(trackMetadata.title);
                    id3v2.setArtist(trackMetadata.artist);
                    id3v2.setComposer(trackMetadata.composer);
                    id3v2.setAlbum(trackMetadata.album);
                    id3v2.setAlbumArtist(trackMetadata.albumArtist);
                    id3v2.setYear(String.valueOf(trackMetadata.year));
                    id3v2.setGenreDescription(trackMetadata.genre);

                    // TODO: noImageBug
                    if (existsThumbImagePath(uuid))
                        id3v2.setAlbumImage(Files.readAllBytes(Paths.get(getPathJPG(trackMetadata.uuid))),
                                Files.probeContentType(Paths.get(getPathJPG(trackMetadata.uuid))));

                    mp3file.setId3v2Tag(id3v2);
                    mp3file.save(FILE_PATCH + trackMetadata.uuid + ".mp3");

                    // Success
                    new GMusicDB(context).insertUUIDbyDownloads(trackMetadata.uuid);
                    Log.i("Download FINISHED:", uuid);
                    ProgressList.get(taskId).doing = Doing.completed;
                    ProgressList.get(taskId).percentage = 100f;
                    return 1;

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Log.e("Download ERROR", uuid);
                    ProgressList.get(taskId).doing = Doing.error;
                    return 0;

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("Download ERROR", uuid);
                    ProgressList.get(taskId).doing = Doing.error;
                    return 0;
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("Download ERROR", uuid);
                    ProgressList.get(taskId).doing = Doing.error;
                    return 0;
                }
            }
        }
    }

    public Bitmap getThumbBitmap(String uuid) {
        File thumbFile = new File(getPathJPG(uuid));
        if (thumbFile.exists())
            return BitmapFactory.decodeFile(thumbFile.getAbsolutePath());
            // return Bitmap.createScaledBitmap(BitmapFactory.decodeFile(thumbFile.getAbsolutePath()), 200, 200, false);
        else {
            String albumArtUrl = db.selectColumnByUUID(uuid, GMusicDB.column.albumArtUrl);
            if (!albumArtUrl.equals(""))
                try {
                    URL url = new URL(albumArtUrl);

                    if (!thumbFile.exists()) {
                        Files.copy(url.openStream(), Paths.get(thumbFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
                        return Bitmap.createScaledBitmap(BitmapFactory.decodeFile(thumbFile.getAbsolutePath()), 200, 200, false);
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

        }
        return getDefaultThumb();
    }

    enum Doing {
        stopped, inProgress, completed, error
    }

    public boolean scan(String uuid) {
        return new File(FILE_PATCH + uuid + ".mp3").exists();
    }

    private boolean existsThumbImagePath(String uuid) {
        File imgFile = new File(getPathJPG(uuid));
        if (imgFile.exists())
            return true;
        else if (db.selectColumnByUUID(uuid, GMusicDB.column.albumArtUrl).equals("")) {
            return false;
        } else {
            try {
                return (!downloadThumbImage(uuid).equals(""));
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private String downloadThumbImage(String uuid) throws MalformedURLException {
        URL url = new URL(db.selectColumnByUUID(uuid, GMusicDB.column.albumArtUrl));
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

    class Progress {
        int id;
        String UUID;
        Doing doing;
        float percentage;

        Progress(int id) {
            this.id = id;
        }
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
