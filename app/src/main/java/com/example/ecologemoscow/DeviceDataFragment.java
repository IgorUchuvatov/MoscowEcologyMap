package com.example.ecologemoscow;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.ArrayList;
import java.util.List;

public class DeviceDataFragment extends Fragment {
    private static final String ARG_DEVICE_ID = "device_id";
    private static final String ARG_DEVICE_NAME = "device_name";
    private LineChart temperatureChart;
    private LineChart humidityChart;
    private LineChart airQualityChart;

    public static DeviceDataFragment newInstance(String deviceId, String deviceName) {
        DeviceDataFragment fragment = new DeviceDataFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DEVICE_ID, deviceId);
        args.putString(ARG_DEVICE_NAME, deviceName);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_data, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        temperatureChart = view.findViewById(R.id.temperature_chart);
        humidityChart = view.findViewById(R.id.humidity_chart);
        airQualityChart = view.findViewById(R.id.air_quality_chart);

        setupCharts();
        loadDemoData();
    }

    private void setupCharts() {
        setupChart(temperatureChart, "Температура (°C)", Color.RED);
        setupChart(humidityChart, "Влажность (%)", Color.BLUE);
        setupChart(airQualityChart, "Качество воздуха (ppm)", Color.GREEN);
    }

    private void setupChart(LineChart chart, String label, int color) {
        chart.getDescription().setEnabled(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.setDrawGridBackground(false);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);

        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisRight().setEnabled(false);
        chart.getLegend().setEnabled(true);
    }

    private void loadDemoData() {
        // Демо-данные для графиков
        List<Entry> temperatureEntries = new ArrayList<>();
        List<Entry> humidityEntries = new ArrayList<>();
        List<Entry> airQualityEntries = new ArrayList<>();

        // Генерируем случайные данные для демонстрации
        for (int i = 0; i < 24; i++) {
            temperatureEntries.add(new Entry(i, (float) (20 + Math.random() * 10)));
            humidityEntries.add(new Entry(i, (float) (40 + Math.random() * 30)));
            airQualityEntries.add(new Entry(i, (float) (100 + Math.random() * 200)));
        }

        LineDataSet temperatureDataSet = new LineDataSet(temperatureEntries, "Температура");
        temperatureDataSet.setColor(Color.RED);
        temperatureDataSet.setCircleColor(Color.RED);
        temperatureDataSet.setDrawValues(false);

        LineDataSet humidityDataSet = new LineDataSet(humidityEntries, "Влажность");
        humidityDataSet.setColor(Color.BLUE);
        humidityDataSet.setCircleColor(Color.BLUE);
        humidityDataSet.setDrawValues(false);

        LineDataSet airQualityDataSet = new LineDataSet(airQualityEntries, "Качество воздуха");
        airQualityDataSet.setColor(Color.GREEN);
        airQualityDataSet.setCircleColor(Color.GREEN);
        airQualityDataSet.setDrawValues(false);

        temperatureChart.setData(new LineData(temperatureDataSet));
        humidityChart.setData(new LineData(humidityDataSet));
        airQualityChart.setData(new LineData(airQualityDataSet));

        temperatureChart.invalidate();
        humidityChart.invalidate();
        airQualityChart.invalidate();
    }
} 