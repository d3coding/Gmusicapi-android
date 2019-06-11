package com.d3coding.gmusicapi;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.d3coding.gmusicapi.items.MusicItems;

import java.util.ArrayList;
import java.util.List;

public class Gmusicdb extends SQLiteOpenHelper {

    private static final String duration = "duration";

    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "MusicData.db";
    private static final String TABLE = "music";
    private static final String id = "id";
    private static final String uid = "uid";
    private static final String title = "title";
    private static final String artist = "artist";
    private static final String composer = "composer";
    private static final String album = "album";
    private static final String albumArtist = "albumArtist";
    private static final String year = "year";
    private static final String trackNumber = "trackNumber";
    private static final String genre = "genre";
    private static final String albumArtUrl = "albumArtUrl";
    private static final String estimatedSize = "estimatedSize";
    private static final String downloaded = "downloaded";
    private static final String albumId = "albumId";
    private static final String artistId = "artistId";
    private static final String comment = "comment";
    private static final String totalTrackCount = "totalTrackCount";
    private static final String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE + " ( " +
            "id              INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            "uid             TEXT    NOT NULL, " +
            "title           TEXT, " +
            "artist          TEXT, " +
            "composer        TEXT, " +
            "album           TEXT, " +
            "albumArtist     TEXT, " +
            "year            INTEGER, " +
            "trackNumber     INTEGER, " +
            "genre           TEXT, " +
            "albumArtUrl     TEXT, " +
            "estimatedSize   INTEGER, " +
            "duration            INTEGER, " +
            "albumId         TEXT, " +
            "artistId        TEXT, " +
            "comment         TEXT, " +
            "totalTrackCount INTEGER, " +
            "downloaded      INTEGER " +
            ");";

    long insertIfNotExists(Gmusicnet.Chunk chunk) {
        long ret = 0L;
        if (getCount(chunk.id) == 0) {
            ContentValues values = new ContentValues();
            SQLiteDatabase db = getWritableDatabase();
            values.put(uid, chunk.id);
            values.put(title, chunk.title);
            values.put(artist, chunk.artist);
            values.put(composer, chunk.composer);
            values.put(album, chunk.album);
            values.put(albumArtist, chunk.albumArtist);
            values.put(year, chunk.year);
            values.put(trackNumber, chunk.trackNumber);
            values.put(genre, chunk.genre);
            values.put(albumArtUrl, chunk.albumArtUrl);
            values.put(estimatedSize, chunk.estimatedSize);
            values.put(duration, chunk.duration);
            values.put(albumId, chunk.albumId);
            values.put(artistId, chunk.artistId);
            values.put(comment, chunk.comment);
            values.put(totalTrackCount, chunk.totalTrackCount);
            values.put(downloaded, chunk.downloaded);
            ret = db.insert(TABLE, null, values);
            db.close();
        }
        return ret;
    }

    private static final String SQL_DELETE_POSTS = "DROP TABLE IF EXISTS ";

    public Gmusicdb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_POSTS + TABLE);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private int getCount(String text) {
        Cursor c = null;
        SQLiteDatabase db = null;
        try {
            db = getReadableDatabase();
            String query = "select count(*) from " + TABLE + " where " + uid + " = ?";
            c = db.rawQuery(query, new String[]{text});
            if (c.moveToFirst())
                return c.getInt(0);
            else
                return 0;
        } finally {
            if (c != null)
                c.close();
            if (db != null)
                db.close();
        }
    }

    public List<MusicItems> getMusicItems(Sort sort, boolean desc, boolean onlyOffline) {
        List<MusicItems> ret = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {uid, albumArtUrl, title, artist, album, duration, downloaded};
        String order = null;
        String selection = null;
        if (sort == Sort.title)
            order = title;
        else if (sort == Sort.artist)
            order = artist;
        else if (sort == Sort.album)
            order = album;
        else if (sort == Sort.genre)
            order = genre;

        if (desc)
            order += " DESC";
        else
            order += " ASC";


        if (onlyOffline)
            selection = "downloaded = 1";


        Cursor cursor = db.query(TABLE, columns, selection, null, null, null, order);

        if (cursor != null && cursor.moveToFirst()) {
            do {

                ret.add(new MusicItems(cursor.getString(0), cursor.getString(1), cursor.getString(2),
                        cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInt(6) != 0));
            } while (cursor.moveToNext());
        }

        db.close();
        try {
            cursor.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public void updateDB(String uid) {
        ContentValues cv = new ContentValues();
        cv.put(downloaded, 1); //These Fields should be your String values of actual column names
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE, cv, Gmusicdb.uid + " = \"" + uid + "\"", null);
        try {
            db.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public enum Sort {
        title, artist, album, genre
    }

}
