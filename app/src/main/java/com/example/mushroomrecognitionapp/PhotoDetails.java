package com.example.mushroomrecognitionapp;

public class PhotoDetails {
    private final long id;
    private final byte[] photoData;
    private final String mushroomName;
    private final double latitude;
    private final double longitude;
    private final String date;

    public PhotoDetails(long id, byte[] photoData, String mushroomName, double latitude, double longitude, String date) {
        this.id = id;
        this.photoData = photoData;
        this.mushroomName = mushroomName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public byte[] getPhotoData() {
        return photoData;
    }

    public String getMushroomName() {
        return mushroomName;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getDate() {
        return date;
    }
}
