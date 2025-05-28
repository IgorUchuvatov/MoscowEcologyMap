package com.example.ecologemoscow.utils;

import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.example.ecologemoscow.models.EcoEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.json.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import org.jsoup.parser.Parser;

public class EcoEventParser {
    private static final String TAG = "EcoEventParser";

    public static List<EcoEvent> fetchEcoEvents() {
        List<EcoEvent> activeEvents = new ArrayList<>();
        try {
            String url = "https://mosvolonter.ru/events?search_type=events";
            Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                .timeout(10000)
                .get();

            String html = doc.html();
            int start = html.indexOf("let events = ");
            if (start == -1) {
                Log.e(TAG, "Не найдена строка 'let events = ' в HTML");
                return activeEvents;
            }
            
            int jsonStart = start + "let events = ".length();
            int jsonEnd = html.indexOf("};", jsonStart) + 1;
            if (jsonEnd <= jsonStart) {
                Log.e(TAG, "Не удалось найти конец JSON объекта");
                return activeEvents;
            }
            
            String jsonString = html.substring(jsonStart, jsonEnd);
            Log.d(TAG, "Получен JSON: " + jsonString);

            JSONObject eventsObj = new JSONObject(jsonString);
            JSONObject eventsList = eventsObj.getJSONObject("eventsList");

            Iterator<String> monthKeys = eventsList.keys();
            while (monthKeys.hasNext()) {
                String monthKey = monthKeys.next();
                JSONObject monthObj = eventsList.getJSONObject(monthKey);
                
                Iterator<String> dayKeys = monthObj.keys();
                while (dayKeys.hasNext()) {
                    String dayKey = dayKeys.next();
                    JSONArray dayArr = monthObj.getJSONArray(dayKey);
                    for (int i = 0; i < dayArr.length(); i++) {
                        JSONObject event = dayArr.getJSONObject(i);
                        String title = event.optString("title");
                        String date = event.optString("dateStart");
                        String link = "https://mosvolonter.ru" + event.optString("url");
                        String description = event.optString("content").replaceAll("<.*?>", "");
                        String location = event.optString("location");
                        
                        Log.d(TAG, "Обработка события: " + title + ", дата: " + date);
                        
                        // Декодируем HTML-сущности
                        description = Parser.unescapeEntities(description, false);
                        EcoEvent ecoEvent = new EcoEvent(title, date, link, description, location);
                        
                        if (ecoEvent.isActive()) {
                            Log.d(TAG, "Событие активно: " + title);
                            activeEvents.add(ecoEvent);
                        } else {
                            Log.d(TAG, "Событие неактивно: " + title);
                        }
                    }
                }
            }
            
            Log.d(TAG, "Всего найдено активных событий: " + activeEvents.size());
            
        } catch (Exception e) {
            Log.e(TAG, "Ошибка при парсинге событий", e);
            if (Looper.myLooper() != null) {
                Toast.makeText(null, "Ошибка загрузки мероприятий: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        return activeEvents;
    }
} 