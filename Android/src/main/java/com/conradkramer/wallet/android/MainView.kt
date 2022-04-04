package com.conradkramer.wallet.android

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.conradkramer.wallet.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView(mainViewModel: MainViewModel) {
    val navController = rememberNavController()

    LaunchedEffect(true) {
        mainViewModel.bind(navController)
    }

    val currentTab = mainViewModel.selectedTab.collectAsState()
    Scaffold(
        bottomBar = {
            NavigationBar {
                MainViewModel.Tab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab.value == tab,
                        onClick = { mainViewModel.selectedTab.value = tab },
                        icon = { Icon(tab.icon, tab.title) },
                        label = { Text(tab.title) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            modifier = Modifier.padding(padding),
            navController = navController,
            startDestination = currentTab.value.route
        ) {
            composable(MainViewModel.Tab.BALANCE.route) {
                Text("Balance")
            }
            composable(MainViewModel.Tab.COLLECTIBLES.route) {
                Text("NFTs")
            }
            composable(MainViewModel.Tab.TRANSFER.route) {
                Text("Transfer")
            }
            composable(MainViewModel.Tab.UTILITY.route) {
                Text("Utilities")
            }
            composable(MainViewModel.Tab.TRANSACTIONS.route) {
                Text("Profile")
            }
        }
    }
}

suspend fun MainViewModel.bind(navController: NavController) {
    selectedTab.collect { tab ->
        navController.navigate(tab.route) {
            popUpTo(tab.route)
        }
    }
}

val MainViewModel.Tab.route: String
    get() = name.lowercase()

val MainViewModel.Tab.icon: ImageVector
    get() = when (this) {
        MainViewModel.Tab.BALANCE -> Icons.Default.Paid
        MainViewModel.Tab.COLLECTIBLES -> Icons.Default.Dashboard
        MainViewModel.Tab.TRANSFER -> Icons.Default.SwapVert
        MainViewModel.Tab.UTILITY -> Icons.Default.Language
        MainViewModel.Tab.TRANSACTIONS -> Icons.Default.AccountCircle
    }
