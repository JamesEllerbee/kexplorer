package com.jamesellerbee.kexplorer.app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.jamesellerbee.kexplorer.app.bl.MainView
import com.jamesellerbee.kexplorer.app.bl.WindowManager
import com.jamesellerbee.kexplorer.app.ui.theme.AppTheme

@Composable
@Preview
fun App() {
    AppTheme {
        MainView.view.value.invoke()
    }
}

fun main() = application {
    // main window, closing this terminates the app
    Window(onCloseRequest = ::exitApplication) {
        App()
    }

    // other windows, closing these only hides them
    WindowManager.windowData.forEach { windowData ->
        Window(onCloseRequest = {
            WindowManager.windowData.removeIf { it.name == windowData.name }
        }) {
            windowData.content
        }
    }
}
