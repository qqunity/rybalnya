package com.example.rybalnya.api.weather

data class ForecastForHour(
    val clouds: Clouds,
    val dt: Int, //Time of data forecasted, unix, UTC
    val dt_txt: String,
    val main: Main,
    val pop: Double, //Probability of precipitation
    val sys: Sys,
    val visibility: Int, //Average visibility, metres
    val weather: ArrayList<Weather>,
    val wind: Wind,
    val rain: Rain,
    val snow: Snow
)

//in rain and snow their parameter is actually called 3h, but we can't use that name
//So, idk if it will work or not. TODO("Check if it actually works")
data class Rain(
    val h: Double //Rain volume for last 3 hours, mm

)

data class Snow(
    val h: Double //Snow volume for last 3 hours
)

data class Coord(
    val lat: Double, //City geo location, latitude
    val lon: Double //City geo location, longitude
)

data class Clouds(
    val all: Int //Cloudiness, %
)

data class Sys(
    val pod: String //Part of the day (n - night, d - day)
)

data class Wind(
    val deg: Int, //Wind direction, degrees (meteorological)
    val speed: Double // Wind speed. Unit Default: meter/sec, Metric: meter/sec, Imperial: miles/hour.
)