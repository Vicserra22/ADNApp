package com.adn.adnapp.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.adn.adnapp.R

@Composable
internal fun AreaSwitchButton(onOpen: () -> Unit, onCycle: () -> Unit) {
    val currentOpen by rememberUpdatedState(onOpen)
    val currentCycle by rememberUpdatedState(onCycle)
    val haptics = LocalHapticFeedback.current
    val base = LocalViewConfiguration.current
    val config = remember(base) {
        object : ViewConfiguration by base { override val longPressTimeoutMillis: Long = 1500L }
    }
    CompositionLocalProvider(LocalViewConfiguration provides config) {
        Surface(
            modifier = Modifier.size(80.dp).semantics {
                contentDescription = "Cambiar área"
                role = Role.Button
                onClick("Elegir área") { currentOpen(); true }
                onLongClick("Siguiente área") { currentCycle(); true }
            }.pointerInput(Unit) {
                detectTapGestures(
                    onTap = { currentOpen() },
                    onLongPress = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentCycle()
                    }
                )
            },
            shape = CellShape(4),
            color = MaterialTheme.colorScheme.primary,
            border = BorderStroke(3.dp, MaterialTheme.colorScheme.primaryContainer),
            shadowElevation = 5.dp
        ) {
            Image(painterResource(R.drawable.adn_logo), null, Modifier.padding(17.dp))
        }
    }
}

