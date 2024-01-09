package com.jamesellerbee.kexplorer.app.dal

import java.io.File

object AppProperties {
    val homeDir
        get() = Defaults.defaultHomeDir

    object Defaults {
        val defaultHomeDir = "${File.separator}Users${File.separator}${System.getProperty("user.name")}"
    }
}