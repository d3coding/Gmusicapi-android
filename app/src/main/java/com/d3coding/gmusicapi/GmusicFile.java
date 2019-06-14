package com.d3coding.gmusicapi;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.d3coding.gmusicapi.items.MusicAdapter;
import com.d3coding.gmusicapi.items.MusicItems;
import com.github.felixgail.gplaymusic.api.GPlayMusic;
import com.github.felixgail.gplaymusic.api.TrackApi;
import com.github.felixgail.gplaymusic.model.enums.StreamQuality;
import com.github.felixgail.gplaymusic.util.TokenProvider;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.ID3v24Tag;
import com.mpatric.mp3agic.Mp3File;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import svarzee.gps.gpsoauth.AuthToken;

public class GmusicFile {

    String MUSIC_CACHE_PATH;
    String THUMB_CACHE_PATH;
    String FILE_PATCH = Environment.getExternalStorageDirectory().toString() + "/Gmusicapi/";

    private Context context;

    public GmusicFile(Context context) {
        this.context = context;

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

    public boolean scan(String uid) {
        File file = new File(FILE_PATCH + uid + ".mp3");
        return file.exists();
    }

    public void addToQueue(String albumArtUrl, MusicAdapter musicAdapter, MusicItems musicItems, int position) {

        if (scan(musicItems.getUid()))
            returnProgress(musicItems.getUid(), true);
        else {

            new Thread(() -> {
                try {

                    System.out.println(MUSIC_CACHE_PATH);
                    Thread t1 = new Thread(() -> {
                        try {

                            System.out.println(MUSIC_CACHE_PATH);
                            System.out.println("downloading");
                            AuthToken authToken = TokenProvider.provideToken(
                                    context.getSharedPreferences(context.getString(R.string.preferences_user), Context.MODE_PRIVATE).getString(context.getString(R.string.token), ""));
                            GPlayMusic api = new GPlayMusic.Builder().setAuthToken(authToken).build();
                            TrackApi trackApi = api.getTrackApi();
                            trackApi.getTrack(musicItems.getUid()).download(StreamQuality.HIGH, Paths.get(MUSIC_CACHE_PATH + musicItems.getUid() + ".mp3"));

                            System.out.println("Final Muisc");

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }), t2 = new Thread(() -> {
                        try {

                            System.out.println(MUSIC_CACHE_PATH);

                            URL url = new URL(albumArtUrl);
                            HttpURLConnection c = (HttpURLConnection) url.openConnection();
                            c.setRequestMethod("GET");
                            c.setDoOutput(true);
                            c.connect();

                            File file = new File(THUMB_CACHE_PATH);
                            file.mkdirs();
                            File outputFile = new File(file, musicItems.getUid() + ".jpg");
                            FileOutputStream fos = new FileOutputStream(outputFile);
                            InputStream is = c.getInputStream();

                            byte[] buffer = new byte[4096];
                            int len1;

                            while ((len1 = is.read(buffer)) != -1)
                                fos.write(buffer, 0, len1);

                            fos.close();
                            is.close();

                            System.out.println("Final Thumb");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    t1.start();
                    t2.start();

                    if (t1.isAlive())
                        t1.join();
                    if (t2.isAlive())
                        t1.join();

                    System.out.println(MUSIC_CACHE_PATH);

                    Mp3File mp3file = new Mp3File(MUSIC_CACHE_PATH + musicItems.getUid() + ".mp3");

                    ID3v2 id3v2 = new ID3v24Tag();
                    id3v2.setTitle(musicItems.getTitle());
                    id3v2.setAlbum(musicItems.getAlbum());
                    id3v2.setArtist(musicItems.getArtist());
                    id3v2.setAlbumImage(Files.readAllBytes(Paths.get(THUMB_CACHE_PATH + musicItems.getUid() + ".jpg")), Files.probeContentType(Paths.get(THUMB_CACHE_PATH + musicItems.getUid() + ".jpg")));
                    mp3file.setId3v2Tag(id3v2);

                    mp3file.save(FILE_PATCH + musicItems.getUid() + ".mp3");

                    // musicAdapter.notifyItemChanged(position);
                    // TODO

                    System.out.println("Final");

                    new Gmusicdb(context).updateDB(musicItems.getUid());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    void returnProgress(String uid, boolean status) {

    }

    private class Download extends AsyncTask<String, Void, Void> {

        private String UID;

        @Override
        protected Void doInBackground(String... strings) {
            if (strings[0] != null)
                UID = strings[0];
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            returnProgress(UID, true);
        }
    }

}
