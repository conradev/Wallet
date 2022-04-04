package com.conradkramer.wallet.android

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Input
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.conradkramer.wallet.PreviewMocks
import com.conradkramer.wallet.viewmodel.WelcomeViewModel
import com.conradkramer.wallet.viewmodel.title

@Composable
fun WelcomeView(viewModel: WelcomeViewModel, onClick: (WelcomeViewModel.Option) -> Unit = { }) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(viewModel.title, style = MaterialTheme.typography.headlineLarge)
            Text(viewModel.subtitle, style = MaterialTheme.typography.headlineSmall)
            viewModel.options.forEach { option ->
                Button(onClick = { onClick(option) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Image(
                            option.icon,
                            null,
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(option.title, style = MaterialTheme.typography.titleLarge)
                    }
                }
            }
        }
    }
}

val WelcomeViewModel.Option.icon: ImageVector
    get() = when (this) {
        WelcomeViewModel.Option.IMPORT_PHRASE -> Icons.Default.Input
        WelcomeViewModel.Option.GENERATE -> Icons.Default.AddCircleOutline
    }

@Preview(showSystemUi = true)
@Composable
fun WelcomeViewPreview() {
    WelcomeView(PreviewMocks.get())
}
