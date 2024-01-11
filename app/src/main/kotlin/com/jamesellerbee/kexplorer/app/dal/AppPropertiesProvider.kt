package com.jamesellerbee.kexplorer.app.dal

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import java.io.File
import java.util.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

@Serializable
data class AppProperties(
    val homeDir: String = AppPropertiesProvider.Defaults.defaultHomeDir,
    val applications: Map<String, String> = emptyMap(),
    val showHiddenFiles: Boolean = false
)

object AppPropertiesProvider {
    private val savePath = "${Defaults.defaultHomeDir}${File.separator}.kexplorer${File.separator}"
    private val saveFileName = "appProperties.json"
    private val channel = Channel<AppProperties>()
    private val coroutineScope = CoroutineScope(SupervisorJob())
    private var saveJob: Job? = null
    private val logger = Logger.getLogger(AppPropertiesProvider::class.simpleName!!)

    val homeDir = mutableStateOf(Defaults.defaultHomeDir)
    val applications = mutableStateMapOf<String, String>()
    val showHiddenFiles = mutableStateOf(false)

    init {
        saveJob = coroutineScope.launch(Dispatchers.IO) {
            File(savePath).mkdirs()

            val saveFile = File("$savePath${File.separator}$saveFileName")
            if (saveFile.exists()) {
                val content = saveFile.readText()
                val appProperties = try {
                    Json.decodeFromString<AppProperties>(content)
                } catch (ex: SerializationException) {
                    logger.severe("Could not decode contents of save file")
                    logger.fine(ex.toString())
                    AppProperties()
                }

                withContext(Dispatchers.Main) {
                    homeDir.value = appProperties.homeDir
                    appProperties.applications.forEach { (name, value) ->
                        applications[name] = value
                    }
                    showHiddenFiles.value = appProperties.showHiddenFiles
                }
            }

            while (isActive) {
                val appProperties = channel.receive()
                saveFile.writeText(Json.encodeToString(appProperties))
            }
        }
    }

    fun addApplication(name: String, path: String) {
        applications[name] = path
        queueSave()
    }

    fun setShowHiddenFiles(newValue: Boolean) {
        showHiddenFiles.value = newValue
        queueSave()
    }

    private fun queueSave() {
        channel.trySend(
            AppProperties(
                homeDir.value,
                applications.toMap(),
                showHiddenFiles.value
            )
        )
    }

    object Defaults {
        val defaultHomeDir = if (System.getProperty("os.name").contains("Windows")) {
            "${File.separator}Users${File.separator}${System.getProperty("user.name")}"
        } else {
            "/home/${System.getProperty("user.name")}"
        }
    }
}