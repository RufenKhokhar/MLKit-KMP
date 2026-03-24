package sample.app.pose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sample.app.CameraLayout
import sample.app.facedetection.FaceDetectionScreenAction
import sample.app.facedetection.FaceDetectionState
import sample.app.utils.CameraPermissionHandler

@Composable
fun PoseDetectionScreen(
    state: PoseDetectionState,
    onAction: (PoseDetectionAction) -> Unit,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit
) {

    var permissionGranted by remember { mutableStateOf(false) }

    if (permissionGranted) {
        Box(modifier = modifier) {
            CameraLayout(
                modifier = Modifier.fillMaxSize(),
                onFrameAvailable = { onAction(PoseDetectionAction.OnFrameAvailable(it)) }
            )
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.safeDrawingPadding()
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = null)
            }
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(vertical = 16.dp)
                    .height(250.dp)
            ) {
                Text(
                    text = "Pose found: ${state.pose}",
                    modifier = Modifier.padding(8.dp)
                )
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