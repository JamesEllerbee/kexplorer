package com.jamesellerbee.kexplorer.app.ui.filebrowser

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.jamesellerbee.kexplorer.app.dal.AppPropertiesProvider
import com.jamesellerbee.kexplorer.app.dal.FileSystem
import com.jamesellerbee.kexplorer.app.dal.Shell
import java.io.File

class FileBrowserViewModel(private val fileSystem: FileSystem = FileSystem) {
    private var _workingDirectory = AppPropertiesProvider.homeDir.value
    val workingDirectory get() = _workingDirectory

    val workingDirectoryText = mutableStateOf(_workingDirectory)
    val files = mutableStateListOf<File>()
    val selectedFileName = mutableStateOf<String?>(null)
    val showContextMenuForFileName = mutableStateOf<String?>(null)
    val showFileCreationDialog = mutableStateOf(false)
    val showFilePreviewDialog = mutableStateOf(false)
    val selectedFileContent = mutableStateOf("")
    val showOpenInDialog = mutableStateOf(false)

    init {
        updateWorkingDirectory(_workingDirectory)
    }

    fun onInteraction(interaction: FileBrowserInteraction) {
        when (interaction) {
            FileBrowserInteraction.GoToParentDirectory -> {
                if (fileSystem.getParentDirectoryPath(_workingDirectory) == null) {
                    return
                }

                updateWorkingDirectory(fileSystem.getParentDirectoryPath(_workingDirectory)!!)
            }

            FileBrowserInteraction.GoToHomeDirectory -> {
                updateWorkingDirectory(AppPropertiesProvider.homeDir.value)
            }

            FileBrowserInteraction.RefreshListing -> {
                workingDirectoryText.value = workingDirectory
                updateWorkingDirectory(workingDirectory)
            }

            is FileBrowserInteraction.SetWorkingDirectoryText -> {
                workingDirectoryText.value = interaction.newValue
            }

            is FileBrowserInteraction.SelectFile -> {
                val targetPath = joinPath(interaction.name)

                if (selectedFileName.value == interaction.name
                    && fileSystem.isDirectory(targetPath)
                ) {
                    updateWorkingDirectory(targetPath)
                } else if (selectedFileName.value == interaction.name
                    && !fileSystem.isDirectory(targetPath)
                ) {
                    previewFile(targetPath)
                } else {
                    selectedFileName.value = interaction.name
                }
            }

            is FileBrowserInteraction.SaveFile -> {
                fileSystem.createFile(joinPath(interaction.name))
                refreshListing()
                showFileCreationDialog.value = false
            }

            is FileBrowserInteraction.ShowFileContextMenu -> {
                showContextMenuForFileName.value = interaction.name
            }

            FileBrowserInteraction.DismissFileContextMenu -> {
                showContextMenuForFileName.value = null
            }

            FileBrowserInteraction.ShowFileCreationDialog -> {
                showFileCreationDialog.value = true
            }

            FileBrowserInteraction.ShowOpenInDialog -> {
                showOpenInDialog.value = true
            }

            is FileBrowserInteraction.FileContextMenuInteraction.DeleteFile -> {
                fileSystem.deletePath(joinPath(interaction.fileName))
                refreshListing()
            }

            is FileBrowserInteraction.FileContextMenuInteraction.PreviewFile -> {
                previewFile(joinPath(interaction.fileName))
            }

            FileBrowserInteraction.FileCreationDialogInteraction.DismissFileCreationDialog -> {
                showFileCreationDialog.value = false
            }

            FileBrowserInteraction.FilePreviewDialogInteraction.DismissFilePreviewDialog -> {
                showFilePreviewDialog.value = false
            }

            FileBrowserInteraction.OpenInDialogInteraction.DismissOpenInDialog -> {
                showOpenInDialog.value = false
            }

            is FileBrowserInteraction.OpenInDialogInteraction.OpenFileInApplication -> {
                Shell.run(interaction.application, joinPath(interaction.fileName))
                showOpenInDialog.value = false
                showContextMenuForFileName.value = null
            }
        }
    }

    private fun previewFile(targetPath: String) {
        if (fileSystem.isTextFile(targetPath)) {
            selectedFileContent.value = fileSystem.readContent(targetPath)
        } else {
            selectedFileContent.value =
                "This file does not appear to be a text file. Only previewing of text files is supported."
        }
        showFilePreviewDialog.value = true
    }

    private fun updateWorkingDirectory(path: String) {
        _workingDirectory = path
        workingDirectoryText.value = _workingDirectory
        refreshListing()
    }

    private fun refreshListing() {
        files.clear()
        val listing = fileSystem.getListing(_workingDirectory)
        val directories = listing.filter { it.isDirectory }.sortedBy { it.name }
        val filesInWorkingDirectory = listing.filter { !it.isDirectory }.sortedBy { it.name }
        files.addAll(directories)
        files.addAll(filesInWorkingDirectory)
        selectedFileName.value = null
    }

    private fun joinPath(fileName: String): String {
        val path = StringBuilder(_workingDirectory)

        if (_workingDirectory != File.separator) {
            path.append(File.separator)
        }

        path.append(fileName)
        return path.toString()
    }
}