package sample.app.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import sample.app.nav.NavigationItem

data class FeatureItem(
    val category: String,
    val name: String,
    val status: String,
    val isHeader: Boolean = false,
    val navigationItem: NavigationItem? = null
)

val featureList = listOf(
    FeatureItem("Vision", "Vision", "", true),
    FeatureItem("Vision", "Barcode Scanning", "✅ Available", navigationItem = NavigationItem.Barcode),
    FeatureItem("Vision", "Text Recognition v2", "⏳ Planned"),
    FeatureItem("Vision", "Face Detection", "✅ Available", navigationItem = NavigationItem.FaceDetection),
    FeatureItem("Vision", "Face Mesh Detection", "⏳ Planned"),
    FeatureItem("Vision", "Pose Detection", "⏳ Planned"),
    FeatureItem("Vision", "Selfie Segmentation", "⏳ Planned"),
    FeatureItem("Vision", "Subject Segmentation", "⏳ Planned"),
    FeatureItem("Vision", "Document Scanner", "⏳ Planned"),
    FeatureItem("Vision", "Image Labeling", "⏳ Planned"),
    FeatureItem("Vision", "Object Detection & Tracking", "⏳ Planned"),
    FeatureItem("Vision", "Digital Ink Recognition", "⏳ Planned"),

    FeatureItem("Natural Language", "Natural Language", "", true),
    FeatureItem("Natural Language", "Language Identification", "⏳ Planned"),
    FeatureItem("Natural Language", "Translation", "⏳ Planned"),
    FeatureItem("Natural Language", "Smart Reply", "⏳ Planned"),
    FeatureItem("Natural Language", "Entity Extraction", "⏳ Planned")
)

@Composable
fun FeatureListScreen(
    modifier: Modifier,
    onItemClick: (NavigationItem) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(featureList) { item ->
            if (item.isHeader) {
                Text(
                    text = item.category,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                )
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    onClick = {
                        if (item.navigationItem != null) {
                            onItemClick(item.navigationItem)
                        } else {
                            println("Coming soon")
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = item.status,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (item.status.contains("✅")) Color(0xFF2E7D32)
                                else Color(0xFFF9A825)
                            )
                        )
                    }
                }

            }
        }
    }
}
