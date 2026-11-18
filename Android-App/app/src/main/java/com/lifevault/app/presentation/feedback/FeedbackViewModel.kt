package com.lifevault.app.presentation.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class FeedbackUiState(
    val rating: Int = 0,
    val review: String = "",
    val isSubmitting: Boolean = false,
    val submitted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState

    fun reset() { _uiState.value = FeedbackUiState() }

    fun onRatingChange(value: Int) = _uiState.update { it.copy(rating = value, error = null) }
    fun onReviewChange(value: String) = _uiState.update { it.copy(review = value.take(500), error = null) }

    fun submit(source: String) {
        val state = _uiState.value
        if (state.isSubmitting) return
        if (state.rating < 1) {
            _uiState.update { it.copy(error = "Please select a star rating") }
            return
        }

        _uiState.update { it.copy(isSubmitting = true, error = null) }
        viewModelScope.launch {
            try {
                val user = firebaseAuth.currentUser
                firestore.collection("feedback").add(
                    hashMapOf(
                        "uid" to (user?.uid ?: "anonymous"),
                        "email" to (user?.email ?: ""),
                        "rating" to state.rating,
                        "review" to state.review.trim(),
                        "source" to source,
                        "createdAtEpochMillis" to System.currentTimeMillis()
                    )
                ).await()
                _uiState.update { it.copy(isSubmitting = false, submitted = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isSubmitting = false, error = e.message ?: "Could not send feedback. Try again.")
                }
            }
        }
    }
}
