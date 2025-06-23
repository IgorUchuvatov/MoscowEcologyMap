package com.example.ecologemoscow;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecologemoscow.adapters.EcoEventAdapter;
import com.example.ecologemoscow.models.EcoEvent;
import com.example.ecologemoscow.utils.EcoEventParser;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class EventsFragment extends Fragment {
    private RecyclerView recyclerView;
    private EcoEventAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        recyclerView = view.findViewById(R.id.recycler_view_events);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EcoEventAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        FloatingActionButton btnProfile = view.findViewById(R.id.btn_profile);
        btnProfile.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new FirebaseEventsFragment())
                .addToBackStack(null)
                .commit();
        });

        // Загрузка событий в отдельном потоке
        new Thread(() -> {
            List<EcoEvent> events = EcoEventParser.fetchEcoEvents();
            requireActivity().runOnUiThread(() -> adapter.setEvents(events));
        }).start();

        return view;
    }
}
