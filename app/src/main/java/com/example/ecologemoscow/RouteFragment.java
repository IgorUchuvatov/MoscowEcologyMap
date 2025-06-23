package com.example.ecologemoscow;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RouteFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "RouteFragment";
    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONGITUDE = "longitude";
    private static final String ARG_TITLE = "title";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final long LOCATION_UPDATE_INTERVAL = 3000; // 3 секунды
    
    private double latitude;
    private double longitude;
    private String title;
    private GoogleMap map;
    private TextView routeInfoTextView;
    private TextView routeDetailsTextView;
    private Button buildRouteButton;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private LatLng currentLocation;
    private LatLng destinationLocation;
    private RequestQueue requestQueue;
    private boolean isRouteBuilt = false;
    private boolean isNavigating = false;
    private List<String> navigationSteps = new ArrayList<>();
    private int currentStepIndex = 0;
    private TextView stepTextView;

    public static RouteFragment newInstance(double latitude, double longitude, String title) {
        RouteFragment fragment = new RouteFragment();
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        requestQueue = Volley.newRequestQueue(requireContext());
        
        // Настраиваем запрос на обновление местоположения
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(LOCATION_UPDATE_INTERVAL)
                .setMaxUpdateDelayMillis(LOCATION_UPDATE_INTERVAL)
                .build();

        // Создаем callback для обработки обновлений местоположения
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    if (isNavigating && map != null) {
                        // Обновляем камеру только если включен режим навигации
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
                    }
                }
            }
        };
        
        Log.d(TAG, "RouteFragment created for: " + title);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route, container, false);
        
        ImageButton backButton = view.findViewById(R.id.back_button);
        routeInfoTextView = view.findViewById(R.id.route_info);
        routeDetailsTextView = view.findViewById(R.id.route_details);
        buildRouteButton = view.findViewById(R.id.build_route_button);
        
        routeInfoTextView.setText("Маршрут к парку: " + title);
        
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        
        buildRouteButton.setOnClickListener(v -> {
            if (!isRouteBuilt) {
                if (checkLocationPermission()) {
                    getCurrentLocation();
                } else {
                    requestLocationPermission();
                }
            } else {
                // Переключаем режим навигации
                isNavigating = !isNavigating;
                if (isNavigating) {
                    startLocationUpdates();
                    buildRouteButton.setText("Остановить навигацию");
                    if (currentLocation != null) {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18));
                    }
                } else {
                    stopLocationUpdates();
                    buildRouteButton.setText("Начать навигацию");
                    // Показываем весь маршрут
                    if (currentLocation != null && destinationLocation != null) {
                        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                        boundsBuilder.include(currentLocation);
                        boundsBuilder.include(destinationLocation);
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
                    }
                }
            }
        });
        
        stepTextView = new TextView(requireContext());
        stepTextView.setTextSize(16);
        stepTextView.setTextColor(Color.BLACK);
        ((ViewGroup) view).addView(stepTextView);
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.route_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        destinationLocation = new LatLng(latitude, longitude);
        
        // Добавляем маркер места назначения
        map.addMarker(new MarkerOptions()
                .position(destinationLocation)
                .title(title));
        
        // Приближаем камеру к месту назначения
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(destinationLocation, 15));
        
        if (checkLocationPermission()) {
            map.setMyLocationEnabled(true);
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(requireContext(), 
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        
        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(requireContext(), "Для построения маршрута необходим доступ к геолокации", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        map.setMyLocationEnabled(true);
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                setupMapAndRoute();
            } else {
                Toast.makeText(requireContext(), "Не удалось получить текущее местоположение", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMapAndRoute() {
        if (currentLocation == null || destinationLocation == null) return;

        map.clear();
        map.addMarker(new MarkerOptions().position(currentLocation).title("Ваше местоположение"));
        map.addMarker(new MarkerOptions().position(destinationLocation).title(title));

        // Формируем запрос к OSRM (пешеходный маршрут)
        String url = String.format(Locale.US,
            "https://router.project-osrm.org/route/v1/foot/%f,%f;%f,%f?overview=full&geometries=geojson",
            currentLocation.longitude, currentLocation.latitude,
            destinationLocation.longitude, destinationLocation.latitude
        );

        Log.d(TAG, "OSRM API URL: " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
            response -> {
                Log.d(TAG, "OSRM API response: " + response);
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    String code = jsonResponse.optString("code", "NO_CODE");
                    String message = jsonResponse.optString("message", "NO_MESSAGE");
                    Log.d(TAG, "OSRM code: " + code);
                    if (!"NO_MESSAGE".equals(message)) {
                        Log.d(TAG, "OSRM message: " + message);
                    }
                    JSONArray routes = jsonResponse.optJSONArray("routes");
                    if ("Ok".equals(code)) {
                        if (routes != null && routes.length() > 0) {
                            JSONObject route = routes.getJSONObject(0);
                            double distance = route.getDouble("distance") / 1000.0; // в км
                            double duration = route.getDouble("duration") / 60.0; // в минутах
                            routeDetailsTextView.setText(String.format("\uD83D\uDDFA %.1f км   ⏱ %.0f мин", distance, duration));

                            // Получаем координаты маршрута
                            JSONObject geometry = route.getJSONObject("geometry");
                            JSONArray coordinates = geometry.getJSONArray("coordinates");
                            List<LatLng> path = new ArrayList<>();
                            for (int i = 0; i < coordinates.length(); i++) {
                                JSONArray coord = coordinates.getJSONArray(i);
                                double lon = coord.getDouble(0);
                                double lat = coord.getDouble(1);
                                path.add(new LatLng(lat, lon));
                            }
                            map.addPolyline(new PolylineOptions()
                                    .addAll(path)
                                    .width(12)
                                    .color(Color.rgb(33, 150, 243)));

                            // Масштабируем карту на маршрут
                            LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                            for (LatLng point : path) boundsBuilder.include(point);
                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));

                            isRouteBuilt = true;
                            buildRouteButton.setText("Начать навигацию");
                        } else {
                            Log.e(TAG, "OSRM: routes array is empty. routes=" + (routes != null ? routes.toString() : "null"));
                            Toast.makeText(requireContext(), "Маршрут не найден (OSRM)", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "OSRM API error: code=" + code + ", message=" + message + ", routes=" + (routes != null ? routes.toString() : "null"));
                        Toast.makeText(requireContext(), "Ошибка OSRM: " + code + ("NO_MESSAGE".equals(message) ? "" : (" (" + message + ")")), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing OSRM response: " + e.getMessage(), e);
                    Toast.makeText(requireContext(), "Ошибка при построении маршрута (OSRM)", Toast.LENGTH_SHORT).show();
                }
            },
            error -> {
                Log.e(TAG, "Error fetching OSRM route: " + error.toString(), error);
                Toast.makeText(requireContext(), "Ошибка при получении маршрута (OSRM)", Toast.LENGTH_SHORT).show();
            });

        requestQueue.add(request);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopLocationUpdates();
        map = null;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isNavigating && checkLocationPermission()) {
            startLocationUpdates();
        }
    }
} 