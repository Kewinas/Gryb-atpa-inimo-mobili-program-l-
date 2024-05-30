package com.example.mushroomrecognitionapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class InfoActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MushroomAdapter adapter;
    private String[] classes = Constants.classes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        String mushroomType = getIntent().getStringExtra("mushroom_type");

        List<MushroomDetails> mushrooms = fetchMushroomDetails(mushroomType);

        String[] mushroomArray = new String[mushrooms.size()];
        for (int i = 0; i < mushrooms.size(); i++) {
            mushroomArray[i] = mushrooms.get(i).getName();
        }

        adapter = new MushroomAdapter(mushroomArray);
        recyclerView.setAdapter(adapter);
    }

    private List<MushroomDetails> fetchMushroomDetails(String mushroomType) {
        List<MushroomDetails> mushrooms = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase database = dbHelper.getReadableDatabase();

        try (Cursor cursor = database.rawQuery("SELECT name, scientific_name, description, type FROM Mushroom WHERE type = ?", new String[]{mushroomType})) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String scientificName = cursor.getString(cursor.getColumnIndex("scientific_name"));
                String description = cursor.getString(cursor.getColumnIndex("description"));
                String type = cursor.getString(cursor.getColumnIndex("type"));
                mushrooms.add(new MushroomDetails(name, scientificName, description, type));
            }
        } finally {
            dbHelper.close();
        }

        return mushrooms;
    }
}
