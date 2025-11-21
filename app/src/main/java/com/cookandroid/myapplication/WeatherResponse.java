package com.cookandroid.myapplication;

import java.util.List;

public class WeatherResponse {

    public List<Weather> weather;
    public Main main;

    public static class Weather {
        public String main;     // Clear, Rain, Clouds 등
        public String description;
    }

    public static class Main {
        public double temp;
    }
}