package com.sage.thelimitbreaker.aqimeter.models

data class Pollutants(
        val co:PollutantObject,
        val no2:PollutantObject,
        val o3:PollutantObject,
        val pm10:PollutantObject,
        val pm25:PollutantObject,
        val so2:PollutantObject
)