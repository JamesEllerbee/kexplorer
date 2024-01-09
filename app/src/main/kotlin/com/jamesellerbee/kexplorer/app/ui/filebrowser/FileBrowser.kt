package com.jamesellerbee.kexplorer.app.ui.filebrowser

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.PointerMatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.onClick
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.window.Dialog
import com.jamesellerbee.kexplorer.app.dal.AppProperties
import com.jamesellerbee.kexplorer.app.dal.FileSystem
import com.jamesellerbee.kexplorer.app.ui.theme.spacing
import java.io.File

sealed class FileBrowserInteraction {
    data object GoToParentDirectory : FileBrowserInteraction()
    data object GoToHomeDirectory : FileBrowserInteraction()
    data class SelectFile(val name: String) : FileBrowserInteraction()
    data class SaveFile(val name: String, val isDirectory: Boolean) : FileBrowserInteraction()
    data class ShowFileContextMenu(val name: String) : FileBrowserInteraction()
    data object DismissFileContextMenu : FileBrowserInteraction()
    data object ShowFileCreationDialog : FileBrowserInteraction()


    sealed class FileContextMenuInteraction : FileBrowserInteraction() {
        data class DeleteFile(val fileName: String) : FileContextMenuInteraction()
    }

    sealed class FileCreationDialogInteraction : FileBrowserInteraction() {
        data object DismissFileCreationDialog : FileCreationDialogInteraction()
    }

    sealed class FilePreviewDialogInteraction : FileBrowserInteraction() {
        data object DismissFilePreviewDialog : FilePreviewDialogInteraction()

    }
}

class FileBrowserViewModel(private val fileSystem: FileSystem = FileSystem) {
    private var workingDirectory = AppProperties.homeDir
    val workingDirectoryText = mutableStateOf(workingDirectory)
    val files = mutableStateListOf<File>()
    val selectedFileName = mutableStateOf<String?>(null)
    val showContextMenuForFileName = mutableStateOf<String?>(null)
    val showFileCreationDialog = mutableStateOf(false)
    val showFilePreviewDialog = mutableStateOf(false)
    val selectedFileContent = mutableStateOf("")

    init {
        updateWorkingDirectory(workingDirectory)
    }

