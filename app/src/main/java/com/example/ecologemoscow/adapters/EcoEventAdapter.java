package com.example.ecologemoscow.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecologemoscow.R;
import com.example.ecologemoscow.models.EcoEvent;
import java.util.List;

public class EcoEventAdapter extends RecyclerView.Adapter<EcoEventAdapter.ViewHolder> {
    private List<EcoEvent> events;

    public EcoEventAdapter(List<EcoEvent> events) {
        this.events = events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_eco_event, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EcoEvent event = events.get(position);
        holder.title.setText(event.getTitle());
        holder.date.setText(event.getDate());
        holder.description.setText(event.getDescription());
        holder.location.setText(event.getLocation());
        holder.link.setText(event.getLink());

        holder.itemView.setOnClickListener(v -> {
            int visibility = holder.description.getVisibility() == View.GONE ? View.VISIBLE : View.GONE;
            holder.description.setVisibility(visibility);
            holder.location.setVisibility(visibility);
            holder.link.setVisibility(visibility);
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public void setEvents(List<EcoEvent> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, date, description, location, link;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.event_title);
            date = itemView.findViewById(R.id.event_date);
            description = itemView.findViewById(R.id.event_description);
            location = itemView.findViewById(R.id.event_location);
            link = itemView.findViewById(R.id.event_link);
        }
    }
} 