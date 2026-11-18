package com.lifevault.app.presentation.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.lifevault.app.presentation.theme.DeepViolet
import com.lifevault.app.presentation.theme.RoyalPurple
import com.lifevault.app.presentation.theme.TextPrimary

@Composable
fun LifeVaultGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = if (enabled) {
                    Brush.horizontalGradient(listOf(RoyalPurple, DeepViolet))
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            RoyalPurple.copy(alpha = 0.4f),
                            DeepViolet.copy(alpha = 0.4f)
                        )
                    )
                }
            )
            .clickable(
                enabled = enabled && !isLoading,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = TextPrimary,
                strokeWidth = 2.dp,
                modifier = Modifier.height(24.dp)
            )
        } else {
            Text(
                text = text,
                color = TextPrimary,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
