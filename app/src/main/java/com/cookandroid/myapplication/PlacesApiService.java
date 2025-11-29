package com.cookandroid.myapplication;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface PlacesApiService {
    @GET("place/nearbysearch/json")
    Call<PlacesResponse> searchNearby(
            @Query("location") String location,
            @Query("radius") int radius,
            @Query("type") String keyword,
            @Query("key") String apiKey
    );
}
