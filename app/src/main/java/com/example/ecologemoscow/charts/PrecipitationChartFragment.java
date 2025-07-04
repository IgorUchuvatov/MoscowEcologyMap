package com.example.ecologemoscow.charts;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ecologemoscow.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.List;

public class PrecipitationChartFragment extends BaseChartFragment {
    private static final String TAG = "PrecipitationChartFragment";
    private static final String[] HOURS = {"00:00", "03:00", "06:00", "09:00", "12:00", "15:00", "18:00", "21:00"};
    private LineChart chart;

    public PrecipitationChartFragment() {
        this.chartTitle = "Уровень осадков";
        this.chartColor = Color.CYAN;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Создание представления фрагмента");
        View view = inflater.inflate(R.layout.fragment_precipitation_chart, container, false);
        
        try {
            // Инициализация графика
            chart = view.findViewById(R.id.chart);
            if (chart == null) {
                Log.e(TAG, "onCreateView: График не найден");
                return view;
            }

            // Настройка графика
            chart.setDrawGridBackground(false);
            chart.getDescription().setEnabled(false);
            chart.setTouchEnabled(true);
            chart.setDragEnabled(true);
            chart.setScaleEnabled(true);
            chart.setPinchZoom(true);
            chart.setDrawBorders(false);

            setupChart();
            Log.d(TAG, "onCreateView: График успешно инициализирован");
        } catch (Exception e) {
            Log.e(TAG, "onCreateView: Ошибка при инициализации: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Ошибка инициализации: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        
        return view;
    }

    @Override
    protected void setupChart() {
        try {
            // Создаем тестовые данные
            List<Entry> entries = new ArrayList<>();
            for (int i = 0; i < HOURS.length; i++) {
                entries.add(new Entry(i, (float) (Math.random() * 10))); // Осадки в мм
            }

            LineDataSet dataSet = new LineDataSet(entries, "Уровень осадков");
            dataSet.setColor(Color.BLUE);
            dataSet.setValueTextColor(Color.BLACK);
            dataSet.setDrawCircles(true);
            dataSet.setDrawValues(true);
            dataSet.setLineWidth(2f);
            dataSet.setCircleRadius(4f);
            dataSet.setCircleColor(Color.BLUE);
            dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);

            // Настройка осей
            XAxis xAxis = chart.getXAxis();
            xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
            xAxis.setDrawGridLines(false);
            xAxis.setValueFormatter(new IndexAxisValueFormatter(HOURS));

            chart.getAxisRight().setEnabled(false);
            chart.getAxisLeft().setDrawGridLines(true);
            chart.getAxisLeft().setGridColor(Color.LTGRAY);
            
            // Настройка легенды
            chart.getLegend().setEnabled(true);
            chart.getLegend().setTextColor(Color.BLACK);
            
            // Анимация
            chart.animateX(1000);
            
            chart.invalidate();
            Log.d(TAG, "setupChart: График успешно настроен");
        } catch (Exception e) {
            Log.e(TAG, "setupChart: Ошибка при настройке графика: " + e.getMessage(), e);
            Toast.makeText(getContext(), "Ошибка настройки графика: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_precipitation_chart;
    }

    @Override
    protected int getChartId() {
        return R.id.chart;
    }
} 