package com.sb.todaytravel.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sb.todaytravel.R

@Composable
fun BottomAnimatedDialog(
    modifier: Modifier,
    isShown: Boolean = false,
    onDismiss: () -> Unit = {},
    onAccept: () -> Unit = {}
) {
    AnimatedContent(
        targetState = isShown,
        modifier = modifier,
        transitionSpec = {
            slideInVertically(
                animationSpec = tween(durationMillis = 100, delayMillis = 60),
                initialOffsetY = { -300 }
            ).togetherWith(
                slideOutVertically(
                    animationSpec = tween(durationMillis = 100, delayMillis = 60),
                    targetOffsetY = { -300 }
                )
            )
        }
    ) { isVisible ->
        if (isVisible) {
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
                    text = stringResource(R.string.set_here_destination),
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
                                onDismiss()
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
                        text = stringResource(R.string.accept),
                        fontSize = 14.sp,
                        color = Color(0xFF4D88FF),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}