package com.jamesellerbee.kexplorer.app.dal

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.InputStreamReader

object Shell {
    private val coroutineScope = CoroutineScope(SupervisorJob())

    fun run(command: String, vararg args: String) {
        val processBuilder = ProcessBuilder()
        processBuilder.command(command, *args)

        coroutineScope.launch(Dispatchers.Default) {
            val process = processBuilder.start()

            val consumer = coroutineScope.launch(Dispatchers.Default) {
                val inputStreamReader = InputStreamReader(process.inputStream)
                while(isActive) {
                    inputStreamReader.forEachLine { println(it) }
                }
            }

            process.waitFor()
            consumer.cancel()
        }
    }
}