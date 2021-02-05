package com.example.rybalnya.ui.map

import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.text.format.DateUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rybalnya.R
import com.example.rybalnya.WEATHER_URL
import com.example.rybalnya.adapters.WeatherRecyclerAdapter
import com.example.rybalnya.api.ApiRequests
import com.example.rybalnya.api.weather.ForecastForDay
import com.example.rybalnya.api.weather.ForecastForHour
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.*
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.Runtime.getApplicationContext
import com.yandex.runtime.image.ImageProvider
import kotlinx.android.synthetic.main.fragment_map.*
import kotlinx.android.synthetic.main.map_bottom_sheet.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
import kotlin.collections.ArrayList


class MapFragment : Fragment(), UserLocationObjectListener {

    private lateinit var mapView: MapView
    private lateinit var userLocationLayer: UserLocationLayer
    private lateinit var standardBottomSheetBehavior: BottomSheetBehavior<View>
    private lateinit var currentPlacemark: PlacemarkMapObject
    private lateinit var weatherRecyclerView: RecyclerView
    private lateinit var mLayoutManager: LinearLayoutManager
    private lateinit var weatherAdapter: WeatherRecyclerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.setApiKey("2d6e6d99-30bb-4643-9e44-08137b20b07b")
        MapKitFactory.initialize(this.requireActivity())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_map, container, false)

    private lateinit var mapObjects: MapObjectCollection

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupStandardBottomSheet()
        setupMap()
        weatherRecyclerView = view.findViewById(R.id.recycler_view_weather)
        weatherRecyclerView.setHasFixedSize(true)
        mLayoutManager = LinearLayoutManager(context)
        weatherRecyclerView.layoutManager = mLayoutManager
    }

    override fun onObjectAdded(userLocationView: UserLocationView) {
        userLocationLayer.setAnchor(
            PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.5).toFloat()),
            PointF((mapView.width * 0.5).toFloat(), (mapView.height * 0.83).toFloat())
        )
        userLocationView.arrow.setIcon(
            ImageProvider.fromResource(
                this.requireActivity(), R.drawable.user_icon_map_30
            )
        )
        userLocationView.pin.setIcon(
            ImageProvider.fromResource(
                this.requireActivity(), R.drawable.user_icon_map_30
            )
        )
        userLocationView.accuracyCircle.fillColor = Color.CYAN
    }

    override fun onObjectRemoved(view: UserLocationView) {
    }

    override fun onObjectUpdated(view: UserLocationView, event: ObjectEvent) {
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
        MapKitFactory.getInstance().onStart()
    }

    private val inputListener: InputListener = object : InputListener {
        override fun onMapTap(map: Map, p1: Point) {
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        override fun onMapLongTap(map: Map, p1: Point) {
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            mapObjects.clear()
            val provider =
                ImageProvider.fromAsset(getApplicationContext(), "map_marker_centered.png")
            currentPlacemark = mapObjects.addPlacemark(p1, provider, IconStyle().setScale(.15f))
            //duration is in seconds, not ms!!!
            found_info.text = "Ищем информацию..."
            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            mapView.map.move(
                CameraPosition(
                    p1,
                    mapView.map.cameraPosition.zoom,
                    mapView.map.cameraPosition.azimuth,
                    mapView.map.cameraPosition.tilt
                ), Animation(Animation.Type.SMOOTH, 0.75f)
            ) {
                fillForecast(p1)
            }
        }
    }

    private fun setupMap() {
        mapView = map_view

        mapObjects = mapView.map.mapObjects

        mapView.map.isRotateGesturesEnabled = true
        mapView.map.move(CameraPosition(Point(0.0, 0.0), 14F, 0F, 0F))
        mapView.map.addInputListener(inputListener)
        val mapKit = MapKitFactory.getInstance()

        userLocationLayer = mapKit.createUserLocationLayer(mapView.mapWindow)
        userLocationLayer.isVisible = true
        userLocationLayer.isHeadingEnabled = true

        userLocationLayer.setObjectListener(this)

    }

    private fun setupStandardBottomSheet() {
        standardBottomSheetBehavior = BottomSheetBehavior.from(standardBottomSheet)
        standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
/*                found_info.text = when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> "STATE_EXPANDED ${currentPlacemark.geometry.latitude} + ${currentPlacemark.geometry.longitude}"
                    BottomSheetBehavior.STATE_COLLAPSED -> "Ищем информацию..."
                    BottomSheetBehavior.STATE_DRAGGING -> "STATE_DRAGGING"
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> "STATE_HALF_EXPANDED"
                    BottomSheetBehavior.STATE_HIDDEN -> "STATE_HIDDEN"
                    BottomSheetBehavior.STATE_SETTLING -> "STATE_SETTLING"
                    else -> null
                }*/
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        sheet_picker.setImageResource(R.drawable.ic_hide)
                        sheet_picker.isClickable = true
                        sheet_picker.setOnClickListener {
                            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                        }
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        sheet_picker.isClickable = false
                        sheet_picker.setImageResource(R.drawable.ic_drag_handle)
                        sheet_picker.setOnClickListener {}
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        sheet_picker.isClickable = true
                        sheet_picker.setImageResource(R.drawable.ic_drag_handle)
                        sheet_picker.setOnClickListener {
                            standardBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                        }
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        sheet_picker.isClickable = false
                        if (this@MapFragment::currentPlacemark.isInitialized) {
                            mapObjects.remove(currentPlacemark)
                        }
                        sheet_picker.setOnClickListener {}
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
            }
        }
        standardBottomSheetBehavior.addBottomSheetCallback(bottomSheetCallback)
        standardBottomSheetBehavior.saveFlags = BottomSheetBehavior.SAVE_ALL
        found_info.setTextColor(Color.parseColor("#FFFFFF"))
    }

    private fun fillForecast(p: Point) {
        var api: ApiRequests? = null
        try {
            api = Retrofit.Builder()
                .baseUrl(WEATHER_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiRequests::class.java)
        } catch (e: Exception) {
            Log.i("ApiRequest", e.toString())
        }
        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = api?.getForecast(p.latitude, p.longitude)?.awaitResponse()
                val city = response?.body()?.city?.name.toString()
//                val sunrise = (response?.body()?.city?.sunrise.toString().toLong() + response?.body()?.city?.timezone.toString().toLong())
                val sunrise = response?.body()?.city?.sunrise.toString().toLong() * 1000
                val sunset = response?.body()?.city?.sunset.toString().toLong() * 1000
                var initDate =
                    Date((response?.body()?.list?.get(0)?.dt!!.toLong() - 10800) * 1000).date
                val days = ArrayList<ForecastForDay>(5)
                val hours = ArrayList<ForecastForHour>()
                for (hour in response.body()?.list!!) {
                    if (Date((hour.dt.toLong() - 10800) * 1000).date != initDate) {
                        days.add(ForecastForDay(ArrayList(hours)))
                        initDate = Date((hour.dt.toLong() - 10800) * 1000).date
                        hours.clear()
                    }
                    hours.add(hour)
                }
                days.add(ForecastForDay(ArrayList(hours)))
                hours.clear()
                val description =
                    response.body()?.list?.get(0)?.weather?.get(0)?.description.toString()
                withContext(Dispatchers.Main) {
                    weatherAdapter =
                        WeatherRecyclerAdapter(this@MapFragment.requireActivity(), days)
                    weatherRecyclerView.adapter = weatherAdapter
                    probability.text = "73%"
                    found_info.text = (city + "\n" + description)
                    time_sunrise.text = DateUtils.formatDateTime(
                        context,
                        sunrise, DateUtils.FORMAT_SHOW_TIME
                    )
                    time_sunset.text = DateUtils.formatDateTime(
                        this@MapFragment.requireContext(),
                        sunset, DateUtils.FORMAT_SHOW_TIME
                    )
                }
            } catch (e: Exception) {
                Log.i("Forecast error", e.toString())
            }
        }
    }

}