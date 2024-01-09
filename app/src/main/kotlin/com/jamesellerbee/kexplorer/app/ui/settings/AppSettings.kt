package com.jamesellerbee.kexplorer.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.jamesellerbee.kexplorer.app.bl.MainView
import com.jamesellerbee.kexplorer.app.dal.AppProperties
import com.jamesellerbee.kexplorer.app.ui.filebrowser.FileBrowser
import com.jamesellerbee.kexplorer.app.ui.theme.spacing

@Composable
fun AppSettings() {
    Column(Modifier.fillMaxWidth()) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            ApplicationsSetting()
        }

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = {
                MainView.view.value = { FileBrowser() }
            }) {
                Text("Apply")
            }

            Button(onClick = {
                MainView.view.value = { FileBrowser() }
            }) {
                Text("Cancel")
            }
        }
    }
}

@Composable
fun ApplicationsSetting() {
    var showFields by remember { mutableStateOf(false) }
    var newApplicationName by remember { mutableStateOf("") }
    var newApplicationPath by remember { mutableStateOf("") }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Applications")

        IconButton(onClick = {
            showFields = true
        }) {
            Icon(Icons.Default.AddCircle, "Add")
        }
    }

    if (AppProperties.applications.isEmpty()) {
        Text("There are no applications.")
    } else {
        AppProperties.applications.forEach { (name, path) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$name:$path")
                IconButton(onClick = {
                    AppProperties.applications.remove(name)
                }) {
                    Icon(Icons.Default.Delete, "Delete")
                }
            }
        }
    }

    if (showFields) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newApplicationName,
                onValueChange = {
                    newApplicationName = it
                },
                label = { Text("Application name") }
            )

            Spacer(Modifier.width(MaterialTheme.spacing.small))

            OutlinedTextField(
                value = newApplicationPath,
                onValueChange = {
                    newApplicationPath = it
                },
                label = { Text("Application path") }
            )

            Spacer(Modifier.width(MaterialTheme.spacing.small))

            Button(onClick = {
                AppProperties.applications[newApplicationName] = newApplicationPath
                newApplicationName = ""
                newApplicationPath = ""
            }) {
                Text("Add")
            }
        }
    }
}