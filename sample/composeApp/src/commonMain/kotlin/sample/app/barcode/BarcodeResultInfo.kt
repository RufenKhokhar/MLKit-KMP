package sample.app.barcode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.rufenkhokhar.barcode.ScanResult
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeResultInfo(
    onDismiss: () -> Unit,
    barcodes: List<ScanResult>
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    ModalBottomSheet(onDismissRequest = onDismiss) {
        BarcodeResultInfoLayout(
            modifier = Modifier.fillMaxWidth(),
            barcodes = barcodes,
            onDismiss = {
                scope.launch {
                    sheetState.hide()
                }.invokeOnCompletion {
                    onDismiss()
                }
            }
        )
    }
}

@Composable
fun BarcodeResultInfoLayout(
    modifier: Modifier = Modifier,
    barcodes: List<ScanResult>,
    onDismiss: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp)
        ) {
            items(items = barcodes) {
                Card(
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxSize()) {
                        Text(text = it.displayValue ?: "")
                        Text(text = it.rawValue ?: "")
                    }
                }
            }
        }
        Button(
            onClick = onDismiss,
            modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth()
        ) {
            Text(text = "Dismiss")
        }

    }


}