package com.sage.thelimitbreaker.aqimeter.models

data class AirQualityObject(
        val datetime:String,
        val country_name:String,
        val breezometer_aqi:Int,
        val breezometer_description:String,
        val country_aqi:Int,
        val country_description:String,
        val dominant_pollutant_canonical_name:String,
        val dominant_pollutant_description:String,
        val dominant_pollutant_text:DominantPollutantDesc,
        val pollutants:Pollutants,
        val random_recommendations:RandomRecommendations
)