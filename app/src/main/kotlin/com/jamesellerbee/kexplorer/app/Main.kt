package com.jamesellerbee.kexplorer.app

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.jamesellerbee.kexplorer.app.bl.MainView
import com.jamesellerbee.kexplorer.app.bl.WindowManager
import com.jamesellerbee.kexplorer.app.bl.WindowStateProvider
import com.jamesellerbee.kexplorer.app.ui.settings.AppSettings
import com.jamesellerbee.kexplorer.app.ui.theme.AppTheme

@Composable
@Preview
fun App() {
    AppTheme {
        Scaffold(topBar = {
            TopAppBar(title = {
                Text("KExplorer")
            }, actions = {
                IconButton(onClick = {
                    MainView.view.value = { AppSettings() }
                }) {
                    Icon(Icons.Default.Settings, "")
                }
            })
        }) { paddingValues ->
            Column(Modifier.padding(paddingValues)) {
                MainView.view.value.invoke()
            }
        }
    }
}

fun main() = application {
    val windowState = rememberWindowState()
    WindowStateProvider.windowState = windowState

    // main window, closing this terminates the app
    Window(state = windowState, onCloseRequest = ::exitApplication) {
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
