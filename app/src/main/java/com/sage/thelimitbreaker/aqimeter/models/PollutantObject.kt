package com.sage.thelimitbreaker.aqimeter.models

data class PollutantObject(
        val pollutant_description:String,
        val units:String,
        val concentration:Double
)