package sample.app.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import googlemlkit_kmp.sample.composeapp.generated.resources.Res
import googlemlkit_kmp.sample.composeapp.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource
import sample.app.nav.NavigationItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onItemClick: (NavigationItem) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(Res.string.app_name))
                }
            )
        }
    ) { paddingValues ->
        FeatureListScreen(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            onItemClick = onItemClick
        )

    }

}