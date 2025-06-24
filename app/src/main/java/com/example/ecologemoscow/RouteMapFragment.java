package com.example.ecologemoscow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.ArrayList;
import java.util.List;

public class RouteMapFragment extends Fragment implements OnMapReadyCallback {
    private static final String ARG_ROUTE = "route";
    private EcologicalRoute route;

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
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.route_map, mapFragment).commit();
        }
        mapFragment.getMapAsync(this);

        // Отображаем название и описание маршрута
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
        if (route != null && route.path != null && !route.path.isEmpty()) {
            List<LatLng> latLngs = new ArrayList<>();
            for (EcologicalRoute.Coordinate coord : route.path) {
                latLngs.add(new LatLng(coord.latitude, coord.longitude));
            }
            PolylineOptions polylineOptions = new PolylineOptions()
                    .addAll(latLngs)
                    .color(0xFF388E3C) // зелёный
                    .width(8f);
            googleMap.addPolyline(polylineOptions);
            // Центрируем карту на первой точке маршрута
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngs.get(0), 15f));
        } else {
            // Если нет координат — центрируем на Москве
            LatLng moscow = new LatLng(55.751244, 37.618423);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(moscow, 10f));
        }
    }
} 