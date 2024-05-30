package com.example.mushroomrecognitionapp;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        String mushroomType = getIntent().getStringExtra("mushroomClass");
        setTitle(mushroomType);

        ImageView imageView1 = findViewById(R.id.imageView1);
        ImageView imageView2 = findViewById(R.id.imageView2);
        String mushroomTypeNormalized = normalizeString(mushroomType);
        String mushroomTypeNormalizedImage = mushroomTypeNormalized.split("\n")[0].trim().replace(" ", "_");

        imageView1.setImageResource(getResources().getIdentifier(mushroomTypeNormalizedImage + "1", "drawable", getPackageName()));
        imageView2.setImageResource(getResources().getIdentifier(mushroomTypeNormalizedImage + "2", "drawable", getPackageName()));

        MushroomDetails mushroomDetails = getMushroomDetails(mushroomType);

        TextView nameTextView = findViewById(R.id.nameTextView);
        TextView scientificNameTextView = findViewById(R.id.scientificNameTextView);
        TextView descriptionTextView = findViewById(R.id.descriptionTextView);
        TextView typeTextView = findViewById(R.id.typeTextView);

        nameTextView.setText(mushroomDetails.getName());
        scientificNameTextView.setText(mushroomDetails.getScientificName());
        descriptionTextView.setText(mushroomDetails.getDescription());
        typeTextView.setText(mushroomDetails.getType());

        setTypeTextViewColor(typeTextView, mushroomDetails.getType());
    }

    private void setTypeTextViewColor(TextView textView, String mushroomType) {
        switch (mushroomType) {
            case "Valgomas":
                textView.setTextColor(Color.GREEN);
                break;
            case "Menkavertis":
                textView.setTextColor(Color.YELLOW);
                break;
            case "Nevalgomas":
                textView.setTextColor(Color.BLUE);
                break;
            case "Nuodingas":
                textView.setTextColor(Color.RED);
                break;
        }
    }

    private MushroomDetails getMushroomDetails(String mushroomType) {
        MushroomDetails mushroomDetails = null;
        SQLiteDatabase db = SQLiteDatabase.openDatabase(getDatabasePath("mushroom_recognition.db").getPath(), null, SQLiteDatabase.OPEN_READONLY);

        try (Cursor cursor = db.rawQuery("SELECT name, scientific_name, description, type FROM Mushroom WHERE name = ?", new String[]{mushroomType})) {
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndex("name"));
                String scientificName = cursor.getString(cursor.getColumnIndex("scientific_name"));
                String description = cursor.getString(cursor.getColumnIndex("description"));
                String type = cursor.getString(cursor.getColumnIndex("type"));

                mushroomDetails = new MushroomDetails(name, scientificName, description, type);
            }
        }

        db.close();
        return mushroomDetails;
    }

    public String normalizeString(String input) {
        return input.replaceAll("ė", "e")
                .replaceAll("š", "s")
                .replaceAll("ž", "z")
                .replaceAll("ū", "u")
                .replaceAll("Š", "S")
                .replaceAll("Ž", "Z")
                .replaceAll("č", "c")
                .toLowerCase();
    }
}
