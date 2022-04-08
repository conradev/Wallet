package com.conradkramer.wallet.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.conradkramer.wallet.viewmodel.MainViewModel
import org.koin.android.ext.android.inject
import org.koin.android.scope.AndroidScopeComponent
import org.koin.androidx.scope.activityRetainedScope
import org.koin.core.scope.Scope

class MainActivity : AppCompatActivity(), AndroidScopeComponent {

    override val scope: Scope by activityRetainedScope()

    private val mainViewModel: MainViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainView(mainViewModel)
        }

        if (mainViewModel.showOnboarding.value) {
            showOnboarding()
        }
    }

    private fun showOnboarding() {
        val intent = Intent(this, OnboardingActivity::class.java)
            .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION }
        startActivity(intent)
    }
}
