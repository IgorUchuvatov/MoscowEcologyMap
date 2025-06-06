package com.example.ecologemoscow.charts;

import android.graphics.Color;
import com.example.ecologemoscow.R;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import java.util.List;

public class HumidityChartFragment extends BaseChartFragment {
    private static final String TAG = "HumidityChartFragment";

    public HumidityChartFragment() {
        this.chartTitle = "Уровень влажности";
        this.chartColor = Color.CYAN;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_humidity_chart;
    }

    @Override
    protected int getChartId() {
        return R.id.chart;
    }


} 