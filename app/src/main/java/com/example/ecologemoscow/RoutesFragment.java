package com.example.ecologemoscow;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class RoutesFragment extends Fragment {
    private static final String TAG = "RoutesFragment";
    private RecyclerView routesRecyclerView;
    private EcologicalRoutesAdapter routesAdapter;
    private List<EcologicalRoute> routes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routes, container, false);
        
        routesRecyclerView = view.findViewById(R.id.routes_recycler_view);
        
        if (routesRecyclerView == null) {
            Log.e(TAG, "RecyclerView is null");
            return view;
        }
        
        routesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        routes = new ArrayList<>();
        routesAdapter = new EcologicalRoutesAdapter(getContext(), routes);
        routesRecyclerView.setAdapter(routesAdapter);
        
        loadEcologicalRoutes();
        
        return view;
    }
    
    private void loadEcologicalRoutes() {
        Log.d(TAG, "Loading ecological routes");
        try {
            InputStream is = getContext().getAssets().open("ecological_routes.json");
            InputStreamReader reader = new InputStreamReader(is);
            Type listType = new TypeToken<ArrayList<EcologicalRoute>>(){}.getType();
            List<EcologicalRoute> loadedRoutes = new Gson().fromJson(reader, listType);
            if (loadedRoutes != null) {
                this.routes.clear();
                this.routes.addAll(loadedRoutes);
                fetchRatingsAndSort();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading ecological routes", e);
        }
    }

    private void fetchRatingsAndSort() {
        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("route_ratings");
        ratingsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, RouteRating> ratingsMap = new HashMap<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    RouteRating rating = snapshot.getValue(RouteRating.class);
                    if (rating != null) {
                        ratingsMap.put(snapshot.getKey(), rating);
                    }
                }

                for (EcologicalRoute route : routes) {
                    String routeId = route.link.replace("/", "_").replace(".", "_");
                    RouteRating rating = ratingsMap.get(routeId);
                    if (rating != null && rating.ratingCount > 0) {
                        route.ratingCount = rating.ratingCount;
                        route.averageRating = (float) (rating.totalScore / rating.ratingCount);
                    }
                }

                Collections.sort(routes, (r1, r2) -> Float.compare(r2.averageRating, r1.averageRating));
                routesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read ratings.", error.toException());
                routesAdapter.notifyDataSetChanged(); // Показать хотя бы без рейтинга
            }
        });
    }

    public static class RouteRating {
        public double totalScore = 0;
        public int ratingCount = 0;
        public RouteRating() {}
    }
} 