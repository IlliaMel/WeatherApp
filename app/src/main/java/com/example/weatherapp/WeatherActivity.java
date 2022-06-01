package com.example.weatherapp;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;


public class WeatherActivity extends AppCompatActivity implements WeatherAdapter.OnItemClicked , WeatherTask.OnTaskCompleted {

    private RecyclerView weatherRecycler;
    private WeatherAdapter weatherAdapter;
    private ArrayList<Day> dayList = new ArrayList<>();
    private EditText searchEditText;
    private TreeMap<Day, ArrayList<Day>> hashMapOfDaysData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_fragment);

        searchEditText = findViewById(R.id.search_editText);

        searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if(searchEditText!= null && searchEditText.getText()!= null && searchEditText.getText().toString().length()>0)
                        new WeatherTask(WeatherActivity.this,WeatherActivity.this).execute(searchEditText.getText().toString());
                    return true;
                }
                return false;
            }
        });

    }

    private void setRecycler(List<Day> dayList) {
        weatherRecycler = findViewById(R.id.all_days_weather_values);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false);
        weatherRecycler.setLayoutManager(layoutManager);
        weatherAdapter = new WeatherAdapter(this, dayList, WeatherActivity.this);
        weatherRecycler.setAdapter(weatherAdapter);
    }

    @Override
    public void OnItemClicked(Day day) {

        ((TextView)findViewById(R.id.name_of_day_main)).setText(new SimpleDateFormat("dd/MM").format(day.getDay()) + " in " + day.getCityName());
        ((TextView)findViewById(R.id.name_of_day_top)).setText(new SimpleDateFormat("dd/MM").format(day.getDay()) + " in " + day.getCityName());

        ((ImageView)findViewById(R.id.image_of_details_main)).setImageResource(day.getImageUrl());
        ((ImageView)findViewById(R.id.image_of_details_top)).setImageResource(day.getImageUrl());

        ((TextView)findViewById(R.id.name_weather_value_main)).setText(day.getWeatherDescription());
        ((TextView)findViewById(R.id.name_weather_value_top)).setText(day.getWeatherDescription());

        ((TextView)findViewById(R.id.temperature_value_main)).setText(day.getTemperature().toString() + "°C");
        ((TextView)findViewById(R.id.temperature_value)).setText(day.getTemperature().toString() + "°C");

        ((TextView)findViewById(R.id.humidity_value_main)).setText(day.getHumidity().toString() + "%");
        ((TextView)findViewById(R.id.pressure_value_main)).setText(day.getPressure().toString() + "hPa");
        ((TextView)findViewById(R.id.wind_value_main)).setText(day.getWind().toString() + "m/s");
    }
    @Override
    public void onResume() {
        super.onResume();
        if(hashMapOfDaysData!=null && hashMapOfDaysData.size() > 0)
            setRecycler(new ArrayList<>(hashMapOfDaysData.keySet()));
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
}
