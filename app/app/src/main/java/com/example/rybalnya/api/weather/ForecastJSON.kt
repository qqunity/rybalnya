package com.example.rybalnya.api.weather

data class ForecastJSON(
    val city: City,
    val cnt: Int, //A number of timestamps returned in the API response
    val cod: String, //Internal parameter
    val list: ArrayList<ForecastForHour>,
    val message: Int //Internal parameter
)