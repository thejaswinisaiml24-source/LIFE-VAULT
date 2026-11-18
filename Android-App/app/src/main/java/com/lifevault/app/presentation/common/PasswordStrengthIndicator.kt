package com.lifevault.app.presentation.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lifevault.app.presentation.theme.ErrorRed
import com.lifevault.app.presentation.theme.SuccessGreen
import com.lifevault.app.presentation.theme.WarningAmber

enum class PasswordStrength(val label: String, val fraction: Float) {
    EMPTY("", 0f),
    WEAK("Weak", 0.33f),
    MEDIUM("Medium", 0.66f),
    STRONG("Strong", 1f)
}

fun evaluatePasswordStrength(password: String): PasswordStrength {
    if (password.isEmpty()) return PasswordStrength.EMPTY

    var score = 0
    if (password.length >= 8) score++
    if (password.length >= 12) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { it.isUpperCase() }) score++
    if (password.any { it.isLowerCase() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++

    return when {
        score <= 2 -> PasswordStrength.WEAK
        score <= 4 -> PasswordStrength.MEDIUM
        else -> PasswordStrength.STRONG
    }
}

@Composable
fun PasswordStrengthIndicator(strength: PasswordStrength, modifier: Modifier = Modifier) {
    if (strength == PasswordStrength.EMPTY) return

    val color = when (strength) {
        PasswordStrength.WEAK -> ErrorRed
        PasswordStrength.MEDIUM -> WarningAmber
        PasswordStrength.STRONG -> SuccessGreen
        PasswordStrength.EMPTY -> ErrorRed
    }
    val animatedFraction by animateFloatAsState(
        targetValue = strength.fraction,
        animationSpec = tween(300),
        label = "passwordStrength"
    )

    Column(modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
        Text(
            text = strength.label,
            color = color,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
