package com.example.rybalnya.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.rybalnya.FORECAST_CLOSED
import com.example.rybalnya.FORECAST_EXPANDED
import com.example.rybalnya.R
import com.example.rybalnya.api.weather.ForecastForDay
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.random.Random


class WeatherRecyclerAdapter(
    private val mContext: Context,
    private val weatherByDays: ArrayList<ForecastForDay>
) :
    RecyclerView.Adapter<WeatherRecyclerAdapter.ForecastVH>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastVH {
        val view: View = LayoutInflater.from(parent.context).inflate(
            R.layout.forecast_item,
            parent,
            false
        )
        return ForecastVH(view)
    }

    override fun onBindViewHolder(
        holder: ForecastVH,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isNotEmpty()) {
            when (payloads[0]) {
                FORECAST_EXPANDED -> {
                    holder.viewPager2.setCurrentItem(0, true)
                    holder.maxTemp.text =
                        (weatherByDays[position].forecastForHours[0].main.temp_max.toInt()
                            .toString() + "°")
                    holder.minTemp.text =
                        (weatherByDays[position].forecastForHours[0].main.temp_min.toInt()
                            .toString() + "°")
                }
                FORECAST_CLOSED -> {
                    holder.maxTemp.text = (weatherByDays[position].forecastForHours.maxBy {
                        it.main.temp
                    }?.main?.temp?.toInt().toString() + "°")

                    holder.minTemp.text = (weatherByDays[position].forecastForHours.minBy {
                        it.main.temp
                    }?.main?.temp?.toInt().toString() + "°")
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: ForecastVH, position: Int) {
        val forecastForDay = weatherByDays[position]

        holder.expandableLayout.visibility =
            if (forecastForDay.isExpanded) View.VISIBLE else View.GONE

        holder.staticLayout.setOnClickListener {
            forecastForDay.isExpanded = !forecastForDay.isExpanded
            if (forecastForDay.isExpanded) {
                holder.expandableLayout.visibility = View.VISIBLE
                notifyItemChanged(position, FORECAST_EXPANDED)
            } else {
                holder.expandableLayout.visibility = View.GONE
                notifyItemChanged(position, FORECAST_CLOSED)
            }
        }

        val `when`: Long = (forecastForDay.forecastForHours[0].dt.toLong() - 10800) * 1000

        holder.date.text = DateUtils.formatDateTime(
            mContext,
            `when`, DateUtils.FORMAT_SHOW_DATE
        )
        holder.day.text = DateUtils.formatDateTime(
            mContext,
            `when`, DateUtils.FORMAT_SHOW_WEEKDAY
        )

        holder.maxTemp.text = (forecastForDay.forecastForHours.maxBy {
            it.main.temp
        }?.main?.temp?.toInt().toString() + "°")

        holder.minTemp.text = (forecastForDay.forecastForHours.minBy {
            it.main.temp
        }?.main?.temp?.toInt().toString() + "°")

        holder.probability.text = (Random.nextInt(11, 62).toString() + "%")

        val name =
            ("ic_" + forecastForDay.forecastForHours.getOrElse(4) { forecastForDay.forecastForHours[0] }.weather[0].icon)
        val imageResource: Int =
            mContext.resources.getIdentifier(
                name,
                "drawable",
                mContext.packageName
            )
        val res: Drawable? = ContextCompat.getDrawable(mContext, imageResource)
        holder.icon.setImageDrawable(res)

        for (hour in forecastForDay.forecastForHours) {
            val title = DateUtils.formatDateTime(
                mContext,
                (hour.dt.toLong() - 10800) * 1000,
                DateUtils.FORMAT_SHOW_TIME
            )
            holder.tabLayout.addTab(holder.tabLayout.newTab())
        }

        holder.viewPager2.adapter = WeatherPagerAdapter(mContext, forecastForDay.forecastForHours)
        TabLayoutMediator(holder.tabLayout, holder.viewPager2) { tab, pagerPosition ->
            tab.text = DateUtils.formatDateTime(
                mContext,
                (forecastForDay.forecastForHours[pagerPosition].dt.toLong() - 10800) * 1000,
                DateUtils.FORMAT_SHOW_TIME
            )
        }.attach()
        holder.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                holder.maxTemp.text =
                    (forecastForDay.forecastForHours[position].main.temp_max.toInt()
                        .toString() + "°")
                holder.minTemp.text =
                    (forecastForDay.forecastForHours[position].main.temp_min.toInt()
                        .toString() + "°")
                super.onPageSelected(position)
            }
        })

    }

    override fun getItemCount(): Int {
        return weatherByDays.size
    }

    class ForecastVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var staticLayout: ConstraintLayout = itemView.findViewById(R.id.forecast_brief_info)
        var date: TextView = itemView.findViewById(R.id.forecast_day)
        var day: TextView = itemView.findViewById(R.id.forecast_day_of_week)
        var maxTemp: TextView = itemView.findViewById(R.id.forecast_temp_max)
        var minTemp: TextView = itemView.findViewById(R.id.forecast_temp_min)
        var probability: TextView = itemView.findViewById(R.id.forecast_fishing_prob)
        var icon: ImageView = itemView.findViewById(R.id.forecast_weather_icon)
        var expandableLayout: ConstraintLayout =
            itemView.findViewById(R.id.forecast_expandable_info)
        var tabLayout: TabLayout = itemView.findViewById(R.id.tabs_hours)
        var viewPager2: ViewPager2 = itemView.findViewById(R.id.forecast_pages)
    }
}