package com.sb.todaytravel.feature.travel_history

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sb.todaytravel.R
import com.sb.todaytravel.feature.core.MainViewModel
import com.sb.todaytravel.feature.core.AcceptionDialog
import com.sb.todaytravel.feature.theme.TodayTravelTeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val travelHistories by historyViewModel.allTravelHistories.collectAsState()
    val orderType by historyViewModel.orderType.collectAsState()
    val lastTravelHistoryIndex by mainViewModel.lastTravelHistoryIndex.collectAsState()

    var isSetTravelDestinationDialogShown by remember {
        mutableStateOf(false)
    }

    var isTravelCancelDialogShown by remember {
        mutableStateOf(false)
    }

    var isTravelHistoryDeleteDialogShown by remember {
        mutableStateOf(false)
    }

    var candidateTravelHistoryIndex by remember {
        mutableIntStateOf(-1)
    }

    val orderTypeDirection by animateFloatAsState(
        targetValue = if (orderType == OrderType.Ascend) 180F else 0F,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )

    Box(
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6F)
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
                        isSetTravelDestinationDialogShown = true
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(0.7F),
                    userScrollEnabled = true,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    items(travelHistories.size) {
                        TravelHistoryItem(
                            travelHistoryWithLocations = travelHistories[it],
                            onClick = { mainViewModel.updateMarkedTravelHistory(travelHistories[it]) },
                            onLongClick = {
                                if (mainViewModel.isTraveling.value && lastTravelHistoryIndex == travelHistories[it].travelHistoryIndex) isTravelCancelDialogShown = true
                                else isTravelHistoryDeleteDialogShown = true
                                candidateTravelHistoryIndex = travelHistories[it].travelHistoryIndex
                            }
                        )
                    }
                }
            }
        }

        if (isSetTravelDestinationDialogShown) {
            AcceptionDialog(
                question = stringResource(R.string.set_here_destination),
                onDismissRequest = { isSetTravelDestinationDialogShown = false },
                onAccept = {
                    isSetTravelDestinationDialogShown = false
                    mainViewModel.startTravel()
                }
            )
        }
    }

    if (isTravelCancelDialogShown) {
        AcceptionDialog(
            question = stringResource(R.string.question_travel_cancel),
            onDismissRequest = {
                isTravelCancelDialogShown = false
                               },
            onAccept = {
                isTravelCancelDialogShown = false
                mainViewModel.cancelTravel()
            }
        )
    }

    if (isTravelHistoryDeleteDialogShown) {
        AcceptionDialog(
            question = stringResource(R.string.question_travel_history_delete),
            onDismissRequest = {
                isTravelHistoryDeleteDialogShown = false
            },
            onAccept = {
                isTravelHistoryDeleteDialogShown = false
                historyViewModel.deleteTravelHistory(candidateTravelHistoryIndex)
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

@Composable
fun TravelHistoryItem(
    travelHistoryWithLocations: TravelHistoryWithLocations,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }
    val ripple = rememberRipple(
        bounded = true,
        radius = 300.dp,
        color = Color(0xFFEEEEEE)
    )

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
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .background(Color(0xFFEEEEEE), RoundedCornerShape(20))
                .clip(RoundedCornerShape(20))
                .pointerInput(key1 = Unit) {
                    detectTapGestures(
                        onPress = { onClick.invoke() },
                        onLongPress = { onLongClick.invoke() }
                    )
                }
                .indication(interactionSource = interactionSource, indication = ripple)
                .padding(horizontal = 24.dp, vertical = 12.dp)
                .align(Alignment.CenterStart),
            text = if (travelHistoryWithLocations.travelLocations.isNotEmpty()) { travelHistoryWithLocations.travelLocations.first().let {
                SimpleDateFormat("yy.MM.dd.(E) a hh:mm", Locale("ko", "KR")).format(Date(it.arrivalTime))
            } + "부터\n" + if (travelHistoryWithLocations.travelLocations.size > 1) { travelHistoryWithLocations.travelLocations.last().let {
                SimpleDateFormat("yy.MM.dd.(E) a hh:mm", Locale("ko", "KR")).format(Date(it.arrivalTime))
            } + "까지의 여정" } else "" } else "",
            fontSize = 18.sp,
            textAlign = TextAlign.Start,
            lineHeight = TextUnit(28F, TextUnitType.Sp)
        )
    }
}