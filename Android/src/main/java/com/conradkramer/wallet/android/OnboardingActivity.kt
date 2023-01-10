package com.conradkramer.wallet.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import com.conradkramer.wallet.viewmodel.MainViewModel
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityRetainedScope
import org.koin.core.scope.Scope

class OnboardingActivity : AppCompatActivity(), AndroidScopeComponent {

    override val scope: Scope by activityRetainedScope()

    private val mainViewModel: MainViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            LaunchedEffect(true) {
                mainViewModel.showOnboarding.collect { show ->
                    if (!show) { finishOnboarding() }
                }
            }
            OnboardingView(scope)
        }
    }

    private fun finishOnboarding() {
        val intent = Intent(this, MainActivity::class.java)
            .apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK or
                    Intent.FLAG_ACTIVITY_NO_ANIMATION
            }
        startActivity(intent)
    }
}
