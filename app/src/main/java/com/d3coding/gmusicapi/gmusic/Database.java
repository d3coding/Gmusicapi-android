package com.d3coding.gmusicapi.gmusic;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.d3coding.gmusicapi.items.MusicItem;
import com.d3coding.gmusicapi.items.PlaylistItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Database extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "TrackMetadata.db";
    private static final String TABLE_TRACKS = "tracks";
    private static final String TABLE_DOWNLOAD = "downloads";
    private static final String TABLE_PLAYLIST = "play";
    private static final String TABLE_PLAYLIST_C = "play_c";
    private static final String SQL_CREATE_TABLE_DOWNLOAD = "CREATE TABLE " + TABLE_DOWNLOAD + " ( " +
            downloadsColumn.id.name() + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            downloadsColumn.uuid.name() + " TEXT NOT NULL, " +
            downloadsColumn.downloadTimestamp.name() + " INTEGER );";
    private static final int DATABASE_VERSION = 8;
    private static final String TYPE_TEXT = " TEXT, ";
    private static final String TYPE_INTEGER = " INTEGER, ";
    private static final String SQL_CREATE_TABLE_TRACKS = "CREATE TABLE " + TABLE_TRACKS + " ( " +
            column.id.name() + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            column.uuid.name() + " TEXT NOT NULL, " +
            column.title.name() + TYPE_TEXT +
            column.artist.name() + TYPE_TEXT +
            column.composer.name() + TYPE_TEXT +
            column.album.name() + TYPE_TEXT +
            column.albumArtist.name() + TYPE_TEXT +
            column.year.name() + TYPE_INTEGER +
            column.trackNumber.name() + TYPE_INTEGER +
            column.genre.name() + TYPE_TEXT +
            column.albumArtUrl.name() + TYPE_TEXT +
            column.estimatedSize.name() + TYPE_INTEGER +
            column.durationMillis.name() + TYPE_INTEGER +
            column.albumId.name() + TYPE_TEXT +
            column.artistId.name() + TYPE_TEXT +
            column.comment.name() + TYPE_TEXT +
            column.creationTimestamp.name() + TYPE_TEXT +
            column.totalTrackCount.name() + " INTEGER );";

    private static final String SQL_CREATE_TABLE_PLAYLIST_C = "CREATE TABLE " + TABLE_PLAYLIST_C + " ( " +
            column.id.name() + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "playlist_id" + TYPE_TEXT +
            "music_id" + " TEXT );";

    private static final String SQL_CREATE_TABLE_PLAYLIST = "CREATE TABLE " + TABLE_PLAYLIST + " ( " +
            column.id.name() + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            "playlist_id" + TYPE_TEXT +
            "playlist_name" + " TEXT );";

    private static final String SQL_DELETE_POSTS = "DROP TABLE IF EXISTS ";

    public Database(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_TRACKS);
        db.execSQL(SQL_CREATE_TABLE_DOWNLOAD);
        db.execSQL(SQL_CREATE_TABLE_PLAYLIST_C);
        db.execSQL(SQL_CREATE_TABLE_PLAYLIST);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO: updateDB
        db.execSQL(SQL_DELETE_POSTS + TABLE_TRACKS);
        db.execSQL(SQL_DELETE_POSTS + TABLE_DOWNLOAD);
        db.execSQL(SQL_DELETE_POSTS + TABLE_PLAYLIST);
        db.execSQL(SQL_DELETE_POSTS + TABLE_PLAYLIST_C);
        onCreate(db);
    }

    public void insertByTrackMetadata(List<TrackMetadata> trackMetadata) {
        SQLiteDatabase db = getWritableDatabase();
        for (TrackMetadata track : trackMetadata) {
            ContentValues values = new ContentValues();
            values.put(column.uuid.name(), track.uuid);
            values.put(column.title.name(), track.title);
            values.put(column.artist.name(), track.artist);
            values.put(column.composer.name(), track.composer);
            values.put(column.album.name(), track.album);
            values.put(column.albumArtist.name(), track.albumArtist);
            values.put(column.year.name(), track.year);
            values.put(column.trackNumber.name(), track.trackNumber);
            values.put(column.genre.name(), track.genre);
            values.put(column.albumArtUrl.name(), track.albumArtUrl);
            values.put(column.estimatedSize.name(), track.estimatedSize);
            values.put(column.durationMillis.name(), track.durationMillis);
            values.put(column.albumId.name(), track.albumId);
            values.put(column.artistId.name(), track.artistId);
            values.put(column.comment.name(), track.comment);
            values.put(column.totalTrackCount.name(), track.totalTrackCount);
            values.put(column.creationTimestamp.name(), track.creationTimestamp);

            db.insert(TABLE_TRACKS, null, values);
        }
        db.close();
    }

    public void insertPlaylist(String id, String title) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("playlist_id", id);
        values.put("playlist_name", title);
        db.insert(TABLE_PLAYLIST, null, values);
        db.close();
    }

    public void insertPlaylistContent(String playlist_id, String music_id) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("playlist_id", playlist_id);
        values.put("music_id", music_id);
        db.insert(TABLE_PLAYLIST_C, null, values);
        db.close();
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public TrackMetadata selectByUUID(String uuid) {
        TrackMetadata trackMetadata = null;

        String selection = column.uuid.name() + " = \"" + uuid + "\"";

        synchronized (this) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_TRACKS, null, selection, null, null, null, null);

            if (cursor != null && cursor.moveToFirst())
                trackMetadata = new TrackMetadata(cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(5),
                        cursor.getString(6),
                        cursor.getInt(7),
                        cursor.getInt(8),
                        cursor.getString(9),
                        cursor.getString(10),
                        cursor.getLong(11),
                        cursor.getLong(12),
                        cursor.getString(13),
                        cursor.getString(14),
                        cursor.getString(15),
                        cursor.getInt(16));
            try {
                db.close();
                cursor.close();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        return trackMetadata;
    }

    public String selectColumnByUUID(String uuid, column columnName) {
        String ret = "";
        String[] columns = {columnName.name()};
        String selection = column.uuid.name() + " = \"" + uuid + "\"";

        synchronized (this) {
            SQLiteDatabase db = this.getReadableDatabase();
            Cursor cursor = db.query(TABLE_TRACKS, columns, selection, null, null, null, null);

            if (cursor != null && cursor.moveToFirst())
                ret = cursor.getString(0);

            try {
                cursor.close();
                db.close();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        return ret;
    }

    public List<MusicItem> getMusicItems(int order, int sortOnline, String filterTitle, boolean desc, String extra) {
        List<MusicItem> ret = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {column.uuid.name(), column.title.name(), column.artist.name(),
                column.album.name(), column.durationMillis.name()};

        StringBuilder selection = new StringBuilder();

        int num = 5000;

        if (!filterTitle.equals("")) {
            if (filterTitle.startsWith("AR:"))
                selection.append(column.artist.name()).append(" LIKE \'%").append(filterTitle.replace("AR:", "")).append("%\'");
            else if (filterTitle.startsWith("AL:"))
                selection.append(column.album.name()).append(" LIKE \'%").append(filterTitle.replace("AL:", "")).append("%\'");
            else if (filterTitle.startsWith("GE:"))
                selection.append(column.genre.name()).append(" LIKE \'%").append(filterTitle.replace("GE:", "")).append("%\'");
            else if (filterTitle.startsWith("NU:")) {
                if (filterTitle.length() == 5)
                    try {
                        num = Integer.parseInt(String.valueOf(filterTitle.charAt(3)) + filterTitle.charAt(4));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
            } else
                selection.append(column.title.name()).append(" LIKE \'%").append(filterTitle).append("%\'");
        }

        if (sortOnline == 2) {
            if (!filterTitle.equals(""))
                selection.append(" AND ");
            selection.append(column.uuid.name()).append(" IN (SELECT ").append(downloadsColumn.uuid.name()).append(" FROM ").append(TABLE_DOWNLOAD).append(")");
        } else if (sortOnline == 1) {
            if (!filterTitle.equals(""))
                selection.append(" AND ");
            selection.append(column.uuid.name()).append(" NOT IN (SELECT ").append(downloadsColumn.uuid.name()).append(" FROM ").append(TABLE_DOWNLOAD).append(")");
        }

        String orderBy;
        if (order == 1)
            orderBy = column.artist.name();
        else if (order == 2)
            orderBy = column.album.name();
        else if (order == 3)
            orderBy = column.genre.name();
        else if (order == 4)
            orderBy = column.creationTimestamp.name();
        else
            orderBy = column.title.name();

        if (desc)
            orderBy += " DESC";
        else
            orderBy += " ASC";

        if (!extra.equals("")) {
            if (selection.length() > 0)
                selection.append(" AND ");
            selection.append(downloadsColumn.uuid.name()).append(" IN (SELECT ").append("music_id").append(" FROM ").append(TABLE_PLAYLIST_C).append(" WHERE playlist_id = \'").append(extra).append("\')");
            //selection.append(downloadsColumn.uuid.name()).append(" IN (SELECT ").append("music_id").append(" FROM ").append(TABLE_PLAYLIST_C).append(")");
        }

        Log.i("SELECTION:", selection.toString());

        Cursor cursor = db.query(TABLE_TRACKS, columns, selection.toString(), null, null, null, orderBy);

        if (cursor != null && cursor.moveToFirst()) {
            int x = 0;
            do {
                Long milliseconds = Long.parseLong(cursor.getString(4));
                ret.add(new MusicItem(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3),
                        String.format("%02d:%02d ", TimeUnit.MILLISECONDS.toMinutes(milliseconds), TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)))));
                ++x;
            } while (cursor.moveToNext() && x < num);
        }

        try {
            db.close();
            cursor.close();
        } catch (
                NullPointerException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public void insertUUIDbyDownloads(String uuid) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(downloadsColumn.uuid.name(), uuid);
        contentValues.put(downloadsColumn.downloadTimestamp.name(), System.currentTimeMillis());
        synchronized (this) {
            SQLiteDatabase db = getWritableDatabase();
            db.insert(TABLE_DOWNLOAD, null, contentValues);
            try {
                db.close();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

    }

    public int countDownloadsByUUID(String uuid) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT (*) FROM " + TABLE_DOWNLOAD + " WHERE " + downloadsColumn.uuid.name() + " = \'" + uuid + "\'", null);

        int ret = 0;
        if (cursor.moveToFirst())
            ret = cursor.getInt(0);
        try {
            cursor.close();
            db.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public List<PlaylistItem> getPlaylists() {

        List<PlaylistItem> ret = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {"playlist_name", "playlist_id"};

        Cursor cursor = db.query(TABLE_PLAYLIST, columns, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {

            do {
                ret.add(new PlaylistItem(cursor.getString(0), cursor.getString(1)));
            } while (cursor.moveToNext());
        }

        try {
            db.close();
            cursor.close();
        } catch (
                NullPointerException e) {
            e.printStackTrace();
        }

        return ret;
    }


    public enum column {
        id, uuid, title, artist, composer, album, albumArtist, year, trackNumber, genre, durationMillis, albumArtUrl, discNumber, estimatedSize, trackType, albumId,
        artistId, explicitType, playCount, rating, beatsPerMinute, comment, totalTrackCount, totalDiscCount, lastModifiedTimestamp, creationTimestamp, recentTimestamp
    }

    enum downloadsColumn {
        id, uuid, downloadTimestamp
    }

    public static class TrackMetadata {
        public String uuid, title, artist, composer, album, albumArtist;
        public int year, trackNumber;
        public String genre, albumArtUrl;
        public int discNumber;
        public Long estimatedSize, durationMillis;
        public String trackType, albumId, artistId, explicitType;
        public int playCount;
        public String rating;
        public int beatsPerMinute;
        public String comment;
        public int totalTrackCount, totalDiscCount;
        public String lastModifiedTimestamp, creationTimestamp, recentTimestamp;

        TrackMetadata() {
        }

        TrackMetadata(String uuid, String title, String artist, String composer, String album, String albumArtist, int year, int trackNumber,
                      String genre, String albumArtUrl, Long estimatedSize, Long durationMillis, String albumId, String artistId, String comment, int totalTrackCount) {

            this.uuid = uuid;
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
            this.durationMillis = durationMillis;
            this.albumId = albumId;
            this.artistId = artistId;
            this.comment = comment;
            this.totalTrackCount = totalTrackCount;
        }
    }

}
