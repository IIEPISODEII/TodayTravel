package com.sb.todaytravel.ui.core

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource

@Composable
fun BottomNavIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    @DrawableRes iconResource: Int,
    iconDescription: String = "",
    iconColor: Color
) {
    Box(
        modifier = modifier
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = ImageVector.vectorResource(id = iconResource), contentDescription = iconDescription, tint = iconColor)
    }
}