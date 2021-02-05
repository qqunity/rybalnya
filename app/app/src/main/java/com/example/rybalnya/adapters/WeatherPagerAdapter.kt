package com.example.rybalnya.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.rybalnya.R
import com.example.rybalnya.api.weather.ForecastForHour

class WeatherPagerAdapter(
    private val mContext: Context,
    private val forecast: ArrayList<ForecastForHour>
) : RecyclerView.Adapter<WeatherPagerAdapter.PagerVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerVH {
        return PagerVH(
            LayoutInflater.from(parent.context).inflate(R.layout.weather_page, parent, false)
        )
    }

    override fun onBindViewHolder(holder: PagerVH, position: Int) {
        val hour = forecast[position]
        holder.windVelocity.text = (hour.wind.speed.toString() + "м/с")
        holder.windDirectionIcon.rotation = (hour.wind.deg.toInt() + 180).toFloat()
        holder.precipitationProbability.text = (hour.pop.toString() + "%")
        holder.humidity.text = (hour.main.humidity.toString() + "%")
        holder.pressure.text =
            ((hour.main.pressure.toInt() * (0.75006)).toInt().toString() + " мм.рт.ст.")
    }

    override fun getItemCount(): Int {
        return forecast.size
    }

    class PagerVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val windDirectionIcon: ImageView = itemView.findViewById(R.id.wind_direction)
        val windVelocity: TextView = itemView.findViewById(R.id.wind_velocity)
        val precipitationProbability: TextView =
            itemView.findViewById(R.id.precipitation_probability_percentage)
        val humidity: TextView = itemView.findViewById(R.id.humidity_percentage)
        val pressure: TextView = itemView.findViewById(R.id.pressure_value)
    }
}