    fun onInteraction(interaction: FileBrowserInteraction) {
        when (interaction) {
            FileBrowserInteraction.GoToParentDirectory -> {
                if (fileSystem.getParentDirectoryPath(workingDirectory) == null) {
                    return
                }

                updateWorkingDirectory(fileSystem.getParentDirectoryPath(workingDirectory)!!)
            }

            FileBrowserInteraction.GoToHomeDirectory -> {
                updateWorkingDirectory(AppProperties.homeDir)
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
                    if(fileSystem.isTextFile(targetPath)) {
                        selectedFileContent.value = fileSystem.readContent(targetPath)
                    } else {
                        selectedFileContent.value = "This file does not appear to be a text file. Only previewing of text files is supported."
                    }
                    showFilePreviewDialog.value = true
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

            is FileBrowserInteraction.FileContextMenuInteraction.DeleteFile -> {
                fileSystem.deletePath(joinPath(interaction.fileName))
                refreshListing()
            }

            FileBrowserInteraction.FileCreationDialogInteraction.DismissFileCreationDialog -> {
                showFileCreationDialog.value = false
            }

            FileBrowserInteraction.FilePreviewDialogInteraction.DismissFilePreviewDialog -> {
                showFilePreviewDialog.value = false
            }
        }
    }

    private fun updateWorkingDirectory(path: String) {
        workingDirectory = path
        workingDirectoryText.value = workingDirectory
        refreshListing()
    }

    private fun refreshListing() {
        files.clear()
        val listing = fileSystem.getListing(workingDirectory)
        val directories = listing.filter { it.isDirectory }.sortedBy { it.name }
        val filesInWorkingDirectory = listing.filter { !it.isDirectory }.sortedBy { it.name }
        files.addAll(directories)
        files.addAll(filesInWorkingDirectory)
        selectedFileName.value = null
    }

    private fun joinPath(fileName: String): String {
        val path = StringBuilder(workingDirectory)

        if (workingDirectory != File.separator) {
            path.append(File.separator)
        }

        path.append(fileName)
        return path.toString()
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileBrowser() {
    val viewModel by remember { mutableStateOf(FileBrowserViewModel()) }

    Scaffold(topBar = {
        TopAppBar(title = {
            Text("KExplorer")
        })
    }) { paddingValues ->
        if (viewModel.showFileCreationDialog.value) {
            FileCreationDialog(viewModel)
        }

        if (viewModel.showFilePreviewDialog.value) {
            FilePreviewDialog(viewModel)
        }

        Column(Modifier.padding(paddingValues)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    viewModel.onInteraction(FileBrowserInteraction.GoToHomeDirectory)
                }) {
                    Icon(Icons.Default.Home, "Home directory")
                }

                IconButton(onClick = {
                    viewModel.onInteraction(FileBrowserInteraction.GoToParentDirectory)
                }) {
                    Icon(Icons.Default.ArrowUpward, "Go to parent directory")
                }

                OutlinedTextField(value = viewModel.workingDirectoryText.value, onValueChange = {}, label = {
                    Text("Working directory")
                })

                IconButton(onClick = {
                    viewModel.onInteraction(FileBrowserInteraction.ShowFileCreationDialog)
                }) {
                    Icon(Icons.Default.NoteAdd, "Create new file")
                }
            }

            Divider(Modifier.fillMaxWidth())

            Column(Modifier.verticalScroll(rememberScrollState())) {
                viewModel.files.forEach { file ->
                    if (!file.name.startsWith(".")) {
                        val itemColor = if (file.name == viewModel.selectedFileName.value) {
                            MaterialTheme.colors.secondary
                        } else {
                            Color.Transparent
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                viewModel.onInteraction(FileBrowserInteraction.SelectFile(file.name))
                            }.onClick(matcher = PointerMatcher.mouse(PointerButton.Secondary)) {
                                viewModel.onInteraction(FileBrowserInteraction.ShowFileContextMenu(file.name))
                            }.background(itemColor)
                        ) {
                            if (file.isDirectory) {
                                Icon(Icons.Default.Folder, "Directory")
                            }

                            Text(file.name)

                            if (viewModel.showContextMenuForFileName.value == file.name) {
                                FileContextMenu(viewModel, file.name)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FileCreationDialog(viewModel: FileBrowserViewModel) {
    var newFileName by remember { mutableStateOf("") }
    var isDirectory by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = {
        viewModel.onInteraction(FileBrowserInteraction.FileCreationDialogInteraction.DismissFileCreationDialog)
    }) {
        Column(Modifier.background(MaterialTheme.colors.background).padding(MaterialTheme.spacing.medium)) {
            OutlinedTextField(
                value = newFileName, onValueChange = {
                    newFileName = it
                }, label = { Text("New File") })

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Is directory?")
                Checkbox(checked = isDirectory, onCheckedChange = {
                    isDirectory = it
                })
            }

            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        viewModel.onInteraction(FileBrowserInteraction.SaveFile(newFileName, isDirectory))
                    },
                    enabled = newFileName.isNotEmpty()
                ) {
                    Text("Save")
                }

                Button(onClick = {
                    viewModel.onInteraction(FileBrowserInteraction.FileCreationDialogInteraction.DismissFileCreationDialog)
                }) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun FilePreviewDialog(viewModel: FileBrowserViewModel) {
    Dialog(onDismissRequest = {
        viewModel.onInteraction(FileBrowserInteraction.FilePreviewDialogInteraction.DismissFilePreviewDialog)
    }) {
        Column(Modifier.background(MaterialTheme.colors.background).padding(MaterialTheme.spacing.medium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Contents of \"${viewModel.selectedFileName.value}\"")
                IconButton(onClick = {

                }) {
                    Icon(Icons.Default.Edit, "Edit contents of file")
                }
            }

            Divider(Modifier.fillMaxWidth())

            Row(Modifier.verticalScroll(rememberScrollState()).weight(0.9f)) {
                Text(
                    if (viewModel.selectedFileContent.value.isEmpty()) {
                        "The file is empty"
                    } else if (viewModel.selectedFileContent.value.isBlank()) {
                        "The File only contains whitespace"
                    } else {
                        viewModel.selectedFileContent.value
                    }
                )
            }

            Divider(Modifier.fillMaxWidth())

            Row(modifier = Modifier.weight(0.1f)) {
                Button(onClick = {
                    viewModel.onInteraction(FileBrowserInteraction.FilePreviewDialogInteraction.DismissFilePreviewDialog)
                }) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun FileContextMenu(viewModel: FileBrowserViewModel, fileName: String) {
    DropdownMenu(expanded = viewModel.showContextMenuForFileName.value != null,
        onDismissRequest = { viewModel.onInteraction(FileBrowserInteraction.DismissFileContextMenu) }) {
        DropdownMenuItem(onClick = {

        }) {
            Text("Preview")
        }

        DropdownMenuItem(onClick = {

        }) {
            Text("Open in...")
        }

        DropdownMenuItem(onClick = {
            viewModel.onInteraction(FileBrowserInteraction.FileContextMenuInteraction.DeleteFile(fileName))
        }) {
            Text("Delete")
        }
    }
}