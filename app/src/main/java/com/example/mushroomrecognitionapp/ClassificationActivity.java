package com.example.mushroomrecognitionapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.graphics.Matrix;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

import com.example.mushroomrecognitionapp.ml.Model;

public class ClassificationActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100;
    private static final int IMAGE_SIZE = 299;

    private ImageButton camera, gallery, detailsButton, detailsButton2, saveButton, resetButton;
    private ImageView imageView;
    private TextView result1, confidence1, result2, confidence2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classification);

        initializeUI();

        checkLocationPermission();

        setOnClickListeners();
    }

    private void initializeUI() {
        camera = findViewById(R.id.button);
        gallery = findViewById(R.id.button2);
        detailsButton = findViewById(R.id.details_button);
        detailsButton2 = findViewById(R.id.details_button2);
        saveButton = findViewById(R.id.saveButton);
        resetButton = findViewById(R.id.resetButton);
        imageView = findViewById(R.id.imageView);
        result1 = findViewById(R.id.result1);
        confidence1 = findViewById(R.id.confidence1);
        result2 = findViewById(R.id.result2);
        confidence2 = findViewById(R.id.confidence2);

        detailsButton.setVisibility(View.GONE);
        detailsButton2.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
    }

    private void setOnClickListeners() {
        resetButton.setOnClickListener(v -> resetActivity());

        camera.setOnClickListener(view -> {
            if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 3);
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
            }
        });

        gallery.setOnClickListener(view -> {
            Intent cameraIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(cameraIntent, 1);
        });

        detailsButton.setOnClickListener(view -> moveToDetailsActivity(result1.getText().toString()));
        detailsButton2.setOnClickListener(view -> moveToDetailsActivity(result2.getText().toString()));

        saveButton.setOnClickListener(view -> saveImageData());
    }

    private void saveImageData() {
        String mushroomName = result1.getText().toString().split("\n")[0].trim();
        double latitude = getLatitude();
        double longitude = getLongitude();
        Bitmap photo = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());

        saveDataToDatabase(photo, mushroomName, latitude, longitude, date);
    }

    public void classifyImage(Bitmap image) {
        try {
            Model model = Model.newInstance(getApplicationContext());
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, IMAGE_SIZE, IMAGE_SIZE, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * IMAGE_SIZE * IMAGE_SIZE * 3).order(ByteOrder.nativeOrder());

            int[] intValues = new int[IMAGE_SIZE * IMAGE_SIZE];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

            for (int val : intValues) {
                byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f));
                byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f));
                byteBuffer.putFloat((val & 0xFF) * (1.f));
            }

            inputFeature0.loadBuffer(byteBuffer);

            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            updateUIWithResults(confidences);

            model.close();
        } catch (IOException e) {
            Log.e("Classification", "Nepavyko paleisti modelio", e);
        }
    }

    private void updateUIWithResults(float[] confidences) {
        String[] classes = Constants.classes;

        int maxPos1 = 0, maxPos2 = 0;
        float maxConfidence1 = 0, maxConfidence2 = 0;

        for (int i = 0; i < confidences.length; i++) {
            if (confidences[i] > maxConfidence1) {
                maxConfidence2 = maxConfidence1;
                maxPos2 = maxPos1;
                maxConfidence1 = confidences[i];
                maxPos1 = i;
            } else if (confidences[i] > maxConfidence2) {
                maxConfidence2 = confidences[i];
                maxPos2 = i;
            }
        }

        setTextAndColor(result1, confidence1, classes[maxPos1], maxConfidence1);
        setTextAndColor(result2, confidence2, classes[maxPos2], maxConfidence2);

        detailsButton.setVisibility(View.VISIBLE);
        detailsButton2.setVisibility(View.VISIBLE);
        saveButton.setVisibility(View.VISIBLE);
    }

    private void setTextAndColor(TextView result, TextView confidence, String resultText, float confidenceValue) {
        String confidenceText = String.format(Locale.getDefault(), "%.2f%%", confidenceValue * 100);
        result.setText(resultText);
        confidence.setText(confidenceText);

        int colorId;
        if (confidenceValue >= 0.7f) {
            colorId = R.color.high_confidence_color;
        } else if (confidenceValue >= 0.5f) {
            colorId = R.color.medium_confidence_color;
        } else {
            colorId = R.color.low_confidence_color;
        }
        confidence.setTextColor(getResources().getColor(colorId));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            Bitmap image = null;
            if (requestCode == 3) {
                image = (Bitmap) data.getExtras().get("data");
            } else if (requestCode == 1) {
                Uri uri = data.getData();
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (image != null) {
                imageView.setImageBitmap(image);
                Bitmap processedImage = makeHorizontal(downsampleBitmap(image, IMAGE_SIZE));
                classifyImage(processedImage);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private Bitmap makeHorizontal(Bitmap image) {
        int width = image.getWidth();
        int height = image.getHeight();
        if (width > height) {
            return image;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        return Bitmap.createBitmap(image, 0, 0, width, height, matrix, true);
    }

    private Bitmap downsampleBitmap(Bitmap bitmap, int targetLongerDimension) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float aspectRatio = (float) width / height;
        int targetWidth = targetLongerDimension;
        int targetHeight = Math.round(targetLongerDimension / aspectRatio);

        if (width < height) {
            targetWidth = Math.round(targetLongerDimension * aspectRatio);
            targetHeight = targetLongerDimension;
        }

        return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
    }

    private void moveToDetailsActivity(String resultText) {
        String mushroomClass = resultText.split("\n")[0].trim();
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("mushroomClass", mushroomClass);
        startActivity(intent);
    }

    private void saveDataToDatabase(Bitmap photo, String mushroomName, double latitude, double longitude, String date) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        Bitmap scaledPhoto = Bitmap.createScaledBitmap(photo, 400, 400, true);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scaledPhoto.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] photoData = stream.toByteArray();

        long id = databaseHelper.insertPhoto(photoData, mushroomName, latitude, longitude, date);
        databaseHelper.close();

        showToast(id != -1 ? "Duomenys išsaugoti sėkmingai" : "Nepavyko išsaugoti duomenų");
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private double getLatitude() {
        return getLocationData(Location::getLatitude);
    }

    private double getLongitude() {
        return getLocationData(Location::getLongitude);
    }

    private double getLocationData(LocationDataRetriever retriever) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
            return 0.0;
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                return retriever.getData(location);
            }
        }
        return 0.0;
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            getLocation();
        }
    }

    private Location getLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return null;
            }
            return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                showToast("Vietos nustatymas negalimas");
            }
        }
    }

    private void resetActivity() {
        imageView.setImageDrawable(null);
        result1.setText("");
        confidence1.setText("");
        result2.setText("");
        confidence2.setText("");
        detailsButton.setVisibility(View.GONE);
        detailsButton2.setVisibility(View.GONE);
        saveButton.setVisibility(View.GONE);
    }

    private interface LocationDataRetriever {
        double getData(Location location);
    }
}
