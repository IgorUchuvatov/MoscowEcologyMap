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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
                routesAdapter.notifyDataSetChanged();
                Log.d(TAG, "Loaded " + loadedRoutes.size() + " routes");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error loading ecological routes", e);
        }
    }
} 