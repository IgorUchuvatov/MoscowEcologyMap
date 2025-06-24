package com.example.ecologemoscow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.ecologemoscow.api.OsrmApiClient;
import com.example.ecologemoscow.api.OsrmResponse;
import com.example.ecologemoscow.utils.PolylineDecoder;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RouteMapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "RouteMapFragment";
    private static final String ARG_ROUTE = "route";
    private EcologicalRoute route;
    private GoogleMap googleMap;

    public static RouteMapFragment newInstance(EcologicalRoute route) {
        RouteMapFragment fragment = new RouteMapFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_ROUTE, route);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            route = getArguments().getParcelable(ARG_ROUTE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_route_map, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.route_map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        TextView title = view.findViewById(R.id.route_title);
        TextView desc = view.findViewById(R.id.route_desc);
        if (route != null) {
            title.setText(route.name);
            desc.setText(route.description);
        }
        return view;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (route != null && route.path != null && !route.path.isEmpty()) {
            fetchAndDrawRoute();
        } else {
            LatLng moscow = new LatLng(55.751244, 37.618423);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(moscow, 10f));
        }
    }

    private void fetchAndDrawRoute() {
        String coordinates = route.path.stream()
                .map(c -> c.longitude + "," + c.latitude)
                .collect(Collectors.joining(";"));

        OsrmApiClient.getOsrmApi().getRoute(coordinates, "full", "polyline").enqueue(new Callback<OsrmResponse>() {
            @Override
            public void onResponse(@NonNull Call<OsrmResponse> call, @NonNull Response<OsrmResponse> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().getRoutes().isEmpty()) {
                    String encodedPolyline = response.body().getRoutes().get(0).getGeometry();
                    List<LatLng> decodedPath = PolylineDecoder.decode(encodedPolyline);
                    drawPolyline(decodedPath);
                } else {
                    Toast.makeText(getContext(), "Не удалось построить маршрут", Toast.LENGTH_SHORT).show();
                    drawFallbackPolyline();
                }
            }

            @Override
            public void onFailure(@NonNull Call<OsrmResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Ошибка при запросе к OSRM", t);
                Toast.makeText(getContext(), "Ошибка сети при построении маршрута", Toast.LENGTH_SHORT).show();
                drawFallbackPolyline();
            }
        });
    }
    
    private void drawPolyline(List<LatLng> path) {
        if (googleMap != null && path != null && !path.isEmpty()) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(path)
                    .color(0xFF009688) // бирюзовый
                    .width(12f)
                    .geodesic(true);
            googleMap.addPolyline(polylineOptions);

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng latLng : path) {
                builder.include(latLng);
            }
            LatLngBounds bounds = builder.build();
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        }
    }

    private void drawFallbackPolyline() {
        List<LatLng> fallbackPath = route.path.stream()
                .map(c -> new LatLng(c.latitude, c.longitude))
                .collect(Collectors.toList());
        drawPolyline(fallbackPath);
    }
} 