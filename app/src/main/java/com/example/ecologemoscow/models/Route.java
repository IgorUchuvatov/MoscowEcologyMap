package com.example.ecologemoscow.models;

import com.google.gson.annotations.SerializedName;

public class Route {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("images")
    private Image[] images;

    @SerializedName("place")
    private Place place;

    @SerializedName("price")
    private String price;

    @SerializedName("duration")
    private String duration;

    @SerializedName("distance")
    private String distance;

    public static class Image {
        @SerializedName("image")
        private String imageUrl;
    }

    public static class Place {
        @SerializedName("title")
        private String title;

        @SerializedName("address")
        private String address;

        @SerializedName("coords")
        private Coords coords;
    }

    public static class Coords {
        @SerializedName("lat")
        private double latitude;

        @SerializedName("lon")
        private double longitude;
    }

    // Геттеры
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getImageUrl() { return images != null && images.length > 0 ? images[0].imageUrl : null; }
    public String getPlaceTitle() { return place != null ? place.title : null; }
    public String getAddress() { return place != null ? place.address : null; }
    public double getLatitude() { return place != null && place.coords != null ? place.coords.latitude : 0; }
    public double getLongitude() { return place != null && place.coords != null ? place.coords.longitude : 0; }
    public String getPrice() { return price; }
    public String getDuration() { return duration; }
    public String getDistance() { return distance; }
} 