package com.sb.todaytravel.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sb.todaytravel.R
import com.sb.todaytravel.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val travelHistories by historyViewModel.allTravelHistories.collectAsState()

    Box(
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6F)
                .background(
                    color = Color.White
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .weight(0.1F)
            ) {
                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterEnd),
                    onClick = {
                        if (travelHistories.isEmpty()) return@IconButton
                        historyViewModel.switchOrderType()
                    }
                ) {
                    Icon(painter = painterResource(id = R.drawable.baseline_sort_24), contentDescription = "")
                }
            }
            if (travelHistories.isEmpty()) {
                EmptyList()
            } else {
                LazyColumn(
                    modifier = Modifier.weight(0.9F),
                    userScrollEnabled = true,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(travelHistories.size) {
                        TravelHistoryItem(
                            travelHistoryWithLocations = travelHistories[it],
                            isBottomDividerEnabled = it!=travelHistories.lastIndex,
                            onClick = {
                                mainViewModel.updateMarkedTravelHistory(travelHistories[it])
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyList() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9F),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            text = stringResource(R.string.no_history),
            fontSize = 24.sp,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun TravelHistoryItem(
    travelHistoryWithLocations: TravelHistoryWithLocations,
    isBottomDividerEnabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.CenterVertically)
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .background(Color(0xFFEEEEEE), RoundedCornerShape(20))
                .clip(RoundedCornerShape(20))
                .clickable {
                    onClick.invoke()
                }
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .align(Alignment.CenterStart),
            text = if (travelHistoryWithLocations.travelLocations.isNotEmpty()) { travelHistoryWithLocations.travelLocations.first().let {
                SimpleDateFormat("yy.MM.dd.(E) a hh:mm", Locale("ko", "KR")).format(Date(it.arrivalTime))
            } + "부터\n" + if (travelHistoryWithLocations.travelLocations.size > 1) { travelHistoryWithLocations.travelLocations.last().let {
                SimpleDateFormat("yy.MM.dd.(E) a hh:mm", Locale("ko", "KR")).format(Date(it.arrivalTime))
            } + "까지의 여행" } else "" } else "",
            fontSize = 18.sp,
            textAlign = TextAlign.Start,
            lineHeight = TextUnit(28F, TextUnitType.Sp)
        )
    }
    if (isBottomDividerEnabled)Divider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
    )
}

@Preview
@Composable
fun Preview() {
    EmptyList()
}
