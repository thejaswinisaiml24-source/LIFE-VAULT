package com.lifevault.app.presentation.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifevault.app.presentation.common.LifeVaultGradientButton
import com.lifevault.app.presentation.theme.SuccessGreen
import com.lifevault.app.presentation.theme.WarningAmber

@Composable
fun StarRatingBar(
    rating: Int,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            IconButton(onClick = { onRatingChange(i) }) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "$i star",
                    tint = if (i <= rating) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(38.dp)
                )
            }
        }
    }
}

private fun ratingLabel(rating: Int) = when (rating) {
    1 -> "Poor"
    2 -> "Fair"
    3 -> "Good"
    4 -> "Very good"
    5 -> "Excellent"
    else -> "Tap a star to rate"
}

/** Full-screen feedback form, opened from Settings > Send Feedback. */
@Composable
fun FeedbackScreen(
    onNavigateBack: () -> Unit,
    viewModel: FeedbackViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
            }
            Text(
                text = "Send Feedback",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(Modifier.height(24.dp))

        if (state.submitted) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(72.dp))
                Spacer(Modifier.height(16.dp))
                Text(
                    "Thank you for your feedback!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Your rating and review help us improve LifeVault.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(28.dp))
                LifeVaultGradientButton(text = "Done", onClick = onNavigateBack)
            }
        } else {
            Text(
                "How would you rate your experience with LifeVault?",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            StarRatingBar(rating = state.rating, onRatingChange = viewModel::onRatingChange)
            Text(
                ratingLabel(state.rating),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp)
            )
            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = state.review,
                onValueChange = viewModel::onReviewChange,
                label = { Text("Write a review (optional)") },
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "${state.review.length}/500",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
            )

            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(24.dp))
            LifeVaultGradientButton(
                text = "Submit Feedback",
                onClick = { viewModel.submit("settings") },
                enabled = !state.isSubmitting,
                isLoading = state.isSubmitting
            )
        }
    }
}

/** Compact version shown right after an SOS alert is stopped. */
@Composable
fun FeedbackDialog(
    source: String,
    onDismiss: () -> Unit,
    viewModel: FeedbackViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.reset() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (state.submitted) "Thank you!" else "How was your SOS experience?")
        },
        text = {
            if (state.submitted) {
                Text("Your feedback has been sent. Stay safe!")
            } else {
                Column {
                    StarRatingBar(rating = state.rating, onRatingChange = viewModel::onRatingChange)
                    OutlinedTextField(
                        value = state.review,
                        onValueChange = viewModel::onReviewChange,
                        label = { Text("Review (optional)") },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth()
                    )
                    state.error?.let {
                        Spacer(Modifier.height(6.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            if (state.submitted) {
                TextButton(onClick = onDismiss) { Text("Close") }
            } else {
                TextButton(onClick = { viewModel.submit(source) }, enabled = !state.isSubmitting) {
                    Text(if (state.isSubmitting) "Sending..." else "Submit")
                }
            }
        },
        dismissButton = {
            if (!state.submitted) {
                TextButton(onClick = onDismiss) { Text("Skip") }
            }
        }
    )
}
