package com.jamesellerbee.kexplorer.app.bl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import com.jamesellerbee.kexplorer.app.ui.filebrowser.FileBrowser

object MainView {
    val view = mutableStateOf<@Composable () -> Unit>({ FileBrowser() })
}