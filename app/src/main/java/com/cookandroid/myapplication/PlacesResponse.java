package com.cookandroid.myapplication;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PlacesResponse {

    @SerializedName("status")
    public String status;

    @SerializedName("html_attributions")
    public List<String> htmlAttributions;

    @SerializedName("results")
    public List<Result> results;

    public List<Result> getResults() { return results; }
    public String getStatus() { return status; }

    public static class Result {
        @SerializedName("name")
        public String name;

        @SerializedName("geometry")
        public Geometry geometry;

        public String getName() { return name; }
        public Geometry getGeometry() { return geometry; }
    }

    public static class Geometry {
        @SerializedName("location")
        public Location location;

        public Location getLocation() { return location; }
    }

    public static class Location {
        @SerializedName("lat")
        public double lat;

        @SerializedName("lng")
        public double lng;

        public double getLat() { return lat; }
        public double getLng() { return lng; }
    }
}