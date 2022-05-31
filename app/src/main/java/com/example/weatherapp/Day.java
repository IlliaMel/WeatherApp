package com.example.weatherapp;

import java.io.Serializable;

public class Day implements Serializable {

    String day, weatherDescription;
    Integer imageUrl , humidity , temperature;
    Double visibility , wind;

    public Day(String day, String weatherDescription, Integer imageUrl, Integer humidity, Double visibility, Double wind , Integer temperature ) {
        this.day = day;
        this.weatherDescription = weatherDescription;
        this.imageUrl = imageUrl;
        this.humidity = humidity;
        this.visibility = visibility;
        this.wind = wind;
        this.temperature = temperature;
    }

    public String getDay() {
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

    public Double getVisibility() {return visibility; }

    public Double getWind() {return wind;   }

    public Integer getTemperature() {
        return temperature;
    }


}
