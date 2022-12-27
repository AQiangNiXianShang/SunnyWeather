package com.sunnyweather.android.ui.weather

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.InputDevice
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.card.MaterialCardView
import com.sunnyweather.android.R
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class WeatherActivity : AppCompatActivity() {
    val viewModel by lazy { ViewModelProvider(this)[WeatherViewModel::class.java] }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //融合状态栏
        val decorView = window.decorView
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT


        setContentView(R.layout.activity_weather)

        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }
        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }

        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            val swipeRefresh =
                findViewById(R.id.swipeRefresh) as androidx.swiperefreshlayout.widget.SwipeRefreshLayout
            swipeRefresh.isRefreshing = false
        })
        val swipeRefresh =
            findViewById(R.id.swipeRefresh) as androidx.swiperefreshlayout.widget.SwipeRefreshLayout
        swipeRefresh.setColorSchemeResources(com.google.android.material.R.color.design_default_color_primary)
        refreshWeather()
        swipeRefresh.setOnRefreshListener {
            refreshWeather()
            Toast.makeText(this, "刷新完成", Toast.LENGTH_SHORT).show()
        }
//        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)

        val navButton: Button = findViewById(R.id.navButton)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
        navButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        drawerLayout.addDrawerListener(
            object : DrawerLayout.DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                }

                override fun onDrawerStateChanged(newState: Int) {
                }

                override fun onDrawerOpened(drawerView: View) {
                }

                override fun onDrawerClosed(drawerView: View) {
                    val manager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    manager.hideSoftInputFromWindow(
                        drawerView.windowToken,
                        InputMethodManager.HIDE_NOT_ALWAYS
                    )
                }
            }
        )
    }

    @SuppressLint("WrongViewCast")
    private fun showWeatherInfo(weather: Weather) {
        val placeName: TextView = findViewById(R.id.placeName)
        placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily

        //填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()} ℃"
        val currentTemp: TextView = findViewById(R.id.currentTemp)
        currentTemp.text = currentTempText
        val currentSky: TextView = findViewById(R.id.currentSky)
        currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        val currentAQI: TextView = findViewById(R.id.currentAQI)
        currentAQI.text = currentPM25Text
        val nowLayout: RelativeLayout = findViewById(R.id.nowLayout)
        nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        //填充forecast.xml布局中的数据
        val forecastLayout: LinearLayout = findViewById(R.id.forecastLayout)
        forecastLayout.removeAllViews()
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skyCon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view =
                LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false)
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skyCon.date)
            val sky = getSky(skyCon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            forecastLayout.addView(view)
        }

        //填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        val coleRiskText: TextView = findViewById(R.id.coldRiskText)
        coleRiskText.text = lifeIndex.coldRisk[0].desc
        val dressingText: TextView = findViewById(R.id.dressingText)
        dressingText.text = lifeIndex.dressing[0].desc
        val ultravioletText: TextView = findViewById(R.id.ultravioletText)
        ultravioletText.text = lifeIndex.ultraviolet[0].desc
        val carWashingText: TextView = findViewById(R.id.carWashingText)
        carWashingText.text = lifeIndex.carWashing[0].desc
        val weatherLayout: ScrollView = findViewById(R.id.weatherLayout)
        weatherLayout.visibility = View.VISIBLE
    }

    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        val swipeRefresh =
            findViewById(R.id.swipeRefresh) as androidx.swiperefreshlayout.widget.SwipeRefreshLayout
        swipeRefresh.isRefreshing = true
    }
}