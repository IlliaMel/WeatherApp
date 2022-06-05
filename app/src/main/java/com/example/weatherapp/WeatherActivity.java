package com.example.weatherapp;

import android.app.Activity;
import android.content.Context;
import android.content.Entity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.renderer.XAxisRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.transition.MaterialSharedAxis;

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
    private ProgressBar progressBar, progressBarChart;
    private View mainFragmentView;
    private ScrollView scrollView;
    private BarChart weatherChart;
    private Button temperatureButton, humidityButton ,windButton;
    private Day defaultDay = new Day("City",new Date(),"None",R.drawable.none,0,0,0.,0.);
    private ArrayList<BarEntry> chartEntries;
    private FloatingActionButton locationFloatingButton;
    private LocationFinderUtil locationFinderUtil;

    private Day lastSelected;

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
        weatherChart = mainFragmentView.findViewById(R.id.weather_chart);
        temperatureButton = mainFragmentView.findViewById(R.id.temperature_button);
        humidityButton = mainFragmentView.findViewById(R.id.humidity_button);
        windButton = mainFragmentView.findViewById(R.id.wind_button);

        progressBarChart = mainFragmentView.findViewById(R.id.progressChart);
        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(searchEditText!= null && searchEditText.getText()!= null && searchEditText.getText().toString().length()>0){
                        progressBar.setVisibility(View.VISIBLE);
                        if(weatherRecycler!=null)
                            weatherRecycler.setVisibility(View.GONE);
                        new WeatherTask(WeatherActivity.this,WeatherActivity.this, true).execute(searchEditText.getText().toString().replaceAll(" "," "));
                    }
                    return true;
                }
                return false;
            }
        });


        locationFloatingButton = mainFragmentView.findViewById(R.id.floatingActionButton);
        locationFinderUtil = new LocationFinderUtil((Context) WeatherActivity.this,(Activity) WeatherActivity.this, WeatherActivity.this);
        locationFinderUtil.trackLocation();
        progressBar.setVisibility(View.VISIBLE);

        locationFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(weatherRecycler!=null)
                    weatherRecycler.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                locationFinderUtil = new LocationFinderUtil((Context) WeatherActivity.this,(Activity) WeatherActivity.this, WeatherActivity.this);
                locationFinderUtil.trackLocation();
            }
        });


        temperatureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hashMapOfDaysData != null && hashMapOfDaysData.size() > 0 && hashMapOfDaysData.containsKey(lastSelected)){
                   chartEntries = new ArrayList<>();
                    progressBarChart.setVisibility(View.VISIBLE);
                    weatherChart.setVisibility(View.GONE);
                    float temp = lastSelected.temperature.floatValue();
                    chartEntries.add(new BarEntry(lastSelected.day.getHours(),temp));
                    for (int i = 0; i < hashMapOfDaysData.get(lastSelected).size(); i++) {
                        chartEntries.add(new BarEntry(hashMapOfDaysData.get(lastSelected).get(i).day.getHours(),hashMapOfDaysData.get(lastSelected).get(i).temperature.floatValue()));
                    }
                    if(chartEntries.size() > 0){
                        progressBarChart.setVisibility(View.GONE);
                        weatherChart.setVisibility(View.VISIBLE);
                        setChart(chartEntries);
                    }

                }
            }
        });

        humidityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hashMapOfDaysData != null && hashMapOfDaysData.size() > 0 && hashMapOfDaysData.containsKey(lastSelected)){
                    chartEntries = new ArrayList<>();
                    progressBarChart.setVisibility(View.VISIBLE);
                    weatherChart.setVisibility(View.GONE);
                    float temp = lastSelected.humidity.floatValue();
                    chartEntries.add(new BarEntry(lastSelected.day.getHours(),temp));
                    for (int i = 0; i < hashMapOfDaysData.get(lastSelected).size(); i++) {
                        chartEntries.add(new BarEntry(hashMapOfDaysData.get(lastSelected).get(i).day.getHours(),hashMapOfDaysData.get(lastSelected).get(i).humidity.floatValue()));
                    }
                    if(chartEntries.size() > 0){
                        progressBarChart.setVisibility(View.GONE);
                        weatherChart.setVisibility(View.VISIBLE);
                        setChart(chartEntries);
                    }

                }
            }
        });

        windButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hashMapOfDaysData != null && hashMapOfDaysData.size() > 0 && hashMapOfDaysData.containsKey(lastSelected)){
                    chartEntries = new ArrayList<>();
                    progressBarChart.setVisibility(View.VISIBLE);
                    weatherChart.setVisibility(View.GONE);
                    float temp = lastSelected.wind.floatValue();
                    chartEntries.add(new BarEntry(lastSelected.day.getHours(),temp));
                    for (int i = 0; i < hashMapOfDaysData.get(lastSelected).size(); i++) {
                        chartEntries.add(new BarEntry(hashMapOfDaysData.get(lastSelected).get(i).day.getHours(),hashMapOfDaysData.get(lastSelected).get(i).wind.floatValue()));
                    }
                    if(chartEntries.size() > 0){
                        progressBarChart.setVisibility(View.GONE);
                        weatherChart.setVisibility(View.VISIBLE);
                        setChart(chartEntries);
                    }

                }
            }
        });


    }

    private void setChart(ArrayList<BarEntry> chartEntries){
        //Init line chart data set
        BarDataSet barDataSet = new BarDataSet(chartEntries,"");
        //Set color
        barDataSet.setGradientColor(getResources().getColor(R.color.light_blue_gradient),getResources().getColor(R.color.blue_gradient));
        //Hide draw value
        barDataSet.setDrawValues(true);
        barDataSet.setFormSize(0);
        barDataSet.setFormLineWidth(10f);
        BarData barData = new BarData(barDataSet);
        barData.setValueTextSize(8);


        weatherChart.getXAxis().setEnabled(true);
        weatherChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        weatherChart.getXAxis().setDrawGridLines(false);

        weatherChart.getXAxis().setLabelCount(8);

        weatherChart.getXAxis().setGranularityEnabled(true);
        weatherChart.getXAxis().setGranularity(3);
        weatherChart.getXAxis().setAxisMinimum(-1f);
        weatherChart.getXAxis().setAxisMaxValue(22.0f);


        weatherChart.getXAxis().setDrawAxisLine(false);
        weatherChart.setScaleEnabled(false);


        weatherChart.setData(barData);
        weatherChart.animateY(1000);
        weatherChart.getDescription().setText("");
        weatherChart.getAxisRight().setEnabled(false);
        weatherChart.getAxisLeft().setEnabled(false);

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

        String locationName;
        locationName = day.getCityName();
        if(locationName.length() > 15)
             locationName = day.getCityName().substring(0,15) + "...";


        ((TextView)mainFragmentView.findViewById(R.id.name_of_day_main)).setText(new SimpleDateFormat("dd/MM").format(day.getDay()) + " " + locationName.toString());
        ((TextView)mainFragmentView.findViewById(R.id.name_of_day_top)).setText(new SimpleDateFormat("dd/MM").format(day.getDay())  + " " +  locationName.toString());

        ((ImageView)mainFragmentView.findViewById(R.id.image_of_details_main)).setImageResource(day.getImageUrl());
        ((ImageView)mainFragmentView.findViewById(R.id.image_of_details_top)).setImageResource(day.getImageUrl());

        ((TextView)mainFragmentView.findViewById(R.id.name_weather_value_main)).setText(day.getWeatherDescription());
        ((TextView)mainFragmentView.findViewById(R.id.name_weather_value_top)).setText(day.getWeatherDescription());

        ((TextView)mainFragmentView.findViewById(R.id.temperature_value_main)).setText(day.getTemperature().toString() + "°C");
        ((TextView)mainFragmentView.findViewById(R.id.temperature_value)).setText(day.getTemperature().toString() + "°C");

        ((TextView)mainFragmentView.findViewById(R.id.humidity_value_main)).setText(day.getHumidity().toString() + "%");
        ((TextView)mainFragmentView.findViewById(R.id.pressure_value_main)).setText(day.getPressure().toString() + "hPa");
        ((TextView)mainFragmentView.findViewById(R.id.wind_value_main)).setText(day.getWind().toString() + "m/s");

        lastSelected = day;

        if(hashMapOfDaysData != null && hashMapOfDaysData.size() > 0 && hashMapOfDaysData.containsKey(lastSelected)){
            chartEntries = new ArrayList<>();
            progressBarChart.setVisibility(View.VISIBLE);
            weatherChart.setVisibility(View.GONE);
            float temp = lastSelected.temperature.floatValue();
            chartEntries.add(new BarEntry(lastSelected.day.getHours(),temp));
            for (int i = 0; i < hashMapOfDaysData.get(lastSelected).size(); i++) {
                chartEntries.add(new BarEntry(hashMapOfDaysData.get(lastSelected).get(i).day.getHours(),hashMapOfDaysData.get(lastSelected).get(i).temperature.floatValue()));
            }
            if(chartEntries.size() > 0){
                progressBarChart.setVisibility(View.GONE);
                weatherChart.setVisibility(View.VISIBLE);
                setChart(chartEntries);
            }

        }
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
    public void OnLocationCompleted(String [] cityLonLat) {

        new WeatherTask(WeatherActivity.this,WeatherActivity.this, false).execute(cityLonLat[1] + " " + cityLonLat[2]);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onTaskCompleted(String resultJson) {
        if(resultJson == null || resultJson.length() == 0){
            ((TextView)findViewById(R.id.name_of_day_top)).setText("Location doesn't Exist");
            setRecycler(new ArrayList<>());
            OnItemClicked(defaultDay);
            progressBar.setVisibility(View.GONE);
            if(weatherRecycler!=null)
                weatherRecycler.setVisibility(View.VISIBLE);
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
                weatherRecycler.setVisibility(View.VISIBLE);
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
            int counter = 0;
            for (int i = 1; i < dayList.size(); i++) {
               if(keyDay.getDay().getDay() == dayList.get(i).getDay().getDay()){
                   counter++;
                    if(hashMapOfDaysData.containsKey(keyDay)){
                        hashMapOfDaysData.get(keyDay).add(dayList.get(i));
                    }else {
                        ArrayList<Day> list = new ArrayList();
                        list.add(dayList.get(i));
                        hashMapOfDaysData.put(keyDay,list);
                    }
               }else if (counter > 0){
                   keyDay = dayList.get(i);
                   counter = 0;
               }

               else{
                   hashMapOfDaysData.put(keyDay,new ArrayList<>());
                   keyDay = dayList.get(i);
                   counter = 0;
               }


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
                    locationFinderUtil.trackLocation();
                else
                    Toast.makeText(this, R.string.location_permission_denied, Toast.LENGTH_SHORT).show();
                break;
        }
    }






}
