package com.example.ecologemoscow.models;

public class Park {
    public final String name;
    public final String slug;
    public final String description;
    public final double latitude;
    public final double longitude;
    public final int cleanlinessIndex;

    public Park(String name, String slug, String description, double latitude, double longitude, int cleanlinessIndex) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.cleanlinessIndex = cleanlinessIndex;
    }
} 