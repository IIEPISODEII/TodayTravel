package com.sb.todaytravel.feature.travel_history

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naver.maps.geometry.LatLng
import com.sb.todaytravel.R
import com.sb.todaytravel.feature.core.AcceptionDialog
import com.sb.todaytravel.feature.core.MainViewModel
import com.sb.todaytravel.feature.theme.TodayTravelDarkGray
import com.sb.todaytravel.feature.theme.TodayTravelBlue
import com.sb.todaytravel.feature.theme.TodayTravelGray
import com.sb.todaytravel.feature.theme.TodayTravelGreen
import com.sb.todaytravel.feature.theme.TodayTravelOrange
import com.sb.todaytravel.feature.theme.TodayTravelTeal
import com.sb.todaytravel.util.haversine
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
    onHistoryItemClick: (TravelHistoryWithLocations) -> Unit = { _ -> },
) {
    val travelHistories by historyViewModel.allTravelHistories.collectAsState()
    val orderType by historyViewModel.orderType.collectAsState()
    val lastTravelHistoryIndex by mainViewModel.lastTravelHistoryIndex.collectAsState()
    val isTraveling by mainViewModel.isTraveling.collectAsState()
    var dialogEvent by remember { mutableStateOf(DialogEvent.NONE) }
    var candidateTravelHistoryIndex by remember { mutableLongStateOf(-1L) }
    val orderTypeDirection by animateFloatAsState(
        targetValue = if (orderType == OrderType.Ascend) 180F else 0F,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = ""
    )
    val isDBLoading by historyViewModel.isLoading.collectAsState()

    Box(
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6F)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(
                        color = TodayTravelTeal,
                        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                    )
                    .padding(end = 24.dp)
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(vertical = 12.dp),
                    text = stringResource(R.string.title_history),
                    fontSize = 20.sp,
                    color = Color.Black,
                )
                IconButton(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .background(color = Color.Transparent, shape = CircleShape)
                        .clipToBounds()
                        .rotate(orderTypeDirection),
                    onClick = {
                        if (travelHistories.isEmpty()) return@IconButton
                        historyViewModel.switchOrderType()
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_sort_24),
                        contentDescription = ""
                    )
                }
            }

            if (travelHistories.isEmpty()) {
                EmptyList(
                    modifier = Modifier.weight(0.7F),
                    onClick = {
                        mainViewModel.setRandomDestination()
                        dialogEvent = DialogEvent.SET_TRAVEL
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(0.7F),
                    userScrollEnabled = true,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(travelHistories.size) {
                        val travelHistoryItem = travelHistories[it]
                        val travelState = when (travelHistoryItem.travelState) {
                            0 -> TravelState.COMPLETE
                            1 -> TravelState.FAIL
                            else -> TravelState.ON_PROGRESS
                        }
                        TravelHistoryItem(
                            travelHistoryWithLocations = travelHistoryItem,
                            isCurrentTravelingHistory = isTraveling && lastTravelHistoryIndex == travelHistoryItem.travelHistoryIndex,
                            travelState = travelState,
                            onClick = { onHistoryItemClick(travelHistoryItem) },
                            onLongClick = {
                                dialogEvent =
                                    if (mainViewModel.isTraveling.value && lastTravelHistoryIndex == travelHistoryItem.travelHistoryIndex) DialogEvent.CANCEL_TRAVEL
                                    else DialogEvent.DELETE_HISTORY
                                candidateTravelHistoryIndex = travelHistoryItem.travelHistoryIndex
                            }
                        )
                    }
                }
            }
        }
    }

    if (isDBLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Black.copy(alpha = 0.3F))
                .pointerInput(Unit) {},
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = TodayTravelGreen
            )
        }
    }

    if (dialogEvent != DialogEvent.NONE) {
        AcceptionDialog(
            question = when (dialogEvent) {
                DialogEvent.DELETE_HISTORY -> stringResource(id = R.string.question_travel_history_delete)
                DialogEvent.SET_TRAVEL -> stringResource(id = R.string.set_here_destination)
                DialogEvent.CANCEL_TRAVEL -> stringResource(id = R.string.question_travel_cancel)
                else -> ""
            },
            onDismissRequest = {
                dialogEvent = DialogEvent.NONE
            },
            onAccept = {
                when (dialogEvent) {
                    DialogEvent.DELETE_HISTORY -> historyViewModel.deleteTravelHistory(candidateTravelHistoryIndex)
                    DialogEvent.SET_TRAVEL -> mainViewModel.startTravel()
                    DialogEvent.CANCEL_TRAVEL -> mainViewModel.cancelTravel()
                    else -> {}
                }
                dialogEvent = DialogEvent.NONE
            }
        )
    }
}

