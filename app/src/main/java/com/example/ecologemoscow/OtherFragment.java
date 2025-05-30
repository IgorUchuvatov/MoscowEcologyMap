package com.example.ecologemoscow;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OtherFragment extends Fragment {
    private static final String TAG = "OtherFragment";
    private static final String CACHE_KEY = "eco_places_cache";
    
    private RecyclerView recyclerView;
    private EcoPlaceAdapter adapter;
    private List<EcoPlace> ecoPlaces = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView emptyView;
    private Button retryButton;
    private FirebaseFirestore db;
    private boolean isLoading = false;
    private int retryCount = 0;
    private static final int MAX_RETRIES = 3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Creating view");
        View view = inflater.inflate(R.layout.fragment_other, container, false);
        
        recyclerView = view.findViewById(R.id.recycler_view);
        progressBar = view.findViewById(R.id.progress_bar);
        emptyView = view.findViewById(R.id.empty_view);
        retryButton = view.findViewById(R.id.retry_button);
        
        if (recyclerView == null || progressBar == null || emptyView == null || retryButton == null) {
            Log.e(TAG, "Some views are null");
            return view;
        }
        
        Log.d(TAG, "Setting up RecyclerView");
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        ecoPlaces = new ArrayList<>();
        adapter = new EcoPlaceAdapter(ecoPlaces, place -> {
            if (getActivity() instanceof MainActivity) {
                try {
                    ((MainActivity) getActivity()).switchToMapFragment();
                    double latitude = Double.parseDouble(place.getLatitude());
                    double longitude = Double.parseDouble(place.getLongitude());
                    ((MainActivity) getActivity()).showPlaceOnMap(latitude, longitude, place.getName());
                } catch (NumberFormatException e) {
                    Log.e(TAG, "Error parsing coordinates: " + e.getMessage());
                    Toast.makeText(getContext(), "Ошибка: неверные координаты", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        Log.d(TAG, "Setting adapter to RecyclerView");
        recyclerView.setAdapter(adapter);
        
        db = FirebaseFirestore.getInstance();
        
        retryButton.setOnClickListener(v -> {
            if (!isLoading) {
                retryCount = 0;
                loadEcoPlaces();
            }
        });
        
        Log.d(TAG, "Starting to load eco places");
        loadEcoPlaces();
        
        return view;
    }
    
    private void loadEcoPlaces() {
        if (isLoading) return;
        
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);
        
        if (!isNetworkAvailable()) {
            Log.e(TAG, "No network connection available");
            Toast.makeText(getContext(), "Нет подключения к интернету", Toast.LENGTH_LONG).show();
            loadCachedData();
            return;
        }
        
        try {
            db.collection("eco_places")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Log.d(TAG, "Successfully loaded " + queryDocumentSnapshots.size() + " places from Firestore");
                            ecoPlaces.clear();
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                EcoPlace place = doc.toObject(EcoPlace.class);
                                ecoPlaces.add(place);
                            }
                            adapter.notifyDataSetChanged();
                            updateUI();
                        } else {
                            Log.d(TAG, "No data in Firestore, parsing from web");
                            parseEcoPlaces();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error loading data from Firestore: " + e.getMessage());
                        Toast.makeText(getContext(), "Ошибка загрузки данных: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        loadCachedData();
                        if (retryCount < MAX_RETRIES) {
                            retryCount++;
                            parseEcoPlaces();
                        } else {
                            isLoading = false;
                            showRetryButton();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Error accessing Firestore: " + e.getMessage());
            Toast.makeText(getContext(), "Ошибка доступа к базе данных", Toast.LENGTH_LONG).show();
            loadCachedData();
        }
    }
    
    private void parseEcoPlaces() {
        Log.d(TAG, "Starting to parse eco places");
        progressBar.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        retryButton.setVisibility(View.GONE);
        
        EcoPlaceParser parser = new EcoPlaceParser();
        parser.parseEcoPlaces(new EcoPlaceParser.OnParsingCompleteListener() {
            @Override
            public void onParsingComplete(List<EcoPlace> places) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d(TAG, "Parsing complete, got " + places.size() + " places");
                        ecoPlaces.clear();
                        ecoPlaces.addAll(places);
                        adapter.notifyDataSetChanged();
                        updateUI();
                        
                        if (!places.isEmpty()) {
                            saveToFirestore(places);
                        }
                        
                        isLoading = false;
                    });
                }
            }

            @Override
            public void onParsingError(String error) {
                Log.e(TAG, "Error parsing eco places: " + error);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Ошибка загрузки данных: " + error, Toast.LENGTH_SHORT).show();
                        loadCachedData();
                        if (retryCount < MAX_RETRIES) {
                            retryCount++;
                            parseEcoPlaces();
                        } else {
                            isLoading = false;
                            showRetryButton();
                        }
                    });
                }
            }
        });
    }
    
    private void updateUI() {
        Log.d(TAG, "Updating UI. Places count: " + ecoPlaces.size());
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        } else {
            Log.e(TAG, "Adapter is null in updateUI!");
        }
        
        if (recyclerView != null && emptyView != null) {
            if (ecoPlaces.isEmpty()) {
                Log.d(TAG, "Showing empty view");
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                Log.d(TAG, "Showing recycler view");
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        } else {
            Log.e(TAG, "recyclerView or emptyView is null!");
        }
        
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        } else {
            Log.e(TAG, "progressBar is null!");
        }
    }
    
    private void showRetryButton() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
        }
        if (retryButton != null) {
            retryButton.setVisibility(View.VISIBLE);
        }
    }
    
    private void loadCachedData() {
        Log.d(TAG, "Loading cached data");
        ecoPlaces.clear();
        ecoPlaces.addAll(EcoPlaceParser.getDemoData());
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        updateUI();
    }
    
    private void cacheData(List<EcoPlace> places) {
        // TODO: Implement caching
    }
    
    private boolean isNetworkAvailable() {
        Context context = getContext();
        if (context == null) return false;
        
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }
    
    private void loadDemoData() {
        Log.d(TAG, "Loading demo data");
        ecoPlaces.clear();
        List<EcoPlace> demoPlaces = EcoPlaceParser.getDemoData();
        Log.d(TAG, "Got " + demoPlaces.size() + " demo places");
        ecoPlaces.addAll(demoPlaces);
        if (adapter != null) {
            Log.d(TAG, "Notifying adapter of data change");
            adapter.notifyDataSetChanged();
        } else {
            Log.e(TAG, "Adapter is null!");
        }
        updateUI();
    }
    
    private void saveToFirestore(List<EcoPlace> places) {
        if (db == null) return;
        
        for (EcoPlace place : places) {
            db.collection("eco_places")
                    .add(place)
                    .addOnSuccessListener(documentReference -> Log.d(TAG, "Document added with ID: " + documentReference.getId()))
                    .addOnFailureListener(e -> Log.e(TAG, "Error adding document", e));
        }
    }
} 