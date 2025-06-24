package com.example.ecologemoscow;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EcologicalRoutesAdapter extends RecyclerView.Adapter<EcologicalRoutesAdapter.RouteViewHolder> {
    private static final String TAG = "EcologicalRoutesAdapter";
    private final List<EcologicalRoute> routes;
    private final Context context;

    public EcologicalRoutesAdapter(Context context, List<EcologicalRoute> routes) {
        this.context = context;
        this.routes = routes;
    }

    @NonNull
    @Override
    public RouteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteViewHolder holder, int position) {
        EcologicalRoute route = routes.get(position);
        holder.nameTextView.setText(route.name);
        holder.descriptionTextView.setText(route.description);
        holder.ratingBar.setRating(route.averageRating);

        holder.cardView.setOnClickListener(v -> {
            if (context instanceof androidx.fragment.app.FragmentActivity) {
                RouteMapFragment fragment = RouteMapFragment.newInstance(route);
                ((androidx.fragment.app.FragmentActivity) context).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }

    static class RouteViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView nameTextView;
        TextView descriptionTextView;
        RatingBar ratingBar;

        RouteViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            nameTextView = itemView.findViewById(R.id.route_name);
            descriptionTextView = itemView.findViewById(R.id.route_description);
            ratingBar = itemView.findViewById(R.id.route_rating_bar);
        }
    }
} 