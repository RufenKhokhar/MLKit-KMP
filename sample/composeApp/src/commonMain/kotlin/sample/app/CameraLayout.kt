package sample.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.rufenkhokhar.corevision.PlatformImage


@Composable
expect fun CameraLayout(
    modifier: Modifier = Modifier,
    onFrameAvailable: (PlatformImage) -> Unit
)

