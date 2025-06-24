package com.example.ecologemoscow;

public class Park {
    public String name;
    public String link;
    public String description;
    public double latitude;
    public double longitude;
    public int cleanlinessIndex; // Индекс чистоты от 0 до 10

    public Park(String name, String link, String description, double latitude, double longitude, int cleanlinessIndex) {
        this.name = name;
        this.link = link;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        // Ограничиваем индекс чистоты диапазоном от 0 до 10
        this.cleanlinessIndex = Math.max(0, Math.min(10, cleanlinessIndex));
    }
} 