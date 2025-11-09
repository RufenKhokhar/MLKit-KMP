package sample.app.facedetection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import sample.app.CameraLayout
import sample.app.barcode.BarcodeScreenAction
import sample.app.utils.CameraPermissionHandler

@Composable
fun FaceDetectionScreen(
    state: FaceDetectionState,
    onAction: (FaceDetectionScreenAction) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {

    var permissionGranted by remember { mutableStateOf(false) }

    if (permissionGranted) {
        Box(modifier = modifier) {
            CameraLayout(
                modifier = Modifier.fillMaxSize(),
                onFrameAvailable = { onAction(FaceDetectionScreenAction.OnOnFrameAvailable(it)) }
            )
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.safeDrawingPadding()
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null)
            }
        }
    } else {
        CameraPermissionHandler(
            onPermissionDenied = onBackClick,
            onPermissionGranted = {
                permissionGranted = true
            }
        )
    }

}