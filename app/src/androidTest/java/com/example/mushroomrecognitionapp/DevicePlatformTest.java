package com.example.mushroomrecognitionapp;

import android.os.Build;
import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class DevicePlatformTest {

    private static final String TAG = "DevicePlatformTest";

    @Test
    public void testIsRunningOnAndroid() {
        boolean isAndroid = isRunningOnAndroid();
        logAndroidDetails();
        assertTrue("The application is not running on an Android device", isAndroid);
    }

    private boolean isRunningOnAndroid() {
        return "Android".equalsIgnoreCase(Build.BRAND) || "Android".equalsIgnoreCase(Build.MANUFACTURER);
    }

    private void logAndroidDetails() {
        Log.i(TAG, "Running on Android device");
        Log.i(TAG, "Brand: " + Build.BRAND);
        Log.i(TAG, "Manufacturer: " + Build.MANUFACTURER);
        Log.i(TAG, "Model: " + Build.MODEL);
        Log.i(TAG, "Device: " + Build.DEVICE);
        Log.i(TAG, "Version Release: " + Build.VERSION.RELEASE);
        Log.i(TAG, "SDK Version: " + Build.VERSION.SDK_INT);
    }
}
