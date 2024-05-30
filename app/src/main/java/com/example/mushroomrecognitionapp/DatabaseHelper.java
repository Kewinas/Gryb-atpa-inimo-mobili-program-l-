package com.example.mushroomrecognitionapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "mushroom_recognition.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_PHOTO = "Photo";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_PHOTO_DATA = "photo_data";
    public static final String COLUMN_MUSHROOM_NAME = "mushroom_name";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_DATE = "date";
    private final Context mContext;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        if (!doesTableExist(db, TABLE_PHOTO)) {
            String createPhotoTableQuery = "CREATE TABLE " + TABLE_PHOTO + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY," +
                    COLUMN_PHOTO_DATA + " BLOB NOT NULL," +
                    COLUMN_MUSHROOM_NAME + " TEXT," +
                    COLUMN_LATITUDE + " REAL NOT NULL," +
                    COLUMN_LONGITUDE + " REAL NOT NULL," +
                    COLUMN_DATE + " TEXT NOT NULL)";
            db.execSQL(createPhotoTableQuery);
        }
    }

    private boolean doesTableExist(SQLiteDatabase db, String tableName) {
        try (Cursor cursor = db.rawQuery("SELECT count(*) FROM sqlite_master WHERE type = ? AND name = ?", new String[]{"table", tableName})) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getInt(0) > 0;
            }
        }
        return false;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTO);
        onCreate(db);
    }

    public List<PhotoDetails> getAllPhotos() {
        List<PhotoDetails> photos = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try (Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PHOTO, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                    byte[] photoData = cursor.getBlob(cursor.getColumnIndex(COLUMN_PHOTO_DATA));
                    String mushroomName = cursor.getString(cursor.getColumnIndex(COLUMN_MUSHROOM_NAME));
                    double latitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LATITUDE));
                    double longitude = cursor.getDouble(cursor.getColumnIndex(COLUMN_LONGITUDE));
                    String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                    photos.add(new PhotoDetails(id, photoData, mushroomName, latitude, longitude, date));
                } while (cursor.moveToNext());
            }
        } finally {
            db.close();
        }

        return photos;
    }

    public long insertPhoto(byte[] photoData, String mushroomName, double latitude, double longitude, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_PHOTO_DATA, photoData);
        values.put(COLUMN_MUSHROOM_NAME, mushroomName);
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);
        values.put(COLUMN_DATE, date);

        long id = db.insert(TABLE_PHOTO, null, values);
        db.close();

        return id;
    }

    public void deletePhoto(long photoId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PHOTO, COLUMN_ID + "=?", new String[]{String.valueOf(photoId)});
        db.close();
    }

    public Cursor getAllPhotosCursor() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_MUSHROOM_NAME, COLUMN_LATITUDE, COLUMN_LONGITUDE, COLUMN_DATE};
        return db.query(TABLE_PHOTO, columns, null, null, null, null, null);
    }

    public long getPhotoId(String mushroomName, double latitude, double longitude) {
        long photoId = -1;
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT " + COLUMN_ID + " FROM " + TABLE_PHOTO + " WHERE " +
                COLUMN_MUSHROOM_NAME + " = ? AND " +
                COLUMN_LATITUDE + " = ? AND " +
                COLUMN_LONGITUDE + " = ?";
        try (Cursor cursor = db.rawQuery(selectQuery, new String[]{mushroomName, String.valueOf(latitude), String.valueOf(longitude)})) {
            if (cursor != null && cursor.moveToFirst()) {
                photoId = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
            }
        }
        return photoId;
    }

    public void copyDatabaseFromAssets() {
        if (!checkDatabaseExists()) {
            try {
                InputStream inputStream = mContext.getAssets().open(DATABASE_NAME);
                String outFileName = mContext.getDatabasePath(DATABASE_NAME).getPath();

                OutputStream outputStream = new FileOutputStream(outFileName);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }

                outputStream.flush();
                outputStream.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkDatabaseExists() {
        return mContext.getDatabasePath(DATABASE_NAME).exists();
    }
}
