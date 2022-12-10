package com.conradkramer.wallet.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.conradkramer.wallet.viewmodel.ImportViewModel
import com.conradkramer.wallet.viewmodel.OnboardingViewModel
import com.conradkramer.wallet.viewmodel.OnboardingViewModel.Screen
import com.conradkramer.wallet.viewmodel.WelcomeViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import org.koin.androidx.compose.get
import org.koin.core.scope.Scope

@Composable
fun OnboardingView(scope: Scope) {
    val navController = rememberNavController()
    val onboardingViewModel: OnboardingViewModel = get(scope = scope)
    val welcomeViewModel: WelcomeViewModel = get(scope = scope)
    val importViewModel: ImportViewModel = get(scope = scope)

    LaunchedEffect(true) {
        onboardingViewModel.bind(navController)
    }
    LaunchedEffect(true) {
        navController.bind(onboardingViewModel)
    }

    NavHost(
        navController = navController,
        startDestination = Screen.WELCOME.route
    ) {
        composable(Screen.WELCOME.route) {
            WelcomeView(welcomeViewModel) { option ->
                when (option) {
                    WelcomeViewModel.Option.IMPORT_PHRASE -> onboardingViewModel.import()
                    else -> {}
                }
            }
        }
        composable(Screen.IMPORT_PHRASE.route) {
            ImportView(importViewModel)
        }
    }
}

suspend fun NavController.bind(viewModel: OnboardingViewModel) {
    visibleEntries.onEach { entries ->
        val screens = viewModel.screens.value
        if (screens != entries.screens) {
            viewModel.screens.compareAndSet(screens, entries.screens)
        }
    }.collect()
}

suspend fun OnboardingViewModel.bind(navController: NavController) {
    screens.collect { screens ->
        if (screens.isEmpty() || navController.visibleEntries.value.screens == screens) {
            return@collect
        }

        if (screens.size < 2) {
            navController.navigate(screens.last().route)
        } else {
            navController.navigate(screens.last().route) {
                popUpTo(screens[screens.lastIndex - 1].route)
            }
        }
    }
}

val List<NavBackStackEntry>.screens
    get() = mapNotNull(NavBackStackEntry::screen)

val NavBackStackEntry.screen: Screen?
    get() = destination.route?.let(Screen.Companion::fromRoute)

fun Screen.Companion.fromRoute(route: String): Screen? {
    return Screen.values().firstOrNull { it.route == route }
}

val Screen.route: String
    get() = name.lowercase()
