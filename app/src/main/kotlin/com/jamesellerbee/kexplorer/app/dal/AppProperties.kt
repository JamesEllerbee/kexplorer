package com.jamesellerbee.kexplorer.app.dal

import androidx.compose.runtime.mutableStateMapOf
import java.io.File

object AppProperties {
    val homeDir
        get() = Defaults.defaultHomeDir

    val applications = mutableStateMapOf<String, String>()

    object Defaults {
        val defaultHomeDir = if (System.getProperty("os.name").contains("Windows")) {
            "${File.separator}Users${File.separator}${System.getProperty("user.name")}"
        } else {
            "/home/${System.getProperty("user.name")}"
        }
    }
}