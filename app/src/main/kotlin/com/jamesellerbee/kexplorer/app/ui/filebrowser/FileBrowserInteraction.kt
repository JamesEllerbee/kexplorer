package com.jamesellerbee.kexplorer.app.ui.filebrowser

sealed class FileBrowserInteraction {
    data object GoToParentDirectory : FileBrowserInteraction()
    data object GoToHomeDirectory : FileBrowserInteraction()
    data object RefreshListing : FileBrowserInteraction()

    data class SetWorkingDirectoryText(val newValue: String) : FileBrowserInteraction()
    data class SelectFile(val name: String) : FileBrowserInteraction()
    data class SaveFile(val name: String, val isDirectory: Boolean) : FileBrowserInteraction()
    data class ShowFileContextMenu(val name: String) : FileBrowserInteraction()
    data object DismissFileContextMenu : FileBrowserInteraction()
    data object ShowFileCreationDialog : FileBrowserInteraction()
    data object ShowOpenInDialog : FileBrowserInteraction()

    sealed class FileContextMenuInteraction : FileBrowserInteraction() {
        data class DeleteFile(val fileName: String) : FileContextMenuInteraction()
        data class PreviewFile(val fileName: String) : FileContextMenuInteraction()
    }

    sealed class FileCreationDialogInteraction : FileBrowserInteraction() {
        data object DismissFileCreationDialog : FileCreationDialogInteraction()
    }

    sealed class FilePreviewDialogInteraction : FileBrowserInteraction() {
        data object DismissFilePreviewDialog : FilePreviewDialogInteraction()

    }

    sealed class OpenInDialogInteraction : FileBrowserInteraction() {
        data class OpenFileInApplication(val application: String, val fileName: String) : OpenInDialogInteraction()
        data object DismissOpenInDialog : OpenInDialogInteraction()
    }
}