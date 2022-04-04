package com.conradkramer.wallet.android

import android.view.KeyCharacterMap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Input
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.conradkramer.wallet.PreviewMocks
import com.conradkramer.wallet.viewmodel.ImportViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ImportView(viewModel: ImportViewModel) {
    var phrase by remember { mutableStateOf("") }
    val import = { viewModel.import(phrase) }
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Input,
                null,
                Modifier.size(48.dp)
            )
            Text(viewModel.title, style = MaterialTheme.typography.headlineLarge)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().onPreviewKeyEvent {
                    val unicodeChar = it.nativeKeyEvent.unicodeChar
                    if (unicodeChar and KeyCharacterMap.COMBINING_ACCENT != 0) {
                        return@onPreviewKeyEvent false
                    }
                    if (Character.toChars(unicodeChar).size > 1) {
                        return@onPreviewKeyEvent false
                    }
                    return@onPreviewKeyEvent !viewModel.accept(Char(unicodeChar))
                },
                placeholder = { Text(viewModel.placeholder) },
                singleLine = false,
                value = phrase,
                onValueChange = { phrase = viewModel.clean(it) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { import() })
            )
            Button(
                onClick = import,
                enabled = viewModel.validate(phrase)
            ) {
                Text(viewModel.action, style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun ImportViewPreview() {
    ImportView(PreviewMocks.get())
}
