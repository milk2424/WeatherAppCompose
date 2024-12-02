package com.example.weatherappcompose

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherappcompose.data.WeatherModel
import com.example.weatherappcompose.ui.screens.DialogSearch
import com.example.weatherappcompose.ui.screens.MainCard
import com.example.weatherappcompose.ui.screens.TabLayout
import com.example.weatherappcompose.ui.theme.BackgroundColor
import org.json.JSONObject

private const val API_KEY = "c042dd8c71a84cdda52111027240808"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            val currentDay = remember {
                mutableStateOf(WeatherModel("", "", "", "", "", "", "", ""))
            }
            val state = remember {
                mutableStateOf(listOf<WeatherModel>())
            }
            val dialogState = remember {
                mutableStateOf(false)
            }

            if (dialogState.value)
                DialogSearch(dialogState, onSubmit =
                { getData(it, state, currentDay, this) }
                )


            getData("Minsk", state, currentDay, this)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(BackgroundColor)
            ) {
                MainCard(currentDay,
                    onClickSync = {
                        getData("Minsk", state, currentDay, this@MainActivity)
                    },
                    onClickSearch = {
                        dialogState.value = true
                    })
                TabLayout(daysList = state, currentDay)
            }
        }
    }
}

private fun getData(
    city: String,
    state: MutableState<List<WeatherModel>>,
    currentDay: MutableState<WeatherModel>,
    context: Context
) {
    val url = "https://api.weatherapi.com/v1/forecast.json" +
            "?key=$API_KEY&" +
            "q=$city&" +
            "days=5&" +
            "aqi=no&" +
            "alerts=no"
    val queue = Volley.newRequestQueue(context)
    val stringRequest = StringRequest(
        Request.Method.GET,
        url,
        { response ->
            val list = getWeatherByDays(response)
            state.value = list
            currentDay.value = list[0]
        },
        { error ->

        }
    )
    queue.add(stringRequest)

}

fun getWeatherByDays(response: String): List<WeatherModel> {
    if (response.isNotEmpty()) {
        val list = ArrayList<WeatherModel>()
        val mainObject = JSONObject(response)
        val city = mainObject.getJSONObject("location").getString("name")
        val days = mainObject.getJSONObject("forecast").getJSONArray("forecastday")
        for (i in 0 until days.length()) {
            val item = days[i] as JSONObject
            list.add(
                WeatherModel(
                    city,
                    item.getString("date"),
                    "",
                    item
                        .getJSONObject("day")
                        .getJSONObject("condition")
                        .getString("text"),
                    item
                        .getJSONObject("day")
                        .getJSONObject("condition")
                        .getString("icon"),
                    item.getJSONObject("day").getString("maxtemp_c").toFloat().toInt().toString(),
                    item.getJSONObject("day").getString("mintemp_c").toFloat().toInt().toString(),
                    item.getJSONArray("hour").toString()
                )
            )
            Log.d("MyLog", list.toString())
        }
        list[0] = list[0].copy(
            time = mainObject.getJSONObject("current").getString("last_updated"),
            currentTemp = mainObject.getJSONObject("current").getString("temp_c").toFloat().toInt()
                .toString()
        )

        return list
    }
    return listOf()
}