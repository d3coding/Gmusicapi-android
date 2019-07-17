package com.d3coding.gmusicapi.gmusic;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.d3coding.gmusicapi.R;
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

public class Network extends AsyncTask<String, Void, Void> {

    private Context context;

    public Network(Context context) {
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... strings) {
        try {
            synchronized (this) {
                ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "Updating database...", Toast.LENGTH_SHORT).show());
            }

            AuthToken authToken;

            if (strings.length > 0)
                authToken = TokenProvider.provideToken(strings[0]);
            else
                throw new NullPointerException("Null token");

            GPlayMusic api = new GPlayMusic.Builder().setAuthToken(authToken).build();
            TrackApi trackApi = api.getTrackApi();
            List<Track> listTrack = trackApi.getLibraryTracks();

            System.out.println("Database size: " + listTrack.size());

            List<Database.TrackMetadata> chunkList = new ArrayList<>();

            for (Track track : listTrack) {
                Database.TrackMetadata trackMetadata = new Database.TrackMetadata();

                trackMetadata.uuid = track.getID();
                trackMetadata.title = track.getTitle();
                trackMetadata.artist = track.getArtist();
                trackMetadata.composer = track.getComposer();
                trackMetadata.album = track.getAlbum();
                trackMetadata.albumArtist = track.getAlbumArtist();

                // year
                OptionalInt optionalInt = track.getYear();
                if (optionalInt.isPresent())
                    trackMetadata.year = optionalInt.getAsInt();
                else
                    trackMetadata.year = 0;

                trackMetadata.trackNumber = track.getTrackNumber();

                // genre
                Optional<String> optionalGenre = track.getGenre();
                if (optionalGenre.isPresent())
                    trackMetadata.genre = optionalGenre.get();
                else
                    trackMetadata.genre = "";

                // albumArtUrl
                Optional<List<ArtRef>> optionalArtRefs = track.getAlbumArtRef();
                if (optionalArtRefs.isPresent())
                    trackMetadata.albumArtUrl = optionalArtRefs.get().get(0).getUrl();
                else
                    trackMetadata.albumArtUrl = "";

                trackMetadata.estimatedSize = track.getEstimatedSize();
                trackMetadata.albumId = track.getAlbumId();
                // artistId
                Optional<List<String>> optionalArtistId = track.getArtistId();
                if (optionalArtistId.isPresent()) {
                    StringBuilder stringBuilder = new StringBuilder(optionalArtistId.get().get(0));
                    for (int y = 1; y < optionalArtistId.get().size(); ++y)
                        stringBuilder.append(optionalArtistId.get().get(y));
                    trackMetadata.artistId = stringBuilder.toString();
                } else
                    trackMetadata.artistId = "";

                // comment
                Optional<String> optionalComment = track.getComment();
                if (optionalComment.isPresent())
                    trackMetadata.comment = optionalComment.get();
                else
                    trackMetadata.comment = "";

                trackMetadata.durationMillis = track.getDurationMillis();

                // totalTrackCount
                OptionalInt optionalTotalTrackCount = track.getTotalTrackCount();
                if (optionalTotalTrackCount.isPresent())
                    trackMetadata.totalTrackCount = optionalTotalTrackCount.getAsInt();
                else
                    trackMetadata.totalTrackCount = 0;

                chunkList.add(trackMetadata);
            }

            new Database(context).insertByTrackMetadata(chunkList);

            synchronized (this) {
                ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "Database info download complete...", Toast.LENGTH_LONG).show());
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        context.getSharedPreferences(context.getString(R.string.preferences_user), Context.MODE_PRIVATE).edit()
                .putLong(context.getString(R.string.last_update), TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis())).apply();

        return null;
    }


}