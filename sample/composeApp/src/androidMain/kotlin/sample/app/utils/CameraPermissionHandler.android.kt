package sample.app.utils

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun CameraPermissionHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    val cameraPermissionHandler =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                onPermissionGranted()
            } else {
                onPermissionDenied()
            }
        }
    LaunchedEffect(Unit) {
        val cameraPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
        if (cameraPermission == PackageManager.PERMISSION_GRANTED) {
            onPermissionGranted()
        } else {
            cameraPermissionHandler.launch(Manifest.permission.CAMERA)
        }
    }
}