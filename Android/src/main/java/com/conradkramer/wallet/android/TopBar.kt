@file:OptIn(ExperimentalMaterial3Api::class)

package com.conradkramer.wallet.android

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import com.conradkramer.wallet.viewmodel.MainViewModel

@Composable
fun TopBar(tab: MainViewModel.Tab) {
    when (tab) {
        MainViewModel.Tab.BALANCE -> BalanceTopBar()
        else -> {}
    }
}

@Composable
private fun BalanceTopBar() {
    TopAppBar(
        title = { Text("Balance") },
        actions = {
            IconButton(onClick = {
            }) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = "Options",
                )
            }
        },
    )
}
