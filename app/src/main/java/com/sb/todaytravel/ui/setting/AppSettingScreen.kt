package com.sb.todaytravel.ui.setting

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.sb.todaytravel.R

@Composable
fun AppSettingScreen(
    viewModel: AppSettingViewModel = hiltViewModel()
) {
    val travelRadius by viewModel.travelRadius.collectAsState()
    var isDeletionForHistoryDialogShown by remember { mutableStateOf(false) }

    BackHandler() {

    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (isDeletionForHistoryDialogShown) {
            DeleteHistoryDialog(
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
            Spacer(modifier = Modifier.height(4.dp))
            SettingComponentBundle(
                content = {
                    DescriptionText(description = "지도 설정")
                    Spacer(modifier = Modifier.height(4.dp))
                    SettingComponentText("지도 시작 지점", "내 위치")
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        color = Color(0xFFEEEEEE)
                    )
                    SettingComponentText("새 설정", "")
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
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
            Spacer(modifier = Modifier.height(4.dp))
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
                    SettingComponentText("새 설정", "")
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
fun DeleteHistoryDialog(
    onDismissRequest: () -> Unit = {},
    onAccept: () -> Unit = {}
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
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
                text = stringResource(R.string.delete_history),
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Divider(modifier = Modifier
                .fillMaxWidth(0.9F)
            )
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
                            onAccept()
                        }
                        .padding(vertical = 16.dp)
                    ,
                    text = stringResource(id = R.string.accept),
                    fontSize = 14.sp,
                    color = Color(0xFF4D88FF),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Description() {
    DeleteHistoryDialog()
}