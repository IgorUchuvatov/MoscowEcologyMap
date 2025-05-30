package com.example.ecologemoscow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "MapFragment";
    private static final String ARG_LATITUDE = "latitude";
    private static final String ARG_LONGITUDE = "longitude";
    private static final String ARG_TITLE = "title";
    
    private double latitude;
    private double longitude;
    private String title;
    private GoogleMap map;

    public static MapFragment newInstance(double latitude, double longitude, String title) {
        MapFragment fragment = new MapFragment();
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
        Log.d(TAG, "Fragment created with coordinates: " + latitude + ", " + longitude);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        
        ImageButton backButton = view.findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated called");
        
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            Log.d(TAG, "Map fragment found, getting map async");
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Map fragment is null!");
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady called");
        map = googleMap;

        try {
            // Добавляем маркер парка
            LatLng parkLocation = new LatLng(latitude, longitude);
            Log.d(TAG, "Adding park marker at: " + parkLocation);
            map.addMarker(new MarkerOptions()
                    .position(parkLocation)
                    .title(title));

            // Устанавливаем камеру на парк
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(parkLocation, 15));
        } catch (Exception e) {
            Log.e(TAG, "Error setting up map markers", e);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        map = null;
    }
} 