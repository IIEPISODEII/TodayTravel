package com.sb.todaytravel.ui.core

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sb.todaytravel.ui.Screen

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val selectedColor = Color(0xFF82C0FF)
    val unselectedColor = Color(0xFFACACAC)
    val currentDestinationRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: Screen.MapScreen.route
    var previousClickedTime by remember { mutableLongStateOf(0L) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Row() {
            BottomNavIconButton(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxHeight(),
                onClick = {
                    if (navController.currentDestination!!.route == Screen.MapScreen.route) return@BottomNavIconButton
                    if (System.currentTimeMillis() - previousClickedTime < 33L) return@BottomNavIconButton
                    previousClickedTime = System.currentTimeMillis()
                    navController.navigate(Screen.MapScreen.route)
                },
                iconResource = Screen.MapScreen.iconResourceId,
                iconDescription = "map_screen",
                iconColor = if (currentDestinationRoute == Screen.MapScreen.route) selectedColor else unselectedColor
            )
            BottomNavIconButton(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxHeight(),
                onClick = {
                    if (navController.currentDestination!!.route == Screen.HistoryScreen.route) return@BottomNavIconButton
                    if (System.currentTimeMillis() - previousClickedTime < 33L) return@BottomNavIconButton
                    previousClickedTime = System.currentTimeMillis()
                    navController.navigate(Screen.HistoryScreen.route)
                },
                iconResource = Screen.HistoryScreen.iconResourceId,
                iconDescription = "history_screen",
                iconColor = if (currentDestinationRoute == Screen.HistoryScreen.route) selectedColor else unselectedColor
            )
            BottomNavIconButton(
                modifier = Modifier
                    .weight(1F)
                    .fillMaxHeight(),
                onClick = {
                    if (navController.currentDestination!!.route == Screen.AppSettingScreen.route) return@BottomNavIconButton
                    if (System.currentTimeMillis() - previousClickedTime < 33L) return@BottomNavIconButton
                    previousClickedTime = System.currentTimeMillis()
                    navController.navigate(Screen.AppSettingScreen.route)
                },
                iconResource = Screen.AppSettingScreen.iconResourceId,
                iconDescription = "history_screen",
                iconColor = if (currentDestinationRoute == Screen.AppSettingScreen.route) selectedColor else unselectedColor
            )
        }
    }
}