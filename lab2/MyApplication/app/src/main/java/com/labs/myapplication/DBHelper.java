package com.labs.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "markerDb";

    public static final String TABLE_MARKER = "marker";
    public static final String TABLE_PHOTO = "photo";

    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";

    public static final String KEY_ID_MARKER = "id_marker";
    public static final String KEY_URI = "uri";

    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_MARKER + "( "
                + KEY_ID + " integer primary key, "
                + KEY_NAME + " text, "
                + KEY_LATITUDE + " real, "
                + KEY_LONGITUDE + " real)"
        );

        db.execSQL("CREATE TABLE " + TABLE_PHOTO + "( "
                + KEY_ID + " integer primary key, "
                + KEY_ID_MARKER + " integer, "
                + KEY_URI + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKER);

        onCreate(db);
    }
}
