package com.jamesellerbee.kexplorer.app.bl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf

data class WindowData(val name: String, val content: @Composable () -> Unit)

object WindowManager {
    val windowData = mutableStateListOf<WindowData>()
}