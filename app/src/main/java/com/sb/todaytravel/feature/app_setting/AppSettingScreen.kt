package com.sb.todaytravel.feature.app_setting

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Slider
import androidx.compose.material.SliderDefaults
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.sb.todaytravel.R
import com.sb.todaytravel.feature.core.AcceptionDialog
import com.sb.todaytravel.feature.theme.TodayTravelBlue
import com.sb.todaytravel.feature.theme.TodayTravelTeal

@Composable
fun AppSettingScreen(
    viewModel: AppSettingViewModel = hiltViewModel()
) {
    val travelRadius by viewModel.travelRadius.collectAsState()
    var dialogEvent: DialogEvent by remember { mutableStateOf(DialogEvent.NONE) }
    var preventionOfMapRotation by remember { mutableStateOf(viewModel.preventionOfMapRotation.value) }
    var travelRadiusProgressState by remember { mutableFloatStateOf((travelRadius-100).toFloat() / 1900) }

    LaunchedEffect(key1 = travelRadius) {
        travelRadiusProgressState = (travelRadius-100).toFloat() / 1900
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
        ) {
            SettingComponentBundle(
                content = {
                    DescriptionText(description = stringResource(R.string.appsetting_map))
                    Spacer(modifier = Modifier.height(4.dp))
                    SettingComponentSwitch(
                        description = stringResource(R.string.description_for_prevention_of_map_rotation),
                        isChecked = preventionOfMapRotation,
                        onCheckedChange = { value ->
                            viewModel.setPreventionOfMapRotation(value)
                            preventionOfMapRotation = value
                        }
                    )
                }
            )
            SettingComponentBundle(
                content = {
                    DescriptionText(description = "여행 설정")
                    Spacer(modifier = Modifier.height(4.dp))
                    SettingComponentText(
                        description = "여행 범위",
                        settingDescription = "내 위치에서 ${travelRadius}m",
                        onClick = { dialogEvent = DialogEvent.SET_TRAVEL_RADIUS }
                    )
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        color = Color(0xFFEEEEEE)
                    )
                    SettingComponentText("새 설정", "")
                }
            )
            SettingComponentBundle(
                content = {
                    DescriptionText(description = "앱 설정")
                    Spacer(modifier = Modifier.height(4.dp))
                    SettingComponentText(
                        description = "사용 기록 삭제",
                        settingDescription = "",
                        onClick = { dialogEvent = DialogEvent.DELETE_HISTORY }
                    )
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        color = Color(0xFFEEEEEE)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "버전",
                            color = Color.DarkGray,
                            fontSize = 16.sp
                        )
                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )
                        Text(
                            text = stringResource(id = R.string.version_codes),
                            color = Color(0xFF888888),
                            fontSize = 12.sp
                        )
                    }
                }
            )
        }

        if (dialogEvent == DialogEvent.DELETE_HISTORY) {
            AcceptionDialog(
                question = stringResource(id = R.string.delete_history),
                onDismissRequest = { dialogEvent = DialogEvent.NONE },
                onAccept = {
                    viewModel.deleteHistory()
                    dialogEvent = DialogEvent.NONE
                }
            )
        }
        if (dialogEvent == DialogEvent.SET_TRAVEL_RADIUS) {
            SeekbarDialog(
                onDismissRequest = { dialogEvent = DialogEvent.NONE },
                onAccept = { progressState ->
                    dialogEvent = DialogEvent.NONE
                    viewModel.setTravelRadius(100 + (progressState * 38).toInt() * 50)
                           },
                initialProgressState = travelRadiusProgressState
            )
        }
    }
}

@Composable
fun SettingComponentBundle(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.background(Color.White)
    ){
        content()
    }
}

@Composable
fun DescriptionText(description: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = Color(0xFFEEEEEE))
            .padding(start = 24.dp, end = 16.dp)
    ) {
        Text(
            modifier = Modifier
                .padding(vertical = 4.dp),
            text = description,
            fontSize = 16.sp
        )
    }
}

@Composable
fun SettingComponentText(
    description: String,
    settingDescription: String,
    onClick: () -> Unit = {}
) {
    val interactionSource by remember { mutableStateOf(MutableInteractionSource()) }
    val ripple = rememberRipple(
        bounded = true,
        radius = 300.dp,
        color = Color(0xFFEEEEEE)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = Color.White)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple,
                onClick = onClick
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = description,
            fontSize = 16.sp,
            modifier= Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp),
            color = Color.DarkGray
        )
        Row(
            modifier = Modifier
                .wrapContentWidth()
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(end = 4.dp),
                text = settingDescription,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Arrow",
                tint = Color.DarkGray
            )
        }
    }
}

@Composable
fun SettingComponentSwitch(
    description: String,
    isChecked: Boolean = false,
    onCheckedChange: (Boolean) -> Unit = {}
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = Color.White),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = description,
            fontSize = 16.sp,
            modifier= Modifier
                .padding(horizontal = 24.dp, vertical = 12.dp),
            color = Color.DarkGray
        )
        Switch(
            modifier = Modifier
                .wrapContentWidth()
                .padding(end = 8.dp)
                .align(Alignment.CenterVertically),
            checked = isChecked,
            onCheckedChange = { value -> onCheckedChange(value) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = TodayTravelTeal
            )
        )
    }
}

enum class DialogEvent {
    NONE,
    DELETE_HISTORY,
    SET_TRAVEL_RADIUS
}

@Composable
fun SeekbarDialog(
    onDismissRequest: () -> Unit = {},
    onAccept: (Float) -> Unit = {},
    initialProgressState: Float
) {
    var progressState by remember { mutableFloatStateOf(initialProgressState) }

    Dialog(
        onDismissRequest = { onDismissRequest() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(1F)
                .wrapContentHeight()
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = "얼마나 멀리 떠나실 건가요?",
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier
                        .weight(0.2F),
                    text = "100m",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Slider(
                    value = progressState,
                    onValueChange = { slideProgress: Float -> progressState = (slideProgress * 20).toInt().toFloat() / 20 },
                    modifier = Modifier
                        .weight(0.6F)
                        .height(2.dp),
                    colors = SliderDefaults.colors(
                        thumbColor = TodayTravelBlue,
                        activeTrackColor = Color(0xFF7FFFFF)
                    )
                )
                Text(
                    modifier = Modifier
                        .weight(0.2F),
                    text = "2000m",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = "${100 + (progressState * 38).toInt() * 50}m",
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentHeight()
                        .weight(1F)
                        .clip(RoundedCornerShape(bottomStart = 20.dp))
                        .clickable {
                            onDismissRequest()
                        }
                        .padding(vertical = 16.dp)
                    ,
                    text = stringResource(R.string.dismiss),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .weight(0.01F)
                )
                Text(
                    modifier = Modifier
                        .wrapContentHeight()
                        .weight(1F)
                        .clip(RoundedCornerShape(bottomEnd = 20.dp))
                        .clickable {
                            onAccept(progressState)
                        }
                        .padding(vertical = 16.dp)
                    ,
                    text = stringResource(R.string.accept),
                    fontSize = 14.sp,
                    color = Color(0xFF4D88FF),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}