package com.example.weatherapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;



public class WeatherTask extends AsyncTask<String, Void, String> {

    private Context mContext;
    private OnTaskCompleted mListener;
    private boolean withCity;

    public WeatherTask(Context mContext, OnTaskCompleted listener , boolean withCity) {
        this.mContext = mContext;
        this.withCity = withCity;
        mListener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        return NetworkUtils.getWeatherInfo(params[0],withCity);
    }

    @Override
    protected void onPostExecute(String weather) {
        mListener.onTaskCompleted(weather);
        super.onPostExecute(weather);
    }

    public interface OnTaskCompleted {
        void onTaskCompleted(String resultJson);
    }
}


class NetworkUtils {
    private static final String WEATHER_API = "https://api.openweathermap.org/data/2.5/forecast?";
    private static String appid = "412b97072a8f3fa87721cfc2b356b948";
// lat=22&lon=42
    static String getWeatherInfo(String queryString, boolean withCity) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String weatherJSONString = null;
            //  tempUrl = url + "?q=" + city + "," + country + "&appid=" + appid;
        try {

            //URL requestURL = new URL(WEATHER_API + "lat=" + lat + "&lon=" + lon + "&exclude=" + exclude + "&appid=" + appid);
            //https://api.openweathermap.org/data/2.5/weather?q=London,uk&units=metric&APPID=412b97072a8f3fa87721cfc2b356b948
          //https://api.openweathermap.org/data/2.5/onecall?lat=22&lon=42&units=metric&exclude=daily&appid=412b97072a8f3fa87721cfc2b356b948
//https://api.openweathermap.org/data/2.5/forecast?q=Kiev&appid=412b97072a8f3fa87721cfc2b356b948

            URL requestURL;
            if(withCity)
                requestURL = new URL(WEATHER_API + "q=" +  queryString + "&units=metric" + "&APPID=" + appid);
            else
                requestURL = new URL(WEATHER_API + "lat=" + queryString.split(" ")[0]  + "&lon=" + queryString.split(" ")[1] + "&units=metric" + "&APPID=" + appid);

            urlConnection = (HttpURLConnection) requestURL.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }

            if (builder.length() == 0)
                return null;


            weatherJSONString = builder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return weatherJSONString;
    }


}
