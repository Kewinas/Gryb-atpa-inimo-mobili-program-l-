package com.example.mushroomrecognitionapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

@RunWith(AndroidJUnit4.class)
public class DatabaseHelperTest {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        dbHelper = new DatabaseHelper(context);
        database = dbHelper.getWritableDatabase();
    }

    @After
    public void tearDown() {
        database.close();
        dbHelper.close();
    }

    @Test
    public void testDatabaseCreation() {
        assertNotNull(database);
        assertTrue(database.isOpen());
    }

    @Test
    public void testTableCreation() {
        Cursor cursor = database.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='Photo'", null);
        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());
        cursor.close();
    }

    @Test
    public void testInsertData() {
        byte[] photoData = new byte[]{1, 2, 3};
        String mushroomName = "Mushroom";
        double latitude = 10.0;
        double longitude = 20.0;
        String date = "2023-05-30";

        long id = dbHelper.insertPhoto(photoData, mushroomName, latitude, longitude, date);
        assertTrue(id != -1);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Photo WHERE id = ?", new String[]{String.valueOf(id)});
        assertNotNull(cursor);
        assertTrue(cursor.moveToFirst());

        String savedDate = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE));
        assertEquals(date, savedDate);

        assertEquals(mushroomName, cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_MUSHROOM_NAME)));
        assertEquals(latitude, cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_LATITUDE)), 0.0);
        assertEquals(longitude, cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_LONGITUDE)), 0.0);

        cursor.close();
    }

    @Test
    public void testQueryData() {
        byte[] photoData = new byte[]{1, 2, 3};
        String mushroomName = "Grybas";
        double latitude = 10.0;
        double longitude = 20.0;
        String date = "2023-05-30";

        dbHelper.insertPhoto(photoData, mushroomName, latitude, longitude, date);

        List<PhotoDetails> photos = dbHelper.getAllPhotos();
        assertEquals(1, photos.size());

        PhotoDetails photo = photos.get(0);
        assertEquals(mushroomName, photo.getMushroomName());
        assertEquals(latitude, photo.getLatitude(), 0.0);
        assertEquals(longitude, photo.getLongitude(), 0.0);
        assertEquals(date, photo.getDate());
    }

    @Test
    public void testDeleteData() {
        byte[] photoData = new byte[]{1, 2, 3};
        String mushroomName = "Mushroom";
        double latitude = 10.0;
        double longitude = 20.0;
        String date = "2023-05-30";

        long id = dbHelper.insertPhoto(photoData, mushroomName, latitude, longitude, date);
        dbHelper.deletePhoto(id);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM Photo WHERE id = ?", new String[]{String.valueOf(id)});
        assertNotNull(cursor);
        assertFalse(cursor.moveToFirst());
        cursor.close();
    }

}
