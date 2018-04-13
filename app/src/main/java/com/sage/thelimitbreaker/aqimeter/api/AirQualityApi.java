package com.sage.thelimitbreaker.aqimeter.api;

import com.sage.thelimitbreaker.aqimeter.models.AirQualityObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface AirQualityApi {
    @GET("/baqi/")
    Call<AirQualityObject>getAQIDetail(
            @Query("key")String key,
            @Query("lat")double lat,
            @Query("lon")double lon
    );
}
