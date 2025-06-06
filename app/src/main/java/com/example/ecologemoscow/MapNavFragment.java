package com.example.ecologemoscow;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.ecologemoscow.charts.ButovoChartFragment;
import com.example.ecologemoscow.charts.ChartsContainerFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class MapNavFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "MapNavFragment";
    private static final LatLng MOSCOW = new LatLng(55.7558, 37.6173);
    private static final float DEFAULT_ZOOM = 10f;
    private static final LatLng SOUTH_BUTOVO_CENTER = new LatLng(55.5417, 37.5317);
    private static final LatLng ESP32_LOCATION = new LatLng(55.5417, 37.5317); // Координаты датчика в Южном Бутово

    private GoogleMap mMap;
    private boolean isLocationEnabled = false;
    private ActivityResultLauncher<String[]> permissionLauncher;
    private boolean isMapReady = false;
    private boolean isFragmentActive = false;
    private ProgressBar progressBar;
    private TextView errorView;
    private Button retryButton;
    private FloatingActionButton graphsButton;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;
    private Polygon southButovoPolygon;
    private Marker deviceMarker;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupPermissionLauncher();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_nav, container, false);
        
        graphsButton = view.findViewById(R.id.graphs_button);
        graphsButton.setOnClickListener(v -> showGraphForSouthButovo());
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        isFragmentActive = true;
        
        progressBar = view.findViewById(R.id.progress_bar);
        errorView = view.findViewById(R.id.error_view);
        retryButton = view.findViewById(R.id.retry_button);
        
        retryButton.setOnClickListener(v -> {
            if (retryCount < MAX_RETRIES) {
                retryCount++;
                initializeMap();
            }
        });
        
        initializeMap();
    }

    private void setupPermissionLauncher() {
        permissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            permissions -> {
                if (!isFragmentActive) return;
                
                boolean allGranted = true;
                for (Boolean isGranted : permissions.values()) {
                    if (!isGranted) {
                        allGranted = false;
                        break;
                    }
                }
                
                isLocationEnabled = allGranted;
                if (allGranted && isMapReady && isFragmentActive) {
                    enableLocationFeatures();
                } else if (!allGranted && isFragmentActive) {
                    Toast.makeText(requireContext(), "Для работы с картой необходим доступ к местоположению", Toast.LENGTH_LONG).show();
                    showError("Для работы с картой необходим доступ к местоположению");
                }
            }
        );
    }

    private void initializeMap() {
        if (!isFragmentActive) return;
        
        showLoading();
        
        //  интернет
        if (!isNetworkAvailable()) {
            showError("Нет подключения к интернету");
            return;
        }
        
        try {
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            } else {
                Log.e(TAG, "Map fragment not found");
                showError("Ошибка загрузки карты");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing map: " + e.getMessage());
            showError("Ошибка инициализации карты: " + e.getMessage());
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (!isFragmentActive) return;
        
        try {
            mMap = googleMap;
            isMapReady = true;
            setupMapSettings();
            checkAndRequestLocationPermission();
            hideLoading();
        } catch (Exception e) {
            Log.e(TAG, "Error in onMapReady: " + e.getMessage());
            showError("Ошибка загрузки карты: " + e.getMessage());
        }
    }

    private void setupMapSettings() {
        if (mMap == null || !isFragmentActive) return;
        
        try {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.getUiSettings().setAllGesturesEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);
            
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MOSCOW, DEFAULT_ZOOM));
            
            if (isLocationEnabled && hasLocationPermission()) {
                mMap.setMyLocationEnabled(true);
            }

            // Добавляем полигон Южное Бутово
            addSouthButovoPolygon();
            
            // Добавляем маркер ESP32 устройства
            deviceMarker = mMap.addMarker(new MarkerOptions()
                    .position(ESP32_LOCATION)
                    .title("ESP32 Датчик")
                    .snippet("Нажмите, чтобы увидеть данные о состоянии окружающей среды")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            
            // Показываем информационное окно маркера устройства
            deviceMarker.showInfoWindow();
            
            // Добавляем обработчик нажатия на маркер
            mMap.setOnMarkerClickListener(this);
            
        } catch (SecurityException e) {
            Log.e(TAG, "Error setting up map: " + e.getMessage());
            showError("Ошибка настройки карты: " + e.getMessage());
        }
    }

    private void addSouthButovoPolygon() {
        List<LatLng> polygonPoints = new ArrayList<>();
        // Обновленные координаты полигона Южное Бутово
        polygonPoints.add(new LatLng(55.5317, 37.5217)); // Юго-запад
        polygonPoints.add(new LatLng(55.5317, 37.5517)); // Северо-запад
        polygonPoints.add(new LatLng(55.5617, 37.5517)); // Северо-восток
        polygonPoints.add(new LatLng(55.5617, 37.5217)); // Юго-восток
        polygonPoints.add(new LatLng(55.5317, 37.5217)); // Замыкаем полигон

        PolygonOptions polygonOptions = new PolygonOptions()
                .addAll(polygonPoints)
                .strokeColor(Color.BLUE)
                .strokeWidth(5)
                .fillColor(Color.argb(50, 0, 0, 255));

        southButovoPolygon = mMap.addPolygon(polygonOptions);
        southButovoPolygon.setClickable(true);

        // Добавляем обработчик нажатия на полигон
        mMap.setOnPolygonClickListener(polygon -> {
            if (polygon.equals(southButovoPolygon)) {
                ChartsContainerFragment chartsFragment = new ChartsContainerFragment();

                // Передаем информацию о районе
                Bundle args = new Bundle();
                args.putString("district_name", "Южное Бутово");
                args.putString("default_chart", "dust");
                chartsFragment.setArguments(args);

                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, chartsFragment)
                            .addToBackStack(null)
                            .commit();
                    Log.d(TAG, "ChartsContainerFragmentотображен");
                }
            }
        });

        // Добавляем обработчик нажатия на карту для отладки
        mMap.setOnMapClickListener(latLng -> {
            Log.d(TAG, "Координаты клика: " + latLng.latitude + ", " + latLng.longitude);
        });
    }

    private void checkAndRequestLocationPermission() {
        if (!isFragmentActive) return;
        
        if (hasLocationPermission()) {
            isLocationEnabled = true;
            enableLocationFeatures();
        } else {
            requestLocationPermission();
        }
    }

    private boolean hasLocationPermission() {
        if (!isFragmentActive) return false;
        
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        if (!isFragmentActive) return;
        
        permissionLauncher.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void enableLocationFeatures() {
        if (mMap == null || !isFragmentActive) return;
        
        try {
            if (hasLocationPermission()) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                isLocationEnabled = true;
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Error enabling location features: " + e.getMessage());
            showError("Ошибка включения геолокации: " + e.getMessage());
        }
    }

    private void showLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        if (errorView != null) {
            errorView.setVisibility(View.GONE);
        }
        if (retryButton != null) {
            retryButton.setVisibility(View.GONE);
        }
    }

    private void hideLoading() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        hideLoading();
        if (errorView != null) {
            errorView.setText(message);
            errorView.setVisibility(View.VISIBLE);
        }
        if (retryButton != null && retryCount < MAX_RETRIES) {
            retryButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isFragmentActive = true;
        if (mMap != null && isMapReady) {
            setupMapSettings();
        } else {
            initializeMap();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isFragmentActive = false;
        if (mMap != null) {
            try {
                mMap.setMyLocationEnabled(false);
            } catch (SecurityException e) {
                Log.e(TAG, "Error disabling location: " + e.getMessage());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFragmentActive = false;
        if (mMap != null) {
            mMap.clear();
            mMap = null;
        }
        isMapReady = false;
    }

    public void showPlace(double latitude, double longitude, String title) {
        if (mMap != null) {
            LatLng location = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions()
                    .position(location)
                    .title(title));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker.equals(deviceMarker)) {
            showGraphForSouthButovo();
            return true;
        }
        return false;
    }

    private void showGraphForSouthButovo() {
        try {
            Log.d(TAG, "showGraphForSouthButovo: Начало метода");
            
            if (getActivity() == null) {
                Log.e(TAG, "showGraphForSouthButovo: Activity is null");
                return;
            }

            // Проверяем контейнер
            View container = getActivity().findViewById(R.id.fragment_container);
            if (container == null) {
                Log.e(TAG, "showGraphForSouthButovo: fragment_container не найден");
                Toast.makeText(getContext(), "Ошибка: контейнер для фрагмента не найден", Toast.LENGTH_SHORT).show();
                return;
            }

            // Создаем новый фрагмент с графиком
            ButovoChartFragment chartFragment = new ButovoChartFragment();
            
            // Заменяем текущий фрагмент на фрагмент с графиком
            getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, chartFragment)
                .addToBackStack("map") // Добавляем тег для идентификации в стеке
                .commit();
                
            Log.d(TAG, "showGraphForSouthButovo: Фрагмент с графиком успешно создан и добавлен");
            
        } catch (Exception e) {
            Log.e(TAG, "showGraphForSouthButovo: Ошибка при создании фрагмента с графиком", e);
            Toast.makeText(getContext(), "Ошибка при открытии графиков", Toast.LENGTH_SHORT).show();
        }
    }
} 