package sample.app.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun CameraPermissionHandler(onPermissionGranted: () -> Unit,onPermissionDenied: () -> Unit)