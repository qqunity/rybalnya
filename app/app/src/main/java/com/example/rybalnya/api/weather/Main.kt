package com.example.rybalnya.api.weather

//Unit Default: Kelvin, Metric: Celsius, Imperial: Fahrenheit.
data class Main(
    val feels_like: Double, //This temperature parameter accounts for the human perception of weather.
    val grnd_level: Int, //Atmospheric pressure on the ground level, hPa
    val humidity: Int, //Humidity, %
    val pressure: Int, //Atmospheric pressure on the sea level by default, hPa
    val sea_level: Int, // Atmospheric pressure on the sea level, hPa
    val temp: Double, //Temperature.
    val temp_kf: Double, //Internal parameter
    val temp_max: Double, //Maximum temperature at the moment of calculation.
    val temp_min: Double //Minimum temperature at the moment of calculation.
)