package com.example.ecologemoscow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecologemoscow.api.ApiClient;
import com.example.ecologemoscow.api.KudaGoApi;
import com.example.ecologemoscow.models.Route;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RoutesFragment extends Fragment {
    private RecyclerView recyclerView;
    private RoutesAdapter adapter;
    private List<Route> routes = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_routes, container, false);
        
        recyclerView = view.findViewById(R.id.routes_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new RoutesAdapter(routes);
        recyclerView.setAdapter(adapter);
        
        loadRoutes();
        
        return view;
    }

    private void loadRoutes() {
        KudaGoApi api = ApiClient.getKudaGoApi();
        api.getRoutes(
            "msk", // location
            "walking-tours,excursions", // categories
            "id,title,description,images,place,price,duration,distance", // fields
            "place", // expand
            20 // page_size
        ).enqueue(new Callback<List<Route>>() {
            @Override
            public void onResponse(Call<List<Route>> call, Response<List<Route>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    routes.clear();
                    routes.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Ошибка загрузки маршрутов", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Route>> call, Throwable t) {
                Toast.makeText(getContext(), "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
} 