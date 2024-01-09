package com.jamesellerbee.kexplorer.app.dal

import java.io.File
import java.io.FileNotFoundException
import java.util.logging.Logger

object FileSystem {
    private val logger = Logger.getLogger(FileSystem::class.simpleName!!)

    fun getListing(directory: String): List<File> {
        return File(directory).listFiles()?.toList() ?: emptyList()
    }

    fun isTextFile(path: String): Boolean {
        val file = File(path)

        if(file.isDirectory) {
            return false
        }

        var bytes = emptyList<Byte>()

        try {
            bytes = file.readBytes().toList()
        } catch (ex: FileNotFoundException) {
            logger.severe("Could not read file at path \"$path\": ${ex.message}")
        }

        var ascii = 0
        var other = 0
        bytes.forEach { byte ->
            if (byte < 0x09) {
                return false
            }

            when (byte) {
                0x09.toByte(), 0x0A.toByte(), 0x0C.toByte(), 0x0D.toByte() -> {
                    ascii++
                }
                in 0x20..0x7E -> {
                    ascii++
                }
                else -> {
                    other++
                }
            }
        }

        if(ascii + other == 0) {
            return true
        }

        return 100 * ascii / (ascii + other) > 95
    }

    fun readContent(path: String): String {
        var content: String
        try {
            content = File(path).readText()
        } catch (ex: FileNotFoundException) {
            logger.severe("Could not read file at path \"${path}\": ${ex.message}")
            content = ex.message ?: "Unknown error"
        }

        return content
    }

    fun createFile(path: String) {
        File(path).createNewFile()
    }

    fun deletePath(path: String) {
        val file = File(path)
        if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }

    fun isDirectory(path: String): Boolean {
        return File(path).isDirectory
    }

    fun getParentDirectoryPath(path: String): String? {
        return File(path).parent
    }
}