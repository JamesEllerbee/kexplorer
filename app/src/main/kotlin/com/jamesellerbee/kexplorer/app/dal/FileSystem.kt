package com.jamesellerbee.kexplorer.app.dal

import java.io.File

object FileSystem {
    fun getListing(directory: String): List<File> {
        return File(directory).listFiles()?.toList() ?: emptyList()
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