package com.example.rybalnya.api.weather

data class City(
    val coord: Coord,
    val country: String, //Country code (GB, JP, RU etc.)
    val id: Int, //City ID
    val name: String, //City name
    val population: Int,
    val sunrise: Int, //Sunrise time
    val sunset: Int, //Sunset time
    val timezone: Int //Shift in seconds from UTC
)