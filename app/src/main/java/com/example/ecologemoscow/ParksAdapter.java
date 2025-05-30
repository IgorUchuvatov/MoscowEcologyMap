package com.example.ecologemoscow;

import android.content.Context;
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
        try {
            Park park = parks.get(position);
            Log.d(TAG, "Binding park at position " + position + ": " + park.name);
            
            holder.nameTextView.setText(park.name);
            holder.descriptionTextView.setText(park.description);
            
            holder.cardView.setOnClickListener(v -> {
                try {
                    if (context instanceof FragmentActivity) {
                        MapFragment mapFragment = MapFragment.newInstance(
                            park.latitude,
                            park.longitude,
                            park.name
                        );
                        
                        ((FragmentActivity) context).getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, mapFragment)
                            .addToBackStack("parks_list")
                            .commit();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error opening map for park: " + park.name, e);
                    Toast.makeText(context, "Не удалось открыть карту", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Error binding park at position " + position, e);
        }
    }

    @Override
    public int getItemCount() {
        return parks.size();
    }

    static class ParkViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView nameTextView;
        TextView descriptionTextView;

        ParkViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            nameTextView = itemView.findViewById(R.id.park_name);
            descriptionTextView = itemView.findViewById(R.id.park_description);
        }
    }
} 