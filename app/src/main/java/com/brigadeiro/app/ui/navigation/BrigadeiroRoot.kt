package com.brigadeiro.app.ui.navigation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brigadeiro.app.di.AppContainerProvider
import com.brigadeiro.app.ui.screens.onboarding.OnboardingScreen

/**
 * Decides what to show on launch: the dietary onboarding on first run, or the
 * app itself once the user has answered (or skipped) it.
 */
@Composable
fun BrigadeiroRoot() {
    val context = LocalContext.current
    val container = (context.applicationContext as AppContainerProvider).container
    val profile by container.dietRepository.profile.collectAsStateWithLifecycle(
        initialValue = null,
    )

    val current = profile
    when {
        current == null -> BrigadeiroSplash()
        !current.onboarded -> OnboardingScreen(
            canSkip = true,
            onFinished = { /* gate reacts to the saved profile flow */ },
        )
        else -> BrigadeiroNavHost()
    }
}

@Composable
private fun BrigadeiroSplash() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = "\uD83C\uDF6B", style = MaterialTheme.typography.displaySmall)
        Text(
            text = "Brigadeiro",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