@Composable
fun EmptyList(
    modifier: Modifier,
    onClick: () -> Unit = {}
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            text = stringResource(R.string.no_history),
            fontSize = 24.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .background(color = TodayTravelTeal, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    onClick()
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .wrapContentSize()
                .align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "새로운 여정",
                fontSize = 20.sp,
                color = Color.Black,
                textAlign = TextAlign.Center
            )
            Icon(imageVector = Icons.Default.Add, contentDescription = "")
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TravelHistoryItem(
    travelHistoryWithLocations: TravelHistoryWithLocations,
    isCurrentTravelingHistory: Boolean = false,
    travelState: TravelState = TravelState.COMPLETE,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }
    val ripple = rememberRipple(
        bounded = true,
        radius = 300.dp,
        color = Color(0xFF7F7F7F)
    )
    var repeatableDotAnimation by remember { mutableIntStateOf(0) }
    LaunchedEffect(key1 = repeatableDotAnimation, key2 = travelState) {
        if (travelState != TravelState.ON_PROGRESS) return@LaunchedEffect
        delay(1200L)
        repeatableDotAnimation = when (repeatableDotAnimation) {
            0 -> 1
            1 -> 2
            else -> 0
        }
    }
    val startTime =
        travelHistoryWithLocations.travelLocations.let {
            if (it.isEmpty()) ""
            else it.first().let {
                simpleDateFormat.format(Date(it.arrivalTime))
            } + "부터\n"
        }
    val endTime =
        if (!isCurrentTravelingHistory) {
            travelHistoryWithLocations.travelLocations.last().let {
                simpleDateFormat.format(Date(it.arrivalTime))
            } + "까지"
        } else "시작한 여정"
    val travelInfo =
        if (travelHistoryWithLocations.travelLocations.size >= 2 && travelState in arrayOf(TravelState.COMPLETE, TravelState.FAIL)) {
            val travelLocations = travelHistoryWithLocations.travelLocations
            val time = (travelLocations.last().arrivalTime - travelLocations.first().arrivalTime)/(60*1000L)
            val day = time/(24*60)
            val hour = (time%(24*60))/60
            val minute = (time%(24*60))%60

            val dayString = if (day == 0L) "" else day.toString() + "일 "
            val hourString = if (hour == 0L) "" else hour.toString() + "시간 "
            val minuteString = if (day == 0L && hour == 0L && minute == 0L) "0분" else if (minute == 0L) "" else minute.toString() + "분"

            var distance = 0.0
            for (i in travelLocations.indices) {
                if (i == 0) continue
                travelLocations[i].latitude - travelLocations[i-1].latitude
                distance += haversine(
                    start = LatLng(travelLocations[i-1].latitude, travelLocations[i-1].longitude),
                    destination = LatLng(travelLocations[i].latitude, travelLocations[i].longitude)
                )
            }
            val distanceWithDimension = if (distance >= 1000) "${(distance/10).roundToInt().toDouble()/100}km" else "${distance.roundToInt()}m"
            "$dayString$hourString$minuteString 동안 ${distanceWithDimension}의 여정"
        } else ""

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .background(TodayTravelGray, RoundedCornerShape(20))
            .clip(RoundedCornerShape(20))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = ripple,
                onLongClick = { onLongClick() },
                onClick = { onClick() }
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.wrapContentSize()) {
            Text(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(align = Alignment.CenterVertically),
                text = startTime,
                fontSize = 14.sp,
                textAlign = TextAlign.Start,
                lineHeight = TextUnit(8F, TextUnitType.Sp)
            )
            Text(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(align = Alignment.CenterVertically),
                text = endTime,
                fontSize = 14.sp,
                textAlign = TextAlign.Start,
                lineHeight = TextUnit(8F, TextUnitType.Sp)
            )
            Text(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(align = Alignment.CenterVertically),
                text = travelInfo,
                fontSize = 20.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Start,
                lineHeight = TextUnit(8F, TextUnitType.Sp)
            )
        }
        if (travelState == TravelState.ON_PROGRESS) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .align(Alignment.CenterVertically)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .drawWithContent {
                            when (repeatableDotAnimation) {
                                0 -> {
                                    drawCircle(
                                        color = TodayTravelOrange,
                                        radius = 8f,
                                        center = Offset(-48f, 0f)
                                    )
                                    drawCircle(
                                        color = TodayTravelDarkGray,
                                        radius = 8f,
                                        center = Offset(-24f, 0f)
                                    )
                                    drawCircle(
                                        color = TodayTravelGray,
                                        radius = 7f,
                                        center = Offset(-24f, 0f)
                                    )
                                    drawCircle(
                                        color = TodayTravelDarkGray,
                                        radius = 8f,
                                        center = Offset(0f, 0f)
                                    )
                                    drawCircle(
                                        color = TodayTravelGray,
                                        radius = 7f,
                                        center = Offset(0f, 0f)
                                    )
                                }

                                1 -> {
                                    drawCircle(
                                        color = TodayTravelDarkGray,
                                        radius = 8f,
                                        center = Offset(-48f, 0f)
                                    )
                                    drawCircle(
                                        color = TodayTravelGray,
                                        radius = 7f,
                                        center = Offset(-48f, 0f)
                                    )
                                    drawCircle(
                                        color = TodayTravelGreen,
                                        radius = 8f,
                                        center = Offset(-24f, 0f)
                                    )
                                    drawCircle(
                                        color = TodayTravelDarkGray,
                                        radius = 8f,
                                        center = Offset(0f, 0f)
                                    )
                                    drawCircle(
                                        color = TodayTravelGray,
                                        radius = 7f,
                                        center = Offset(0f, 0f)
                                    )
                                }

                                else -> {
                                    drawCircle(
                                        color = TodayTravelDarkGray,
                                        radius = 8f,
                                        center = Offset(-48f, 0f)
                                    )
                                    drawCircle(
                                        color = TodayTravelGray,
                                        radius = 7f,
                                        center = Offset(-48f, 0f)
                                    )
                                    drawCircle(
                                        color = TodayTravelDarkGray,
                                        radius = 8f,
                                        center = Offset(-24f, 0f)
                                    )
                                    drawCircle(
                                        color = TodayTravelGray,
                                        radius = 7f,
                                        center = Offset(-24f, 0f)
                                    )
                                    drawCircle(
                                        color = TodayTravelBlue,
                                        radius = 8f,
                                        center = Offset(0f, 0f)
                                    )
                                }
                            }
                        }
                )
            }
        }
    }
}

enum class DialogEvent {
    NONE,
    DELETE_HISTORY,
    CANCEL_TRAVEL,
    SET_TRAVEL
}

val simpleDateFormat = SimpleDateFormat("yy.MM.dd.(E) a hh:mm", Locale("ko", "KR"))