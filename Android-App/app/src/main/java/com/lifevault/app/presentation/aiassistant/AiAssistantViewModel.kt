package com.lifevault.app.presentation.aiassistant

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifevault.app.data.ai.GeminiClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(val role: String, val text: String)

data class AiAssistantUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val error: String? = null
)

private const val SYSTEM_CONTEXT = """You are the LifeVault AI Health Assistant, embedded in a personal
emergency and medical records app. Help the user understand medicines, summarize health info they
describe, and give general safety/emergency guidance. You are NOT a doctor — for anything serious,
clearly tell the user to consult a real medical professional or call emergency services. Keep answers
short, clear, and calm."""

@HiltViewModel
class AiAssistantViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AiAssistantUiState())
    val uiState: StateFlow<AiAssistantUiState> = _uiState

    fun onInputChange(value: String) = _uiState.update { it.copy(inputText = value, error = null) }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        val updatedMessages = _uiState.value.messages + ChatMessage("user", text)
        _uiState.update { it.copy(messages = updatedMessages, inputText = "", isSending = true, error = null) }

        viewModelScope.launch {
            val conversation = updatedMessages.map { it.role to it.text }
            val result = GeminiClient.sendMessage(conversation, SYSTEM_CONTEXT)
            result.fold(
                onSuccess = { reply ->
                    _uiState.update {
                        it.copy(messages = it.messages + ChatMessage("model", reply), isSending = false)
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(isSending = false, error = error.message ?: "Failed to get a response")
                    }
                }
            )
        }
    }
}
