package com.example.weatherapp;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.ArrayList;
import java.util.List;



public class WeatherActivity extends AppCompatActivity implements WeatherAdapter.OnTaskCompleted {

    RecyclerView weatherRecycler;
    WeatherAdapter weatherAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_fragment);

        List<Day> dayList = new ArrayList<>();
        dayList.add(new Day("Monday", "Snow", R.drawable.snow , 32 ,6.5 , 4.1, 20));
        dayList.add(new Day("Tuesday", "Rain", R.drawable.rain, 83 ,3.5 , 6.2, 23));
        dayList.add(new Day("Wednesday", "Sunny", R.drawable.sunny, 72 ,2.5 , 1.5, 17));
        dayList.add(new Day("Thursday", "Cloudy", R.drawable.sunwithclouds, 16 ,1.5 , 4.2, 15));
        dayList.add(new Day("Friday", "Storm", R.drawable.thunderstorm, 23 ,1.5 , 2.2, 22));
        dayList.add(new Day("Saturday", "Lightning", R.drawable.lightning, 77 ,2.5 , 2.7, 12));
        dayList.add(new Day("Sunday", "Rain", R.drawable.rain, 77 ,2.5 , 2.7, 12));
        setRecycler(dayList);
        onTaskCompleted(dayList.get(0));
        //Day day = (Day) getIntent().getSerializableExtra("WEATHER_DESCRIPTION");




    }

    private void setRecycler(List<Day> dayList) {
        weatherRecycler = findViewById(R.id.all_days_weather_values);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        weatherRecycler.setLayoutManager(layoutManager);
        weatherAdapter = new WeatherAdapter(this, dayList, WeatherActivity.this);
        weatherRecycler.setAdapter(weatherAdapter);
    }


    @Override
    public void onTaskCompleted(Day day) {

        ((TextView)findViewById(R.id.name_of_day_main)).setText(day.getDay());
        ((TextView)findViewById(R.id.name_of_day_top)).setText(day.getDay());

        ((ImageView)findViewById(R.id.image_of_details_main)).setImageResource(day.getImageUrl());
        ((ImageView)findViewById(R.id.image_of_details_top)).setImageResource(day.getImageUrl());

        ((TextView)findViewById(R.id.name_weather_value_main)).setText(day.getWeatherDescription());
        ((TextView)findViewById(R.id.name_weather_value_top)).setText(day.getWeatherDescription());

        ((TextView)findViewById(R.id.temperature_value_main)).setText(day.getTemperature().toString() + "°C");
        ((TextView)findViewById(R.id.temperature_value)).setText(day.getTemperature().toString() + "°C");

        ((TextView)findViewById(R.id.humidity_value_main)).setText(day.getHumidity().toString() + "%");
        ((TextView)findViewById(R.id.visibility_value_main)).setText(day.getVisibility().toString() + "km");
        ((TextView)findViewById(R.id.wind_value_main)).setText(day.getWind().toString() + "m/s");
    }
}
