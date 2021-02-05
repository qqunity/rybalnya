package com.example.rybalnya.api.weather

data class Weather(
    val description: String, //Weather condition within the group.
    val icon: String, //Weather icon id
    val id: Int, //Weather condition id
    val main: String //Group of weather parameters (Rain, Snow, Extreme etc.)
)