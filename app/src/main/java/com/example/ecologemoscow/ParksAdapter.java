package com.example.ecologemoscow;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ParksAdapter extends RecyclerView.Adapter<ParksAdapter.ParkViewHolder> {
    private static final String TAG = "ParksAdapter";
    private final List<Park> parks;
    private final Context context;

    public ParksAdapter(Context context, List<Park> parks) {
        this.context = context;
        this.parks = parks;
        Log.d(TAG, "ParksAdapter initialized with " + parks.size() + " parks");
    }

    @NonNull
    @Override
    public ParkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_park, parent, false);
        return new ParkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ParkViewHolder holder, int position) {
        Park park = parks.get(position);
        holder.nameTextView.setText(park.name);
        holder.descriptionTextView.setText(park.description);
        holder.cleanlinessIndexView.setText("Чистота: " + park.cleanlinessIndex + "/10");
        
        // Установка цвета в зависимости от индекса чистоты
        int color;
        if (park.cleanlinessIndex >= 8) {
            color = holder.itemView.getContext().getColor(android.R.color.holo_green_dark);
        } else if (park.cleanlinessIndex >= 4) {
            color = holder.itemView.getContext().getColor(android.R.color.holo_orange_dark);
        } else {
            color = holder.itemView.getContext().getColor(android.R.color.holo_red_dark);
        }
        holder.cleanlinessIndexView.setTextColor(color);

        holder.cardView.setOnClickListener(v -> {
            if (context instanceof FragmentActivity) {
                ParkLocationFragment fragment = ParkLocationFragment.newInstance(
                        park.latitude,
                        park.longitude,
                        park.name
                );

                ((FragmentActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return parks.size();
    }

    static class ParkViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView nameTextView;
        TextView descriptionTextView;
        TextView cleanlinessIndexView;

        ParkViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            nameTextView = itemView.findViewById(R.id.park_name);
            descriptionTextView = itemView.findViewById(R.id.park_description);
            cleanlinessIndexView = itemView.findViewById(R.id.cleanliness_index);
        }
    }
} 