package com.example.ecologemoscow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ecologemoscow.models.EcoEvent;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;

public class EventDetailsFragment extends Fragment {
    private static final String ARG_EVENT = "event";
    private Event event;
    private EcoEvent ecoEvent;

    public static EventDetailsFragment newInstance(EcoEvent event) {
        EventDetailsFragment fragment = new EventDetailsFragment();
        Bundle args = new Bundle();
        args.putString("eco_title", event.getTitle());
        args.putString("eco_date", event.getDate());
        args.putString("eco_link", event.getLink());
        args.putString("eco_description", event.getDescription());
        args.putString("eco_location", event.getLocation());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null && getArguments().containsKey("eco_title")) {
            ecoEvent = new EcoEvent(
                getArguments().getString("eco_title"),
                getArguments().getString("eco_date"),
                getArguments().getString("eco_link"),
                getArguments().getString("eco_description"),
                getArguments().getString("eco_location")
            );
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView titleTextView = view.findViewById(R.id.event_title);
        TextView timeTextView = view.findViewById(R.id.event_time);
        TextView addressTextView = view.findViewById(R.id.event_address);
        TextView descriptionTextView = view.findViewById(R.id.event_description);

        if (ecoEvent != null) {
            titleTextView.setText(ecoEvent.getTitle());
            timeTextView.setText(ecoEvent.getDate());
            addressTextView.setText(ecoEvent.getLocation());
            descriptionTextView.setText(ecoEvent.getDescription());
        }

        TextView linkTextView = new TextView(getContext());
        linkTextView.setId(View.generateViewId());
        linkTextView.setText(ecoEvent != null ? ecoEvent.getLink() : "");
        linkTextView.setAutoLinkMask(Linkify.WEB_URLS);
        linkTextView.setMovementMethod(LinkMovementMethod.getInstance());
        ((ViewGroup) view.findViewById(R.id.event_description).getParent()).addView(linkTextView);
    }
} 