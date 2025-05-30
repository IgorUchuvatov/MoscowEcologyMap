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
import java.util.ArrayList;
import java.util.List;

public class PlacesFragment extends Fragment {
    private static final String TAG = "PlacesFragment";
    private RecyclerView recyclerView;
    private ParksAdapter adapter;
    private List<Park> parks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "Creating view");
        View view = inflater.inflate(R.layout.fragment_places, container, false);
        
        recyclerView = view.findViewById(R.id.parks_recycler_view);
        if (recyclerView == null) {
            Log.e(TAG, "RecyclerView is null");
            return view;
        }
        
        Log.d(TAG, "Setting up RecyclerView");
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        parks = new ArrayList<>();
        adapter = new ParksAdapter(getContext(), parks);
        recyclerView.setAdapter(adapter);
        
        loadDemoData();
        
        return view;
    }
    
    private void loadDemoData() {
        Log.d(TAG, "Loading demo data");
        parks.clear();
        
        // Парк Горького
        parks.add(new Park(
            "Парк Горького",
            "/place/park-gorkogo/",
            "Центральный парк культуры и отдыха имени Горького - один из самых известных парков Москвы. Здесь можно покататься на велосипеде, поиграть в пляжный волейбол, посетить музеи и выставки.",
            55.7287,
            37.6038
        ));
        
        // Сокольники
        parks.add(new Park(
            "Сокольники",
            "/place/sokolniki/",
            "Парк Сокольники - один из старейших парков Москвы с богатой историей. Здесь есть спортивные площадки, велодорожки, роллердром и многое другое.",
            55.7897,
            37.6732
        ));
        
        // ВДНХ
        parks.add(new Park(
            "ВДНХ",
            "/place/vdnh/",
            "Выставка достижений народного хозяйства - крупнейший выставочный комплекс в России. Здесь можно увидеть уникальные павильоны, фонтаны и архитектурные памятники.",
            55.8297,
            37.6322
        ));
        
        // Коломенское
        parks.add(new Park(
            "Коломенское",
            "/place/kolomenskoe/",
            "Музей-заповедник Коломенское - бывшая царская резиденция с уникальными памятниками архитектуры. Здесь сохранились старинные церкви, дворцы и сады.",
            55.6667,
            37.6833
        ));
        
        // Царицыно
        parks.add(new Park(
            "Царицыно",
            "/place/tsaritsyno/",
            "Музей-заповедник Царицыно - дворцово-парковый ансамбль на юге Москвы. Здесь можно увидеть Большой дворец, Хлебный дом и другие архитектурные памятники.",
            55.6167,
            37.6833
        ));
        
        Log.d(TAG, "Added " + parks.size() + " demo parks");
        adapter.notifyDataSetChanged();
    }
} 