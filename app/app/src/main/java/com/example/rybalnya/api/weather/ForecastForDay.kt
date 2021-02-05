package com.example.rybalnya.api.weather

data class ForecastForDay(
    var forecastForHours: ArrayList<ForecastForHour>,
    var isExpanded: Boolean = false
)