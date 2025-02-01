@file:OptIn(ExperimentalMaterial3Api::class)

package com.conradkramer.wallet.android

import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.conradkramer.wallet.viewmodel.BalancesViewModel
import com.conradkramer.wallet.viewmodel.MainViewModel
import org.koin.compose.koinInject
import kotlin.math.ln

@Composable
fun MainView(mainViewModel: MainViewModel) {
    val navController = rememberNavController()

    LaunchedEffect(true) {
        mainViewModel.bind(navController)
    }

    val colorScheme = MaterialTheme.colorScheme
    val currentTab = mainViewModel.selectedTab.collectAsState()
    val balancesViewModel: BalancesViewModel = koinInject()
    Scaffold(
        modifier = Modifier
            .systemBarsPadding(),
        topBar = { TopBar(currentTab.value) },
        bottomBar = {
            NavigationBar {
                MainViewModel.Tab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab.value == tab,
                        onClick = { mainViewModel.selectedTab.value = tab },
                        icon = { Icon(tab.icon, tab.title) },
                        label = { Text(tab.title) },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = currentTab.value.route,
        ) {
            composable(MainViewModel.Tab.BALANCE.route) {
                BalancesView(viewModel = balancesViewModel, padding)
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

/**
 * Copied from the MaterialTheme source code. I need the raw
 * color in order to make the bottom navigation bar match the app's
 * elevated bottom bar
 */
private fun ColorScheme.surfaceColorAtElevation(
    elevation: Dp,
): Color {
    if (elevation == 0.dp) return surface
    val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
    return surfaceTint.copy(alpha = alpha).compositeOver(surface)
}
