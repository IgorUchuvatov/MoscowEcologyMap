package com.example.ecologemoscow.models;

import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EcoEvent {
    private static final String TAG = "EcoEvent";
    private String title;
    private String date;
    private String link;
    private String description;
    private String location;

    public EcoEvent(String title, String date, String link, String description, String location) {
        this.title = title;
        this.date = date;
        this.link = link;
        this.description = description;
        this.location = location;
    }

    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getLink() { return link; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }

    public boolean isActive() {
        try {
            // Добавляем время к дате, если его нет
            String dateWithTime = date;
            if (date.length() == 10) { // Если дата в формате "YYYY-MM-DD"
                dateWithTime = date + "T00:00:00";
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date eventDate = sdf.parse(dateWithTime);
            Date currentDate = new Date();
            
            // Сравниваем даты без учета времени
            SimpleDateFormat dateOnlySdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date eventDateOnly = dateOnlySdf.parse(dateOnlySdf.format(eventDate));
            Date currentDateOnly = dateOnlySdf.parse(dateOnlySdf.format(currentDate));
            
            Log.d(TAG, String.format("Проверка события '%s': дата события=%s, текущая дата=%s", 
                title, dateOnlySdf.format(eventDateOnly), dateOnlySdf.format(currentDateOnly)));
            
            boolean isActive = eventDateOnly != null && 
                             (eventDateOnly.equals(currentDateOnly) || eventDateOnly.after(currentDateOnly));
            
            Log.d(TAG, String.format("Событие '%s' %s", title, isActive ? "активно" : "неактивно"));
            
            return isActive;
        } catch (ParseException e) {
            Log.e(TAG, "Ошибка парсинга даты для события: " + title + ", дата: " + date, e);
            return false;
        }
    }
} 