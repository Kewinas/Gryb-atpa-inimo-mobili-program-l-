package com.example.mushroomrecognitionapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class TypeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_type);

        ImageButton imageButton1 = findViewById(R.id.btn_valgomi);
        ImageButton imageButton2 = findViewById(R.id.btn_menkaverciai);
        ImageButton imageButton3 = findViewById(R.id.btn_nevalgomi);
        ImageButton imageButton4 = findViewById(R.id.btn_nuodingi);

        imageButton1.setOnClickListener(v -> goToInfoActivity("Valgomas"));
        imageButton2.setOnClickListener(v -> goToInfoActivity("Menkavertis"));
        imageButton3.setOnClickListener(v -> goToInfoActivity("Nevalgomas"));
        imageButton4.setOnClickListener(v -> goToInfoActivity("Nuodingas"));
    }

    private void goToInfoActivity(String type) {
        Intent intent = new Intent(TypeActivity.this, InfoActivity.class);
        intent.putExtra("mushroom_type", type);
        startActivity(intent);
    }
}
