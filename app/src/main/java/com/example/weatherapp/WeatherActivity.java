package com.example.weatherapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;


public class WeatherActivity extends AppCompatActivity implements WeatherAdapter.OnItemClicked , WeatherTask.OnTaskCompleted , LocationTaskFinder.OnLocationCompleted {

    private RecyclerView weatherRecycler;
    private WeatherAdapter weatherAdapter;
    private ArrayList<Day> dayList = new ArrayList<>();
    private EditText searchEditText;
    private TreeMap<Day, ArrayList<Day>> hashMapOfDaysData;
    private ProgressBar progressBar;
    private View mainFragmentView;
    private ScrollView scrollView;


    private boolean mTrackingLocation;

    private FloatingActionButton locationFloatingButton;
    private LoactionFinderUtil loactionFinderUtil;

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_activity);

        WeatherFragment weatherFragment = WeatherFragment.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_weather_container, weatherFragment).commit();



    }

    public void initFragment(View rootView){
        mainFragmentView = rootView;

        searchEditText = mainFragmentView.findViewById(R.id.search_editText);
        progressBar = mainFragmentView.findViewById(R.id.findingLocationPBar);
        scrollView = mainFragmentView.findViewById(androidx.appcompat.R.id.scrollView);


        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(searchEditText!= null && searchEditText.getText()!= null && searchEditText.getText().toString().length()>0){
                        progressBar.setVisibility(View.VISIBLE);
                        new WeatherTask(WeatherActivity.this,WeatherActivity.this).execute(searchEditText.getText().toString());
                    }
                    return true;
                }
                return false;
            }
        });


        loactionFinderUtil = new LoactionFinderUtil((Context) WeatherActivity.this,(Activity) WeatherActivity.this, WeatherActivity.this);
        locationFloatingButton = mainFragmentView.findViewById(R.id.floatingActionButton);
        locationFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                loactionFinderUtil.trackLocation();
            }
        });


    }

    private void setRecycler(List<Day> dayList) {
        weatherRecycler = mainFragmentView.findViewById(R.id.all_days_weather_values);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        weatherRecycler.setLayoutManager(layoutManager);
        weatherAdapter = new WeatherAdapter(this, dayList, WeatherActivity.this);
        weatherRecycler.setAdapter(weatherAdapter);
    }

    @Override
    public void OnItemClicked(Day day) {

        ((TextView)mainFragmentView.findViewById(R.id.name_of_day_main)).setText(new SimpleDateFormat("dd/MM").format(day.getDay()) + " in " + day.getCityName());
        ((TextView)mainFragmentView.findViewById(R.id.name_of_day_top)).setText(new SimpleDateFormat("dd/MM").format(day.getDay()) + " in " + day.getCityName());

        ((ImageView)mainFragmentView.findViewById(R.id.image_of_details_main)).setImageResource(day.getImageUrl());
        ((ImageView)mainFragmentView.findViewById(R.id.image_of_details_top)).setImageResource(day.getImageUrl());

        ((TextView)mainFragmentView.findViewById(R.id.name_weather_value_main)).setText(day.getWeatherDescription());
        ((TextView)mainFragmentView.findViewById(R.id.name_weather_value_top)).setText(day.getWeatherDescription());

        ((TextView)mainFragmentView.findViewById(R.id.temperature_value_main)).setText(day.getTemperature().toString() + "°C");
        ((TextView)mainFragmentView.findViewById(R.id.temperature_value)).setText(day.getTemperature().toString() + "°C");

        ((TextView)mainFragmentView.findViewById(R.id.humidity_value_main)).setText(day.getHumidity().toString() + "%");
        ((TextView)mainFragmentView.findViewById(R.id.pressure_value_main)).setText(day.getPressure().toString() + "hPa");
        ((TextView)mainFragmentView.findViewById(R.id.wind_value_main)).setText(day.getWind().toString() + "m/s");
    }
    @Override
    public void onResume() {
        super.onResume();
        if(hashMapOfDaysData!=null && hashMapOfDaysData.size() > 0)
            setRecycler(new ArrayList<>(hashMapOfDaysData.keySet()));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onTaskCompleted(String resultJson) {
        if(resultJson == null || resultJson.length() == 0){
            ((TextView)findViewById(R.id.name_of_day_top)).setText("Location doesn't Exist");
            return;
        }

        try {
            JSONObject jsonResponse = new JSONObject(resultJson);
            JSONArray jsonArray = jsonResponse.getJSONArray("list");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObjectList = jsonArray.getJSONObject(i);

                JSONArray jsonWeather = jsonObjectList.getJSONArray("weather");

                JSONObject jsonObjectWeather = jsonWeather.getJSONObject(0);

                JSONObject jsonObjectMain = jsonObjectList.getJSONObject("main");
                JSONObject jsonObjectWind = jsonObjectList.getJSONObject("wind");

                int id = jsonObjectWeather.getInt("id");
                String weatherDescription = jsonObjectWeather.getString("main");
                double temperature = Math.round(jsonObjectMain.getDouble("temp")*10.)/10.;
                int humidity = jsonObjectMain.getInt("humidity");
                double wind = Math.round(jsonObjectWind.getDouble("speed")*10.)/10.;
                String cityName = jsonResponse.getJSONObject("city").getString("name");

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date day = formatter.parse(jsonObjectList.getString("dt_txt"));
                int pressure = jsonObjectMain.getInt("pressure");

                Integer imageUrl = R.drawable.cloudy;

                if (id < 212)
                    imageUrl = R.drawable.lightning;
                if (id < 300)
                    imageUrl = R.drawable.thunderstorm;
                else if (id < 400)
                    imageUrl = R.drawable.drizzle;
                else if (id < 600)
                    imageUrl = R.drawable.rain;
                else if (id < 700)
                    imageUrl = R.drawable.snow;
                else if (id < 800)
                    imageUrl = R.drawable.none;
                else if (id == 800)
                    imageUrl = R.drawable.sunny;
                else if (id == 801 || id == 802)
                    imageUrl = R.drawable.sunwithclouds;
                else if (id == 803 || id == 804)
                    imageUrl = R.drawable.cloud;

                dayList.add(new Day(cityName,day,weatherDescription,imageUrl,humidity,pressure,wind,temperature));
            }
            if(dayList.size() > 0){
                makeHashMapOfDaysData();
                setRecycler(new ArrayList<>(hashMapOfDaysData.keySet()));
                OnItemClicked(dayList.get(0));
                progressBar.setVisibility(View.GONE);
            }
            dayList = new ArrayList<>();

        } catch (JSONException e) {
            e.printStackTrace();
        }catch (ParseException e){
            e.printStackTrace();
        }
    }

    void makeHashMapOfDaysData(){
        hashMapOfDaysData = new TreeMap<>();
        if(dayList.size()>1) {
            Day keyDay = dayList.get(0);
            for (int i = 1; i < dayList.size(); i++) {
               if(keyDay.getDay().getDay() == dayList.get(i).getDay().getDay()){
                    if(hashMapOfDaysData.containsKey(keyDay)){
                        hashMapOfDaysData.get(keyDay).add(dayList.get(i));
                    }else {
                        ArrayList<Day> list = new ArrayList();
                        list.add(dayList.get(i));
                        hashMapOfDaysData.put(keyDay,list);
                    }
               }else
                   keyDay = dayList.get(i);

            }
        }else
            hashMapOfDaysData.put(dayList.get(0),new ArrayList<>());
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    loactionFinderUtil.trackLocation();
                else
                    Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show();
                break;
        }
    }


    private void updateInfo() {

    }


    @Override
    public void OnLocationCompleted(String result) {
        ((TextView)mainFragmentView.findViewById(R.id.name_of_day_top)).setText(result);
        progressBar.setVisibility(View.GONE);
    }
}
