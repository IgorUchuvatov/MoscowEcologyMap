package com.example.ecologemoscow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ParkDetailsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_park_details, container, false);

        Bundle args = getArguments();
        if (args != null) {
            TextView nameView = view.findViewById(R.id.park_name);
            TextView descriptionView = view.findViewById(R.id.park_description);
            TextView cleanlinessView = view.findViewById(R.id.cleanliness_index);

            nameView.setText(args.getString("name", ""));
            descriptionView.setText(args.getString("description", ""));
            
            int cleanlinessIndex = args.getInt("cleanlinessIndex", 0);
            cleanlinessView.setText("Индекс чистоты: " + cleanlinessIndex + "/10");

            // Установка цвета в зависимости от индекса чистоты
            int color;
            if (cleanlinessIndex >= 8) {
                color = getResources().getColor(android.R.color.holo_green_dark, null);
            } else if (cleanlinessIndex >= 4) {
                color = getResources().getColor(android.R.color.holo_orange_dark, null);
            } else {
                color = getResources().getColor(android.R.color.holo_red_dark, null);
            }
            cleanlinessView.setTextColor(color);
        }

        return view;
    }
} 