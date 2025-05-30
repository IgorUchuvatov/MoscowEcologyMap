package com.example.ecologemoscow;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EcoPlaceParser {
    private static final String TAG = "EcoPlaceParser";
    private static final int TIMEOUT = 10000; // 10 секунд
    
    public interface OnParsingCompleteListener {
        void onParsingComplete(List<EcoPlace> places);
        void onParsingError(String error);
    }

    public void parseEcoPlaces(OnParsingCompleteListener listener) {
        new ParseEcoPlacesTask(listener).execute();
    }

    private static class ParseEcoPlacesTask extends AsyncTask<Void, Void, List<EcoPlace>> {
        private final OnParsingCompleteListener listener;
        private String errorMessage;

        ParseEcoPlacesTask(OnParsingCompleteListener listener) {
            this.listener = listener;
        }

        @Override
        protected List<EcoPlace> doInBackground(Void... voids) {
            List<EcoPlace> places = new ArrayList<>();
            
            try {
                // Парсинг с mos.ru
                places.addAll(parseMosRu());
                
                // Парсинг с moscowparks.ru
                places.addAll(parseMoscowParks());
                
            } catch (Exception e) {
                errorMessage = e.getMessage();
                Log.e(TAG, "Error parsing eco places", e);
            }
            
            return places;
        }

        @Override
        protected void onPostExecute(List<EcoPlace> places) {
            if (errorMessage != null) {
                listener.onParsingError(errorMessage);
            } else {
                listener.onParsingComplete(places);
            }
        }
    }

    private static List<EcoPlace> parseMosRu() throws IOException {
        List<EcoPlace> places = new ArrayList<>();
        String url = "https://www.mos.ru/eco/parks/";
        
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(TIMEOUT)
                    .get();
            
            Elements parkElements = doc.select(".park-item, .park-card, .park-list__item");
            
            for (Element park : parkElements) {
                String name = "";
                String description = "";
                String imageUrl = "";
                String address = "";
                String workingHours = "Круглосуточно";
                
                // Поиск названия парка
                Element nameElement = park.selectFirst(".park-name, .park-title, h2, h3");
                if (nameElement != null) {
                    name = nameElement.text().trim();
                }
                
                // Поиск описания
                Element descElement = park.selectFirst(".park-description, .description, p");
                if (descElement != null) {
                    description = descElement.text().trim();
                }
                
                // Поиск изображения
                Element imgElement = park.selectFirst("img");
                if (imgElement != null) {
                    imageUrl = imgElement.attr("src");
                    if (!imageUrl.startsWith("http")) {
                        imageUrl = "https://www.mos.ru" + imageUrl;
                    }
                }
                
                // Поиск адреса
                Element addressElement = park.selectFirst(".park-address, .address, [itemprop='address']");
                if (addressElement != null) {
                    address = addressElement.text().trim();
                }
                
                // Поиск времени работы
                Element hoursElement = park.selectFirst(".working-hours, .hours, [itemprop='openingHours']");
                if (hoursElement != null) {
                    workingHours = hoursElement.text().trim();
                }
                
                if (name != null && !name.isEmpty()) {
                    String[] coords = extractCoordinates(address);
                    places.add(new EcoPlace(
                        name,
                        description,
                        imageUrl,
                        address,
                        workingHours,
                        coords != null ? coords[0] : null,
                        coords != null ? coords[1] : null
                    ));
                    Log.d(TAG, "Added park from mos.ru: " + name);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error parsing mos.ru: " + e.getMessage());
            throw e;
        }
        
        return places;
    }

    private static List<EcoPlace> parseMoscowParks() throws IOException {
        List<EcoPlace> places = new ArrayList<>();
        String url = "https://moscowparks.ru/parks/";
        
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(TIMEOUT)
                    .get();
            
            Elements parkElements = doc.select(".park-item");
            
            for (Element park : parkElements) {
                String name = park.select(".park-title").text();
                String description = park.select(".park-description").text();
                String imageUrl = park.select(".park-image img").attr("src");
                String address = park.select(".park-address").text();
                String workingHours = park.select(".working-hours").text();
                
                if (name != null && !name.isEmpty()) {
                    String[] coords = extractCoordinates(address);
                    places.add(new EcoPlace(
                        name,
                        description,
                        imageUrl,
                        address,
                        workingHours,
                        coords != null ? coords[0] : null,
                        coords != null ? coords[1] : null
                    ));
                    Log.d(TAG, "Added park from moscowparks.ru: " + name);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error parsing moscowparks.ru: " + e.getMessage());
            throw e;
        }
        
        return places;
    }

    private static String[] extractCoordinates(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }

        // Известные координаты популярных парков Москвы
        switch (address.toLowerCase()) {
            case "ул. крымский вал, 9":
            case "крымский вал, 9":
                return new String[]{"55.7287", "37.6038"}; // Парк Горького
            case "воробьёвская наб., 1":
            case "воробьевы горы":
                return new String[]{"55.7100", "37.5594"}; // Воробьевы горы
            case "ул. поперечный просек, 1г":
            case "лосиный остров":
                return new String[]{"55.8833", "37.7833"}; // Лосиный остров
            case "новоясеневский тупик, 1":
            case "битцевский лес":
                return new String[]{"55.6000", "37.5500"}; // Битцевский лес
            case "таманская ул., 2а":
            case "серебряный бор":
                return new String[]{"55.7833", "37.4333"}; // Серебряный бор
            case "ул. дольская, 1":
            case "царицыно":
                return new String[]{"55.6167", "37.6833"}; // Царицыно
            case "ул. сокольнический вал, 1":
            case "сокольники":
                return new String[]{"55.8000", "37.6833"}; // Сокольники
            case "ул. островитянова, 10":
            case "долина реки сетунь":
                return new String[]{"55.7000", "37.4000"}; // Долина реки Сетунь
            case "ул. красная площадь":
            case "александровский сад":
                return new String[]{"55.7520", "37.6175"}; // Александровский сад
            case "ул. тверская, 13":
            case "сад эрмитаж":
                return new String[]{"55.7670", "37.6050"}; // Сад Эрмитаж
            case "поклонная гора":
            case "парк победы":
                return new String[]{"55.7300", "37.5000"}; // Парк Победы
            case "варшавское шоссе, 125":
            case "парк северного речного вокзала":
                return new String[]{"55.8500", "37.4833"}; // Парк Северного речного вокзала
            case "варварка, 6":
            case "зарядье":
                return new String[]{"55.7517", "37.6278"}; // Зарядье
            default:
                // Если адрес не найден в списке, возвращаем null
                // В будущем здесь можно добавить геокодирование через Google Maps API
                return null;
        }
    }

    public static List<EcoPlace> getDemoData() {
        List<EcoPlace> places = new ArrayList<>();
        
        // Парк Горького
        places.add(new EcoPlace(
            "Парк Горького",
            "Центральный парк культуры и отдыха имени Горького - один из самых известных парков Москвы",
            "https://park-gorkogo.com/upload/iblock/1c8/1c8c0c0c0c0c0c0c0c0c0c0c0c0c0c0c.jpg",
            "ул. Крымский Вал, 9",
            "Ежедневно с 6:00 до 23:00",
            "55.7287",
            "37.6048"
        ));

        // Сокольники
        places.add(new EcoPlace(
            "Сокольники",
            "Парк Сокольники - один из старейших парков Москвы с богатой историей",
            "https://park.sokolniki.com/upload/iblock/2d9/2d9c0c0c0c0c0c0c0c0c0c0c0c0c0c0c.jpg",
            "ул. Сокольнический Вал, 1",
            "Ежедневно с 6:00 до 23:00",
            "55.7897",
            "37.6732"
        ));

        // ВДНХ
        places.add(new EcoPlace(
            "ВДНХ",
            "Выставка достижений народного хозяйства - крупнейший выставочный комплекс в России",
            "https://vdnh.ru/upload/iblock/3ea/3eac0c0c0c0c0c0c0c0c0c0c0c0c0c0c.jpg",
            "проспект Мира, 119",
            "Ежедневно с 6:00 до 22:00",
            "55.8297",
            "37.6322"
        ));

        // Коломенское
        places.add(new EcoPlace(
            "Коломенское",
            "Музей-заповедник Коломенское - бывшая царская резиденция с уникальными памятниками архитектуры",
            "https://mgomz.ru/upload/iblock/4fb/4fbc0c0c0c0c0c0c0c0c0c0c0c0c0c0c.jpg",
            "проспект Андропова, 39",
            "Ежедневно с 7:00 до 22:00",
            "55.6677",
            "37.6712"
        ));

        // Царицыно
        places.add(new EcoPlace(
            "Царицыно",
            "Музей-заповедник Царицыно - дворцово-парковый ансамбль на юге Москвы",
            "https://tsaritsyno-museum.ru/upload/iblock/5gc/5gcc0c0c0c0c0c0c0c0c0c0c0c0c0c0c.jpg",
            "ул. Дольская, 1",
            "Ежедневно с 6:00 до 22:00",
            "55.6157",
            "37.6822"
        ));

        return places;
    }
} 