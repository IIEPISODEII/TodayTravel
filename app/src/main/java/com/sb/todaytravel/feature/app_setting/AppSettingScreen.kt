package com.sb.todaytravel.feature.app_setting

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sb.todaytravel.R
import com.sb.todaytravel.feature.core.AcceptionDialog
import com.sb.todaytravel.feature.theme.TodayTravelTeal

@Composable
fun AppSettingScreen(
    viewModel: AppSettingViewModel = hiltViewModel()
) {
    val travelRadius by viewModel.travelRadius.collectAsState()
    var isDeletionForHistoryDialogShown by remember { mutableStateOf(false) }
    var preventionOfMapRotation by remember { mutableStateOf(viewModel.preventionOfMapRotation.value) }

    BackHandler() {

    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (isDeletionForHistoryDialogShown) {
            AcceptionDialog(
                question = stringResource(id = R.string.delete_history),
                onDismissRequest = { isDeletionForHistoryDialogShown = false },
                onAccept = {
                    viewModel.deleteHistory()
                    isDeletionForHistoryDialogShown = false
                }
            )
        }

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
                    SettingComponentText(stringResource(R.string.appsetting_map_starting_point), "내 위치")
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        color = Color(0xFFEEEEEE)
                    )
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
                        onClick = { viewModel.setTravelRadius(0) }
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
                        onClick = { isDeletionForHistoryDialogShown = true }
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