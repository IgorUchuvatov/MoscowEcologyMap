package com.example.ecologemoscow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecologemoscow.models.Route;
import java.util.List;

public class RoutesAdapter extends RecyclerView.Adapter<RoutesAdapter.RouteViewHolder> {
    private List<Route> routes;

    public RoutesAdapter(List<Route> routes) {
        this.routes = routes;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_route, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        Route route = routes.get(position);
        holder.titleTextView.setText(route.getTitle());
        holder.descriptionTextView.setText(route.getDescription());
        holder.addressTextView.setText(route.getAddress());
        
        if (route.getDuration() != null) {
            holder.durationTextView.setText(route.getDuration());
        }
        
        if (route.getDistance() != null) {
            holder.distanceTextView.setText(route.getDistance());
        }

        // Обработка нажатия на элемент
        holder.itemView.setOnClickListener(v -> {
            if (route.getLatitude() != 0 && route.getLongitude() != 0) {
                ((MainActivity) holder.itemView.getContext())
                    .showPlaceOnMap(route.getLatitude(), route.getLongitude(), route.getTitle());
            }
        });
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView descriptionTextView;
        TextView addressTextView;
        TextView durationTextView;
        TextView distanceTextView;

        RouteViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.route_title);
            descriptionTextView = itemView.findViewById(R.id.route_description);
            addressTextView = itemView.findViewById(R.id.route_address);
            durationTextView = itemView.findViewById(R.id.route_duration);
            distanceTextView = itemView.findViewById(R.id.route_distance);
        }
    }
} 