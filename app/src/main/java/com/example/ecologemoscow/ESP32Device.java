package com.example.ecologemoscow;

public class ESP32Device {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private double temperature;
    private double humidity;
    private double airQuality;
    private long lastUpdate;

    public ESP32Device(String id, String name, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.temperature = 0;
        this.humidity = 0;
        this.airQuality = 0;
        this.lastUpdate = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public double getTemperature() { return temperature; }
    public void setTemperature(double temperature) { this.temperature = temperature; }
    public double getHumidity() { return humidity; }
    public void setHumidity(double humidity) { this.humidity = humidity; }
    public double getAirQuality() { return airQuality; }
    public void setAirQuality(double airQuality) { this.airQuality = airQuality; }
    public long getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(long lastUpdate) { this.lastUpdate = lastUpdate; }
} 