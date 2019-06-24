package com.d3coding.gmusicapi;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.github.felixgail.gplaymusic.api.GPlayMusic;
import com.github.felixgail.gplaymusic.api.TrackApi;
import com.github.felixgail.gplaymusic.model.Track;
import com.github.felixgail.gplaymusic.model.snippets.ArtRef;
import com.github.felixgail.gplaymusic.util.TokenProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.TimeUnit;

import svarzee.gps.gpsoauth.AuthToken;

public class Gmusicnet extends AsyncTask<String, Void, Void> {

    private List<Chunk> chunkList;
    private Context context;

    Gmusicnet(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... strings) {
        try {
            synchronized (this) {
                ((Activity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "Updating database...", Toast.LENGTH_SHORT).show();
                });
            }
            AuthToken authToken;

            if (strings.length > 0)
                authToken = TokenProvider.provideToken(strings[0]);
            else
                throw new Exception("Invalid token");

            GPlayMusic api = new GPlayMusic.Builder().setAuthToken(authToken).build();
            TrackApi trackApi = api.getTrackApi();
            List<Track> listTrack = trackApi.getLibraryTracks();

            System.out.println("Database size: " + listTrack.size());


            chunkList = new ArrayList<>();

            for (int x = 0; x < listTrack.size(); ++x) {
                Chunk chunk = new Chunk();
                chunk.id = listTrack.get(x).getID();
                chunk.title = listTrack.get(x).getTitle();
                chunk.artist = listTrack.get(x).getArtist();
                chunk.composer = listTrack.get(x).getComposer();
                chunk.album = listTrack.get(x).getAlbum();
                chunk.albumArtist = listTrack.get(x).getAlbumArtist();

                // year
                OptionalInt optionalInt = listTrack.get(x).getYear();
                if (optionalInt.isPresent())
                    chunk.year = optionalInt.getAsInt();
                else
                    chunk.year = 0;

                chunk.trackNumber = listTrack.get(x).getTrackNumber();

                // genre
                Optional<String> optionalGenre = listTrack.get(x).getGenre();
                if (optionalGenre.isPresent())
                    chunk.genre = optionalGenre.get();
                else
                    chunk.genre = "";

                // albumArtUrl
                Optional<List<ArtRef>> optionalArtRefs = listTrack.get(x).getAlbumArtRef();
                if (optionalArtRefs.isPresent())
                    chunk.albumArtUrl = optionalArtRefs.get().get(0).getUrl();
                else
                    chunk.albumArtUrl = "";

                chunk.estimatedSize = listTrack.get(x).getEstimatedSize();
                chunk.albumId = listTrack.get(x).getAlbumId();
                // artistId
                Optional<List<String>> optionalArtistId = listTrack.get(x).getArtistId();
                if (optionalArtistId.isPresent()) {
                    StringBuilder stringBuilder = new StringBuilder(optionalArtistId.get().get(0));
                    for (int y = 1; y < optionalArtistId.get().size(); ++y)
                        stringBuilder.append(optionalArtistId.get().get(y));
                    chunk.artistId = stringBuilder.toString();
                } else
                    chunk.artistId = "";

                // comment
                Optional<String> optionalComment = listTrack.get(x).getComment();
                if (optionalComment.isPresent())
                    chunk.comment = optionalComment.get();
                else
                    chunk.comment = "";

                chunk.duration = listTrack.get(x).getDurationMillis();

                // totalTrackCount
                OptionalInt optionalTotalTrackCount = listTrack.get(x).getTotalTrackCount();
                if (optionalTotalTrackCount.isPresent())
                    chunk.totalTrackCount = optionalTotalTrackCount.getAsInt();
                else
                    chunk.totalTrackCount = 0;

                chunkList.add(chunk);
            }

            new Gmusicdb(context).insertIfNotExists(chunkList);

            synchronized (this) {
                ((Activity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "Database info download complete...", Toast.LENGTH_LONG).show();
                });
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        context.getSharedPreferences(context.getString(R.string.preferences_user), Context.MODE_PRIVATE).edit()
                .putLong(context.getString(R.string.last_update), TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())).apply();

        return null;
    }

    public static class Chunk {

        public String id, title, artist, composer, album, albumArtist;
        public int year, trackNumber;
        public String genre, albumArtUrl;
        public Long estimatedSize, duration;
        public String albumId, artistId, comment;
        public int totalTrackCount, downloaded;

        Chunk() {
        }

        Chunk(String id, String title, String artist, String composer, String album, String albumArtist, int year, int trackNumber,
              String genre, String albumArtUrl, Long estimatedSize, Long duration, String albumId, String artistId, String comment, int totalTrackCount, int downloaded) {

            this.id = id;
            this.title = title;
            this.artist = artist;
            this.composer = composer;
            this.album = album;
            this.albumArtist = albumArtist;
            this.year = year;
            this.trackNumber = trackNumber;
            this.genre = genre;
            this.albumArtUrl = albumArtUrl;
            this.estimatedSize = estimatedSize;
            this.duration = duration;
            this.albumId = albumId;
            this.artistId = artistId;
            this.comment = comment;
            this.totalTrackCount = totalTrackCount;
            this.downloaded = downloaded;

        }

    }

}