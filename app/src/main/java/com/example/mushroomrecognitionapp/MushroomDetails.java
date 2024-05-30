package com.example.mushroomrecognitionapp;

public class MushroomDetails {

    private final String name;
    private final String scientificName;
    private final String description;
    private final String type;

    public MushroomDetails(String name, String scientificName, String description, String type) {
        this.name = name;
        this.scientificName = scientificName;
        this.description = description;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getScientificName() {
        return scientificName;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }
}
