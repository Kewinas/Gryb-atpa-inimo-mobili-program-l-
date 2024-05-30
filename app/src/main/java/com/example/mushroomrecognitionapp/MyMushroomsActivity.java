package com.example.mushroomrecognitionapp;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyMushroomsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private GoogleMap mMap;
    private DatabaseHelper dbHelper;
    private Map<Marker, Long> markerIdMap;
    private TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_mushrooms);

        dbHelper = new DatabaseHelper(this);
        markerIdMap = new HashMap<>();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        tableLayout = findViewById(R.id.tableLayout);
        addLegendRow();
        populateTable();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else {
            enableMyLocation();
        }
    }

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            addMushroomMarkers();
        }
    }

    private void addMushroomMarkers() {
        List<PhotoDetails> photoDetailsList = dbHelper.getAllPhotos();
        for (PhotoDetails photoDetails : photoDetailsList) {
            LatLng location = new LatLng(photoDetails.getLatitude(), photoDetails.getLongitude());
            String markerTitle = photoDetails.getMushroomName() + " Data: " + photoDetails.getDate();
            MarkerOptions markerOptions = new MarkerOptions().position(location).title(markerTitle);
            Marker marker = mMap.addMarker(markerOptions);
            markerIdMap.put(marker, photoDetails.getId());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        } else {
            Toast.makeText(this, "Vietos nustatymas negalimas", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        Toast.makeText(this, "Grybo rūšis: " + marker.getTitle(), Toast.LENGTH_LONG).show();
        return true;
    }

    private void populateTable() {
        Cursor cursor = dbHelper.getAllPhotosCursor();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String mushroomName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_MUSHROOM_NAME));
                String date = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_DATE));
                double latitude = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_LATITUDE));
                double longitude = cursor.getDouble(cursor.getColumnIndex(DatabaseHelper.COLUMN_LONGITUDE));
                addTableRow(mushroomName, date, latitude, longitude);
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private void addTableRow(String mushroomName, String date, double latitude, double longitude) {
        TableRow row = createTableRow();
        addMushroomNameCell(row, mushroomName);
        addVerticalBorder(row);
        addDateCell(row, date);

        row.setOnClickListener(view -> zoomToLocation(latitude, longitude));
        row.setOnLongClickListener(view -> showDeleteConfirmationDialog(mushroomName, latitude, longitude));

        tableLayout.addView(row);
    }

    private TableRow createTableRow() {
        TableRow row = new TableRow(this);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, 10);
        row.setLayoutParams(layoutParams);
        row.setBackgroundResource(R.drawable.cell_border);
        return row;
    }

    private void addMushroomNameCell(TableRow row, String mushroomName) {
        TextView textView = new TextView(this);
        textView.setText(mushroomName);
        textView.setTextColor(Color.WHITE);
        textView.setPadding(10, 20, 10, 20);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2));
        row.addView(textView);
    }

    private void addVerticalBorder(TableRow row) {
        View verticalBorder = new View(this);
        verticalBorder.setLayoutParams(new TableRow.LayoutParams(2, TableRow.LayoutParams.MATCH_PARENT));
        verticalBorder.setBackgroundColor(Color.parseColor("#FF6A3B"));
        row.addView(verticalBorder);
    }

    private void addDateCell(TableRow row, String date) {
        TextView textView = new TextView(this);
        textView.setText(date);
        textView.setTextColor(Color.WHITE);
        textView.setPadding(10, 10, 10, 10);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2));
        row.addView(textView);
    }

    private void zoomToLocation(double latitude, double longitude) {
        LatLng mushroomLocation = new LatLng(latitude, longitude);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(mushroomLocation)
                .zoom(12)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private boolean showDeleteConfirmationDialog(String mushroomName, double latitude, double longitude) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ištrinti įrašą?")
                .setMessage("Ar norite pašalinti įrašą?")
                .setPositiveButton("Taip", (dialogInterface, i) -> deleteTableRow(mushroomName, latitude, longitude))
                .setNegativeButton("Ne", null)
                .show();
        return true;
    }

    private void deleteTableRow(String mushroomName, double latitude, double longitude) {
        long photoId = dbHelper.getPhotoId(mushroomName, latitude, longitude);
        dbHelper.deletePhoto(photoId);
        tableLayout.removeAllViews();
        addLegendRow();
        populateTable();
        removeMarker(photoId);
    }

    private void removeMarker(long photoId) {
        for (Map.Entry<Marker, Long> entry : markerIdMap.entrySet()) {
            if (entry.getValue().equals(photoId)) {
                Marker marker = entry.getKey();
                marker.remove();
                markerIdMap.remove(marker);
                break;
            }
        }
    }

    private void addLegendRow() {
        TableRow legendRow = createTableRow();

        TextView legendMushroomName = createLegendTextView("Rūšis");
        legendRow.addView(legendMushroomName);

        addVerticalBorder(legendRow);

        TextView legendDate = createLegendTextView("Data");
        legendRow.addView(legendDate);

        tableLayout.addView(legendRow);
    }

    private TextView createLegendTextView(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextColor(Color.WHITE);
        textView.setPadding(10, 10, 10, 10);
        textView.setGravity(Gravity.CENTER);
        textView.setLayoutParams(new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2));
        return textView;
    }
}
