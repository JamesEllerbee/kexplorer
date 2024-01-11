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
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerButton
import androidx.compose.ui.window.Dialog
import com.jamesellerbee.kexplorer.app.bl.WindowStateProvider
import com.jamesellerbee.kexplorer.app.dal.AppPropertiesProvider
import com.jamesellerbee.kexplorer.app.ui.theme.spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileBrowser() {
    val viewModel by remember { mutableStateOf(FileBrowserViewModel()) }

    if (viewModel.showFileCreationDialog.value) {
        FileCreationDialog(viewModel)
    }

    if (viewModel.showFilePreviewDialog.value) {
        FilePreviewDialog(viewModel)
    }

    if (viewModel.showOpenInDialog.value) {
        OpenInDialog(viewModel)
    }

    Column {
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

            OutlinedTextField(
                value = viewModel.workingDirectoryText.value,
                onValueChange = {
                    viewModel.onInteraction(FileBrowserInteraction.SetWorkingDirectoryText(it))
                },
                label = {
                    Text("Working directory")
                },
                trailingIcon = {
                    Row {
                        if (viewModel.workingDirectoryText.value != viewModel.workingDirectory) {
                            IconButton(onClick = {

                            }) {
                                Icon(Icons.Default.ArrowForward, "Go to")
                            }
                        }

                        IconButton(onClick = {
                            viewModel.onInteraction(FileBrowserInteraction.RefreshListing)
                        }) {
                            Icon(Icons.Default.Refresh, "Refresh")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.widthIn(max = WindowStateProvider.windowState.size.width * 0.5f)
            )

            IconButton(onClick = {
                viewModel.onInteraction(FileBrowserInteraction.ShowFileCreationDialog)
            }) {
                Icon(Icons.Default.NoteAdd, "Create new file")
            }

            IconButton(onClick = {
                AppPropertiesProvider.setShowHiddenFiles(!AppPropertiesProvider.showHiddenFiles.value)
            }) {
                if (AppPropertiesProvider.showHiddenFiles.value) {
                    Icon(Icons.Default.Visibility, "Hide hidden files")
                } else {
                    Icon(Icons.Default.VisibilityOff, "Show hidden files")
                }
            }
        }

        Divider(Modifier.fillMaxWidth())

        Column(Modifier.verticalScroll(rememberScrollState())) {
            val files = if (AppPropertiesProvider.showHiddenFiles.value) {
                viewModel.files
            } else {
                viewModel.files.filter { !it.name.startsWith(".") }
            }

            files.forEach { file ->
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
            viewModel.onInteraction(FileBrowserInteraction.FileContextMenuInteraction.PreviewFile(fileName))
        }) {
            Text("Preview")
        }

        DropdownMenuItem(onClick = {
            viewModel.onInteraction(FileBrowserInteraction.ShowOpenInDialog)
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

@Composable
fun OpenInDialog(viewModel: FileBrowserViewModel) {
    Dialog(onDismissRequest = {
        viewModel.onInteraction(FileBrowserInteraction.OpenInDialogInteraction.DismissOpenInDialog)
    }) {
        Column(Modifier.background(MaterialTheme.colors.background).padding(MaterialTheme.spacing.medium)) {
            if (AppPropertiesProvider.applications.isEmpty()) {
                Text("There are no applications configured. Go to app settings and add applications to use this feature.")
            } else {
                Text("Applications:")
                AppPropertiesProvider.applications.forEach { (key, value) ->
                    Row(Modifier.clickable {
                        viewModel.onInteraction(
                            FileBrowserInteraction.OpenInDialogInteraction.OpenFileInApplication(
                                value,
                                viewModel.showContextMenuForFileName.value ?: ""
                            )
                        )
                    }) {
                        Text(key)
                    }
                }
            }
        }
    }
}
