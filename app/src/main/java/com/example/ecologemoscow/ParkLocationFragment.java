package com.example.ecologemoscow;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import android.util.Log;
import java.util.Locale;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Polyline;

public class ParkLocationFragment extends Fragment implements OnMapReadyCallback {

    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONGITUDE = "longitude";
    private static final String ARG_TITLE = "title";
    private static final String TAG = "ParkLocationFragment";

    private double latitude;
    private double longitude;
    private String title;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue requestQueue;
    private Button buildRouteButton;
    private PolylineOptions currentPolyline;
    private boolean isRouteBuilt = false;
    private CardView routeInfoCard;
    private TextView routeDistanceView;
    private TextView routeDurationView;
    private Marker userMarker;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private boolean followUser = true; // карта будет следовать за пользователем
    private LatLng lastUserLocation; // для перемещения карты после построения маршрута
    private List<LatLng> routePath = new ArrayList<>();
    private Polyline passedPolyline;
    private Polyline remainingPolyline;
    private double finishLat;
    private double finishLng;

    public static ParkLocationFragment newInstance(double latitude, double longitude, String title) {
        ParkLocationFragment fragment = new ParkLocationFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_LATITUDE, latitude);
        args.putDouble(ARG_LONGITUDE, longitude);
        args.putString(ARG_TITLE, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            latitude = getArguments().getDouble(ARG_LATITUDE);
            longitude = getArguments().getDouble(ARG_LONGITUDE);
            title = getArguments().getString(ARG_TITLE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_park_location, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.park_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        requestQueue = Volley.newRequestQueue(requireContext());

        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        buildRouteButton = view.findViewById(R.id.build_route_button);
        buildRouteButton.setOnClickListener(v -> buildRoute());

        routeInfoCard = view.findViewById(R.id.route_info_card);
        routeDistanceView = view.findViewById(R.id.route_distance);
        routeDurationView = view.findViewById(R.id.route_duration);
        routeInfoCard.setVisibility(View.GONE);

        finishLat = latitude;
        finishLng = longitude;

        // Настраиваем LocationCallback для отслеживания пользователя
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null && map != null) {
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if (userMarker == null) {
                        userMarker = map.addMarker(new MarkerOptions().position(userLatLng).title("Вы здесь").icon(com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    } else {
                        userMarker.setPosition(userLatLng);
                    }
                    if (followUser) {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));
                    }
                    lastUserLocation = userLatLng;
                    if (!routePath.isEmpty()) {
                        updateRouteProgress(userLatLng);
                    }
                }
            }
        };
        // Настраиваем LocationRequest
        locationRequest = new LocationRequest.Builder(1000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .build();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private void buildRoute() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext(), "Нет разрешения на определение местоположения", Toast.LENGTH_SHORT).show();
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                Log.d(TAG, "User location: " + location.getLatitude() + ", " + location.getLongitude());
                requestRoute(location);
            } else {
                Toast.makeText(getContext(), "Не удалось получить местоположение пользователя", Toast.LENGTH_LONG).show();
                Log.e(TAG, "User location is null");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Ошибка получения местоположения: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Location error: ", e);
        });
    }

    private void requestRoute(Location location) {
        String url = String.format(Locale.US, "https://router.project-osrm.org/route/v1/foot/%.6f,%.6f;%.6f,%.6f?overview=full&geometries=geojson",
                location.getLongitude(), location.getLatitude(), longitude, latitude);
        Log.d(TAG, "OSRM request url: " + url);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> handleRouteResponse(response),
                error -> {
                    Toast.makeText(getContext(), "Ошибка построения маршрута: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "OSRM error: ", error);
                }
        );
        requestQueue.add(request);
    }

    private void handleRouteResponse(String response) {
        try {
            JSONObject json = new JSONObject(response);
            JSONArray routes = json.getJSONArray("routes");
            if (routes.length() > 0) {
                JSONObject route = routes.getJSONObject(0);
                double distance = route.getDouble("distance") / 1000.0; // в км
                double duration = route.getDouble("duration") / 60.0; // в минутах
                JSONObject geometry = route.getJSONObject("geometry");
                JSONArray coordinates = geometry.getJSONArray("coordinates");
                List<LatLng> path = new ArrayList<>();
                for (int i = 0; i < coordinates.length(); i++) {
                    JSONArray coord = coordinates.getJSONArray(i);
                    double lon = coord.getDouble(0);
                    double lat = coord.getDouble(1);
                    path.add(new LatLng(lat, lon));
                }
                if (map != null) {
                    if (currentPolyline != null) map.clear();
                    map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(title));
                    currentPolyline = new PolylineOptions().addAll(path).width(10).color(0xFF2196F3);
                    map.addPolyline(currentPolyline);
                    // Сохраняем маршрут для прогресса
                    routePath.clear();
                    routePath.addAll(path);
                    // После построения маршрута — моментально к пользователю
                    if (lastUserLocation != null) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15));
                        updateRouteProgress(lastUserLocation);
                    }
                    isRouteBuilt = true;
                    buildRouteButton.setVisibility(View.GONE); // Скрываем кнопку
                    routeInfoCard.setVisibility(View.VISIBLE);
                    routeDistanceView.setText(String.format("🗺️ %.1f км", distance));
                    routeDurationView.setText(String.format("⏱️ %.0f мин", duration));
                }
            }
        } catch (Exception e) {
            Toast.makeText(getContext(), "Ошибка обработки маршрута: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Route parse error: ", e);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        LatLng parkLocation = new LatLng(latitude, longitude);
        map.addMarker(new MarkerOptions().position(parkLocation).title(title));
        // Сначала фокус на парке
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(parkLocation, 15));
    }

    private void updateRouteProgress(LatLng userLatLng) {
        if (routePath.isEmpty() || map == null) return;
        // Найти ближайшую точку маршрута к пользователю
        int closestIdx = 0;
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < routePath.size(); i++) {
            double d = distanceBetween(userLatLng, routePath.get(i));
            if (d < minDist) {
                minDist = d;
                closestIdx = i;
            }
        }
        // Пройденный путь — от старта до ближайшей точки
        List<LatLng> passed = new ArrayList<>(routePath.subList(0, closestIdx + 1));
        // Оставшийся путь — от ближайшей точки до финиша
        List<LatLng> remaining = new ArrayList<>(routePath.subList(closestIdx, routePath.size()));
        // Удаляем старые линии
        if (passedPolyline != null) passedPolyline.remove();
        if (remainingPolyline != null) remainingPolyline.remove();
        // Рисуем новые
        if (passed.size() > 1) {
            passedPolyline = map.addPolyline(new PolylineOptions().addAll(passed).width(10).color(0xFF4CAF50)); // зелёный
        }
        if (remaining.size() > 1) {
            remainingPolyline = map.addPolyline(new PolylineOptions().addAll(remaining).width(10).color(0xFF2196F3)); // синий
        }
        // Показываем расстояние до финиша
        double remainDist = 0;
        for (int i = closestIdx; i < routePath.size() - 1; i++) {
            remainDist += distanceBetween(routePath.get(i), routePath.get(i + 1));
        }
        remainDist += distanceBetween(userLatLng, routePath.get(closestIdx));
        routeDistanceView.setText(String.format("🗺️ %.1f км до финиша", remainDist / 1000.0));
    }

    // Вычисление расстояния между двумя точками (метры)
    private double distanceBetween(LatLng a, LatLng b) {
        double earthRadius = 6371000; // meters
        double dLat = Math.toRadians(b.latitude - a.latitude);
        double dLng = Math.toRadians(b.longitude - a.longitude);
        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);
        double va = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(a.latitude)) * Math.cos(Math.toRadians(b.latitude));
        double vc = 2 * Math.atan2(Math.sqrt(va), Math.sqrt(1 - va));
        return earthRadius * vc;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Удаляем карту из childFragmentManager, чтобы не было конфликтов при повторном открытии
        Fragment mapFragment = getChildFragmentManager().findFragmentById(R.id.park_map);
        if (mapFragment != null) {
            getChildFragmentManager().beginTransaction().remove(mapFragment).commitAllowingStateLoss();
        }
    }
} 