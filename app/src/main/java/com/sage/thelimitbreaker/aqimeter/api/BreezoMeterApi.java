package com.sage.thelimitbreaker.aqimeter.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BreezoMeterApi {
    static BreezoMeterApi apiInstance;
    Retrofit retrofitInstance;
    AirQualityApi airQualityApi;

    public AirQualityApi getAirQualityApi() {
        return airQualityApi;
    }

    public static BreezoMeterApi getApiInstance(){
        if(apiInstance==null){
            apiInstance=new BreezoMeterApi();
        }

        return apiInstance;
    }
    private BreezoMeterApi(){
        retrofitInstance=new Retrofit.Builder()
                .baseUrl("https://api.breezometer.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        airQualityApi=retrofitInstance.create(AirQualityApi.class);
    }
}
