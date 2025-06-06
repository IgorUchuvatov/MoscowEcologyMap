package com.example.ecologemoscow.charts;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.ecologemoscow.R;
import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChartsContainerFragment extends Fragment {
    private static final String TAG = "ChartsContainerFragment";
    private String districtName;
    private String defaultChart;
    private Random random = new Random();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            districtName = getArguments().getString("district_name", "Южное Бутово");
            defaultChart = getArguments().getString("default_chart", "dust");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_charts_container, container, false);
        

        TextView districtNameView = view.findViewById(R.id.district_name);
        if (districtNameView != null) {
            districtNameView.setText(districtName);
        }
        

        ImageButton closeButton = view.findViewById(R.id.close_button);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            });
        }
        

        Button dustButton = view.findViewById(R.id.dust_button);
        Button radiationButton = view.findViewById(R.id.radiation_button);
        Button airButton = view.findViewById(R.id.air_button);
        Button precipitationButton = view.findViewById(R.id.precipitation_button);


        dustButton.setOnClickListener(v -> showDustChart());
        radiationButton.setOnClickListener(v -> showRadiationChart());
        airButton.setOnClickListener(v -> showAirChart());
        precipitationButton.setOnClickListener(v -> showPrecipitationChart());

        // Показываем график по умолчанию
        switch (defaultChart) {
            case "dust":
                showDustChart();
                break;
            case "radiation":
                showRadiationChart();
                break;
            case "air":
                showAirChart();
                break;
            case "precipitation":
                showPrecipitationChart();
                break;
            default:
                showDustChart();
        }

        return view;
    }

    private void showDustChart() {
        try {
            DustChartFragment fragment = new DustChartFragment();
            fragment.setChartTitle("Уровень пыли в " + districtName);
            fragment.setData(generateData(30, 70));
            showChart(fragment);
        } catch (Exception e) {
            Log.e(TAG, "showDustChart: Ошибка при отображении графика пыли: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Ошибка при отображении графика пыли: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showRadiationChart() {
        try {
            RadiationChartFragment fragment = new RadiationChartFragment();
            fragment.setChartTitle("Уровень радиации в " + districtName);
            fragment.setData(generateData(0.1f, 0.2f));
            showChart(fragment);
        } catch (Exception e) {
            Log.e(TAG, "showRadiationChart: Ошибка при отображении графика радиации: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Ошибка при отображении графика радиации: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showAirChart() {
        try {
            AirChartFragment fragment = new AirChartFragment();
            fragment.setChartTitle("Качество воздуха в " + districtName);
            fragment.setData(generateData(50, 150));
            showChart(fragment);
        } catch (Exception e) {
            Log.e(TAG, "showAirChart: Ошибка при отображении графика качества воздуха: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Ошибка при отображении графика качества воздуха: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showPrecipitationChart() {
        try {
            PrecipitationChartFragment fragment = new PrecipitationChartFragment();
            fragment.setChartTitle("Уровень осадков в " + districtName);
            fragment.setData(generateData(0, 10));
            showChart(fragment);
        } catch (Exception e) {
            Log.e(TAG, "showPrecipitationChart: Ошибка при отображении графика осадков: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Ошибка при отображении графика осадков: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showChart(Fragment chartFragment) {
        try {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.chart_container, chartFragment);
            transaction.commit();
            Log.d(TAG, "showChart: График успешно отображен");
        } catch (Exception e) {
            Log.e(TAG, "showChart: Ошибка при отображении графика: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Ошибка при отображении графика: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private List<Entry> generateData(float min, float max) {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            float value = min + random.nextFloat() * (max - min);
            entries.add(new Entry(i, value));
        }
        return entries;
    }
} 