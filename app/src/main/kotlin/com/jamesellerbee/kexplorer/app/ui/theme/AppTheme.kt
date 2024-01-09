package com.jamesellerbee.kexplorer.app.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object Spacing {
    val small = 8.dp
    val medium = 12.dp
}

val MaterialTheme.spacing get() = Spacing

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme {
        content.invoke()
    }
}