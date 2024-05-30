package com.example.mushroomrecognitionapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        databaseHelper = new DatabaseHelper(this);
        databaseHelper.copyDatabaseFromAssets();

        setupButton(R.id.btn_classification, ClassificationActivity.class);
        setupButton(R.id.btn_info, TypeActivity.class);
        setupButton(R.id.btn_my_mushrooms, MyMushroomsActivity.class);
    }

    private void setupButton(int buttonId, final Class<?> activityClass) {
        ImageButton button = findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, activityClass));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}
