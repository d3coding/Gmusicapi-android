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

    private static final String SQL_DELETE_POSTS = "DROP TABLE IF EXISTS ";

    public GMusicDB(Context context) {
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

    void insertIfNotExists(List<GMusicNet.Chunk> chunk) {
        SQLiteDatabase db = getWritableDatabase();
        for (int x = 0; x < chunk.size(); ++x) {
            ContentValues values = new ContentValues();
            values.put(uid, chunk.get(x).id);
            values.put(title, chunk.get(x).title);
            values.put(artist, chunk.get(x).artist);
            values.put(composer, chunk.get(x).composer);
            values.put(album, chunk.get(x).album);
            values.put(albumArtist, chunk.get(x).albumArtist);
            values.put(year, chunk.get(x).year);
            values.put(trackNumber, chunk.get(x).trackNumber);
            values.put(genre, chunk.get(x).genre);
            values.put(albumArtUrl, chunk.get(x).albumArtUrl);
            values.put(estimatedSize, chunk.get(x).estimatedSize);
            values.put(duration, chunk.get(x).duration);
            values.put(albumId, chunk.get(x).albumId);
            values.put(artistId, chunk.get(x).artistId);
            values.put(comment, chunk.get(x).comment);
            values.put(totalTrackCount, chunk.get(x).totalTrackCount);
            values.put(downloaded, chunk.get(x).downloaded);
            db.insert(TABLE, null, values);
        }
        db.close();
    }

    public GMusicNet.Chunk selectByUID(String uid) {
        GMusicNet.Chunk chunk = null;

        SQLiteDatabase db = this.getReadableDatabase();
        String selection = GMusicDB.uid + " = \"" + uid + "\"";

        Cursor cursor = db.query(TABLE, null, selection, null, null, null, null);

        if (cursor != null && cursor.moveToFirst())
            chunk = new GMusicNet.Chunk(cursor.getString(1),
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
                    cursor.getInt(16),
                    cursor.getInt(17));

        try {
            db.close();
            cursor.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }


        return chunk;
    }

    public List<MusicItem> getMusicItems(Sort sort, boolean desc, boolean onlyOffline) {
        List<MusicItem> ret = new ArrayList<>();
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

                ret.add(new MusicItem(cursor.getString(0), cursor.getString(1), cursor.getString(2),
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

    public String getThumbURL(String uuid) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {albumArtUrl};
        String selection = uid + " = \"" + uuid + "\"";

        Cursor cursor = db.query(TABLE, columns, selection, null, null, null, null);

        String ret = "";
        if (cursor != null && cursor.moveToFirst())
            ret = cursor.getString(0);

        db.close();
        try {
            cursor.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return ret;
    }

    public void updateDB(String uuid, int value) {
        ContentValues cv = new ContentValues();
        cv.put(downloaded, value);
        SQLiteDatabase db = getWritableDatabase();
        db.update(TABLE, cv, uid + " = \"" + uuid + "\"", null);
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
