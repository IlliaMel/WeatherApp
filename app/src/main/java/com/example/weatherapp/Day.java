package com.example.weatherapp;

import java.io.Serializable;
import java.util.Date;

public class Day implements Serializable , Comparable<Day> {

    Date day;
    String weatherDescription , cityName = "Your Location";
    Integer imageUrl , humidity, pressure;
    Double  wind,temperature;

    public Day(Date day, String weatherDescription, Integer imageUrl, Integer humidity, Integer pressure, Double wind , Double temperature ) {
        this.day = day;
        this.weatherDescription = weatherDescription;
        this.imageUrl = imageUrl;
        this.humidity = humidity;
        this.pressure = pressure;
        this.wind = wind;
        this.temperature = temperature;
    }

    public Day(String cityName, Date day, String weatherDescription, Integer imageUrl, Integer humidity, Integer pressure, Double wind , Double temperature ) {
        this(day, weatherDescription, imageUrl, humidity, pressure, wind, temperature);
        this.cityName = cityName;
    }

    public Date getDay() {
        return day;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public Integer getImageUrl() {
        return imageUrl;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public Integer getPressure() {return pressure; }

    public Double getWind() {return wind;   }

    public Double getTemperature() {
        return temperature;
    }

    public String getCityName() {
        return cityName;
    }


    @Override
    public int compareTo(Day day) {
        return this.day.compareTo(day.getDay());
    }
}
