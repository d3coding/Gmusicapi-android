package com.d3coding.gmusicapi;

import android.content.Context;
import android.os.AsyncTask;

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
import svarzee.gps.gpsoauth.Gpsoauth;

public class Gmusicnet extends AsyncTask<String, Void, Void> {

    private List<Chunk> chunkList;
    private Context context;

    Gmusicnet(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... strings) {
        try {

            System.out.println("#010");
            AuthToken authToken;

            if (strings.length > 0)
                authToken = TokenProvider.provideToken(strings[0]);
            else
                throw new Exception("Argumnent");

            System.out.println("#001");
            GPlayMusic api = new GPlayMusic.Builder().setAuthToken(authToken).build();
            System.out.println("#002");
            TrackApi trackApi = api.getTrackApi();
            System.out.println("#003");
            List<Track> listTrack = trackApi.getLibraryTracks();
            System.out.println("#004");
            System.out.println("# Size: " + listTrack.size());

            chunkList = new ArrayList<>();

            for (int x = 0; x < listTrack.size(); ++x) {
                String id, title, artist, composer, album, albumArtist, genre, albumArtUrl, albumId, artistId, comment;
                int year, trackNumber, totalTrackCount;
                Long estimatedSize, duration;
                // id
                id = listTrack.get(x).getID();
                // title
                title = listTrack.get(x).getTitle();
                // artist
                artist = listTrack.get(x).getArtist();
                // composer
                composer = listTrack.get(x).getComposer();
                // album
                album = listTrack.get(x).getAlbum();
                // albumArtist
                albumArtist = listTrack.get(x).getAlbumArtist();
                // year
                OptionalInt optionalInt = listTrack.get(x).getYear();
                if (optionalInt.isPresent())
                    year = optionalInt.getAsInt();
                else
                    year = 0;
                // trackNumber
                trackNumber = listTrack.get(x).getTrackNumber();
                // genre
                Optional<String> optionalGenre = listTrack.get(x).getGenre();
                if (optionalGenre.isPresent())
                    genre = optionalGenre.get();
                else
                    genre = "";
                // albumArtUrl
                Optional<List<ArtRef>> optionalArtRefs = listTrack.get(x).getAlbumArtRef();
                if (optionalArtRefs.isPresent())
                    albumArtUrl = optionalArtRefs.get().get(0).getUrl();
                else
                    albumArtUrl = "";
                // estimatedSize
                estimatedSize = listTrack.get(x).getEstimatedSize();
                // albumId
                albumId = listTrack.get(x).getAlbumId();
                // artistId
                Optional<List<String>> optionalArtistId = listTrack.get(x).getArtistId();
                if (optionalArtistId.isPresent()) {
                    StringBuilder stringBuilder = new StringBuilder(optionalArtistId.get().get(0));
                    for (int y = 1; y < optionalArtistId.get().size(); ++y)
                        stringBuilder.append(optionalArtistId.get().get(y));
                    artistId = stringBuilder.toString();

                } else
                    artistId = "";
                // comment
                Optional<String> optionalComment = listTrack.get(x).getComment();
                if (optionalComment.isPresent())
                    comment = optionalComment.get();
                else
                    comment = "";
                // duration
                duration = listTrack.get(x).getDurationMillis();
                // totalTrackCount
                OptionalInt optionalTotalTrackCount = listTrack.get(x).getTotalTrackCount();
                if (optionalTotalTrackCount.isPresent())
                    totalTrackCount = optionalTotalTrackCount.getAsInt();
                else
                    totalTrackCount = 0;

                chunkList.add(new Chunk(id, title, artist, composer, album, albumArtist, year, trackNumber,
                        genre, albumArtUrl, estimatedSize, duration, albumId, artistId, comment, totalTrackCount, 0));
            }

            System.out.println("#005");

            Gmusicdb gmusicdb = new Gmusicdb(context);
            for (int x = 0; x < chunkList.size(); ++x)
                gmusicdb.insertIfNotExists(chunkList.get(x));

            System.out.println("#006");
        } catch (IOException e) {
            System.out.println("#100");
            e.printStackTrace();
        } catch (Gpsoauth.TokenRequestFailed e) {
            System.out.println("#666");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        context.getSharedPreferences(context.getString(R.string.preferences_user), Context.MODE_PRIVATE).edit()
                .putLong(context.getString(R.string.last_update), TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())).apply();

        return null;
    }

    class Chunk {

        String id, title, artist, composer, album, albumArtist;
        int year, trackNumber;
        String genre, albumArtUrl;
        Long estimatedSize, duration;
        String albumId, artistId, comment;
        int totalTrackCount, downloaded;

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