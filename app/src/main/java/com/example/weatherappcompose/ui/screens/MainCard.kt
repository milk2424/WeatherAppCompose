package com.example.weatherappcompose.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherappcompose.R
import com.example.weatherappcompose.data.WeatherModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun MainCard(currentDay: MutableState<WeatherModel>, onClickSync: () -> Unit, onClickSearch: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp),
            colors = CardColors(Color.Black, Color.White, Color.Transparent, Color.Gray),
            elevation = CardDefaults.cardElevation(10.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 5.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currentDay.value.time,
                        style = TextStyle(
                            fontSize = 16.sp
                        ),
                        color = Color.White
                    )
                    AsyncImage(
                        model = "https:${currentDay.value.icon}",
                        contentDescription = "",
                        modifier = Modifier
                            .size(35.dp)
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentDay.value.city,
                        style = TextStyle(
                            fontSize = 26.sp,
                            fontWeight = Bold
                        ),
                        color = Color.White
                    )
                    Text(
                        text = if (currentDay.value.currentTemp.isNotEmpty()) "${currentDay.value.currentTemp}°C"
                        else "${currentDay.value.minTemp}°C/${currentDay.value.maxTemp}°C",
                        style = TextStyle(
                            fontSize = 46.sp
                        ),
                        color = Color.White,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                    )
                    Text(
                        text = currentDay.value.condition,
                        style = TextStyle(
                            fontSize = 20.sp
                        ),
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                onClickSearch.invoke()
                            },
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_search_24),
                                contentDescription = "",
                                Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = if (currentDay.value.currentTemp.isNotEmpty())
                                "${currentDay.value.minTemp}°C/${currentDay.value.maxTemp}°C"
                            else "",
                            style = TextStyle(
                                fontSize = 20.sp
                            ),
                            color = Color.White
                        )
                        IconButton(
                            onClick = { onClickSync.invoke()},
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.baseline_cloud_sync_24),
                                contentDescription = "",
                                Modifier.size(24.dp)
                            )
                        }
                    }

                }

            }


        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabLayout(daysList: MutableState<List<WeatherModel>>, currentDay: MutableState<WeatherModel>) {
    val pagerState = rememberPagerState()
    val tabIndex = pagerState.currentPage
    val coroutineScope = rememberCoroutineScope()
    val tabList = listOf("Hours", "Days")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 5.dp)
            .clip(RoundedCornerShape(5.dp))
    ) {
        TabRow(
            selectedTabIndex = tabIndex,
            indicator = { pos ->
                TabRowDefaults.Indicator(Modifier.tabIndicatorOffset(pos[tabIndex]))
            },
            containerColor = Color.Black
        ) {
            tabList.forEachIndexed { index, s ->
                Tab(
                    selected = false,
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.White,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(text = s) }
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            count = tabList.size,
            modifier = Modifier
                .weight(1f)
        ) { index ->
            val list = when (index) {
                0 -> getWeatherByHours(currentDay.value.hours)
                else -> daysList.value
            }
            WeatherList(list = list, currentDay = currentDay)
        }
    }
}

private fun getWeatherByHours(hours: String): List<WeatherModel> {
    if (hours.isNotEmpty()) {
        val hoursArray = JSONArray(hours)
        val list = ArrayList<WeatherModel>()
        for (i in 0 until hoursArray.length()) {
            val item = hoursArray[i] as JSONObject
            list.add(
                WeatherModel(
                    "",
                    item.getString("time"),
                    item.getString("temp_c").toFloat().toInt().toString() + "°C",
                    item.getJSONObject("condition").getString("text"),
                    item.getJSONObject("condition").getString("icon"),
                    "",
                    "",
                    ""
                )
            )
        }
        return list
    }
    return listOf()
}