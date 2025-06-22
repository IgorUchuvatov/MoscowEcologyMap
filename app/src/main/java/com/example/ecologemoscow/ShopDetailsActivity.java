package com.example.ecologemoscow;

import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class ShopDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_details);

        TextView nameView = findViewById(R.id.shop_details_name);
        TextView timeView = findViewById(R.id.shop_details_time);
        TextView descriptionView = findViewById(R.id.shop_details_description);

        String name = getIntent().getStringExtra("name");
        String openTime = getIntent().getStringExtra("openTime");
        String closeTime = getIntent().getStringExtra("closeTime");
        String description = getIntent().getStringExtra("description");

        nameView.setText(name);
        timeView.setText("Время работы: " + openTime + " - " + closeTime);
        descriptionView.setText(description);
    }
} 