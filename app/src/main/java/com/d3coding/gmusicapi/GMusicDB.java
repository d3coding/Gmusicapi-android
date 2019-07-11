package com.d3coding.gmusicapi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.d3coding.gmusicapi.items.MusicItem;

import java.util.ArrayList;
import java.util.List;

public class GMusicDB extends SQLiteOpenHelper {

    static final String DATABASE_NAME = "TrackMetadata.db";
    private static final String TABLE_TRACKS = "tracks";
    private static final String TABLE_DOWNLOAD = "downloads";
    private static final String SQL_CREATE_TABLE_DOWNLOAD = "CREATE TABLE " + TABLE_DOWNLOAD + " ( " +
            downloadsColumn.id.name() + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            downloadsColumn.uuid.name() + " TEXT NOT NULL, " +
            downloadsColumn.downloadTimestamp.name() + " INTEGER );";
    private static final int DATABASE_VERSION = 6;
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
            column.totalTrackCount.name() + " INTEGER );";
    private static final String SQL_DELETE_POSTS = "DROP TABLE IF EXISTS ";

    public GMusicDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    static int getDatabaseVersion() {
        return DATABASE_VERSION;
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_TRACKS);
        db.execSQL(SQL_CREATE_TABLE_DOWNLOAD);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_POSTS + TABLE_TRACKS);
        db.execSQL(SQL_DELETE_POSTS + TABLE_DOWNLOAD);
        onCreate(db);
    }

    void insertByTrackMetadata(List<TrackMetadata> trackMetadata) {
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

            db.insert(TABLE_TRACKS, null, values);
        }
        db.close();
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    TrackMetadata selectByUUID(String uuid) {
        TrackMetadata trackMetadata = null;

        SQLiteDatabase db = this.getReadableDatabase();
        String selection = column.uuid.name() + " = \"" + uuid + "\"";

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

        return trackMetadata;
    }

    String selectColumnByUUID(String uuid, column columnName) {
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

    List<MusicItem> getMusicItems(int order, int sortOnline, String filterTitle, boolean desc) {
        List<MusicItem> ret = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {column.uuid.name(), column.title.name(), column.artist.name(),
                column.album.name(), column.durationMillis.name()};

        StringBuilder selection = new StringBuilder();

        if (!filterTitle.equals(""))
            selection.append(column.title.name()).append(" LIKE \'%").append(filterTitle).append("%\'");

        if (sortOnline == 2) {
            if (!filterTitle.equals(""))
                selection.append(" AND ");
            selection.append(column.uuid.name()).append(" IN (SELECT ").append(downloadsColumn.uuid.name()).append(" FROM ").append(TABLE_DOWNLOAD).append(")");
        } else if (sortOnline == 1) {
            if (!filterTitle.equals(""))
                selection.append(" AND ");
            selection.append(column.uuid.name()).append(" NOT IN (SELECT ").append(downloadsColumn.uuid.name()).append(" FROM ").append(TABLE_DOWNLOAD).append(")");
        }

        String orderBy = null;
        if (order == 1)
            orderBy = column.artist.name();
        else if (order == 2)
            orderBy = column.artist.name();
        else if (order == 3)
            orderBy = column.artist.name();
        else
            orderBy = column.artist.name();

        if (desc)
            orderBy += " DESC";
        else
            orderBy += " ASC";

        Cursor cursor = db.query(TABLE_TRACKS, columns, selection.toString(), null, null, null, orderBy);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                ret.add(new MusicItem(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                        cursor.getString(3), cursor.getString(4)));
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

    void insertUUIDbyDownloads(String uuid) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(downloadsColumn.uuid.name(), uuid);
        contentValues.put(downloadsColumn.downloadTimestamp.name(), System.currentTimeMillis());

        SQLiteDatabase db = getWritableDatabase();
        db.insert(TABLE_DOWNLOAD, null, contentValues);
        try {
            db.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
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


    enum column {
        id, uuid, title, artist, composer, album, albumArtist, year, trackNumber, genre, durationMillis, albumArtUrl, discNumber, estimatedSize, trackType, albumId,
        artistId, explicitType, playCount, rating, beatsPerMinute, comment, totalTrackCount, totalDiscCount, lastModifiedTimestamp, creationTimestamp, recentTimestamp
    }

    enum downloadsColumn {
        id, uuid, downloadTimestamp
    }

    static class TrackMetadata {
        String uuid, title, artist, composer, album, albumArtist;
        int year, trackNumber;
        String genre, albumArtUrl;
        int discNumber;
        Long estimatedSize, durationMillis;
        String trackType, albumId, artistId, explicitType;
        int playCount;
        String rating;
        int beatsPerMinute;
        String comment;
        int totalTrackCount, totalDiscCount;
        String lastModifiedTimestamp, creationTimestamp, recentTimestamp;

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

    public enum SortOnline {
        all, offline, online
    }

}
