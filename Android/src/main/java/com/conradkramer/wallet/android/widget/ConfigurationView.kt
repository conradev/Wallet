@file:OptIn(ExperimentalGlanceRemoteViewsApi::class, ExperimentalMaterial3Api::class)

package com.conradkramer.wallet.android.widget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.ExperimentalGlanceRemoteViewsApi
import com.google.android.glance.appwidget.configuration.AppWidgetConfigurationScaffold
import com.google.android.glance.appwidget.configuration.AppWidgetConfigurationState
import com.google.android.glance.appwidget.configuration.rememberAppWidgetConfigurationState
import kotlinx.coroutines.launch

@Composable
internal fun ConfigurationView() {
    val scope = rememberCoroutineScope()
    val configurationState = rememberAppWidgetConfigurationState(BalanceWidget())

    if (configurationState.glanceId == null) {
        configurationState.discardConfiguration()
        return
    }

    AppWidgetConfigurationScaffold(
        appWidgetConfigurationState = configurationState,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                scope.launch {
                    configurationState.applyConfiguration()
                }
            }) {
                Icon(imageVector = Icons.Rounded.Done, contentDescription = "Save")
            }
        }
    ) {
        ConfigurationList(Modifier.padding(it), configurationState)
    }
}

@Composable
private fun ConfigurationList(modifier: Modifier, state: AppWidgetConfigurationState) {
    fun <T> updatePreferences(key: Preferences.Key<T>, value: T) {
        state.updateCurrentState<Preferences> {
            it.toMutablePreferences().apply {
                set(key, value)
            }.toPreferences()
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Setup counter:")
        IconButton(onClick = {
        }) {
            Icon(imageVector = Icons.Rounded.Add, contentDescription = "Add")
        }
        Text("Unknown")
        IconButton(onClick = {
        }) {
            Icon(imageVector = Icons.Rounded.Remove, contentDescription = "Subtract")
        }
    }
}
