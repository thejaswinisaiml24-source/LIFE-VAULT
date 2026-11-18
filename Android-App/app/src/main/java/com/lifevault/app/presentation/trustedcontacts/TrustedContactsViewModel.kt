package com.lifevault.app.presentation.trustedcontacts

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

data class TrustedContact(
    val id: String,
    val name: String,
    val phone: String,
    val relationship: String
)

data class TrustedContactsUiState(
    val contacts: List<TrustedContact> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val newName: String = "",
    val newPhone: String = "",
    val newRelationship: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

private const val USERS_COLLECTION = "users"
private const val SUBCOLLECTION = "trustedContacts"

@HiltViewModel
class TrustedContactsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrustedContactsUiState())
    val uiState: StateFlow<TrustedContactsUiState> = _uiState

    init { load() }

    private fun collectionRef() = firebaseAuth.currentUser?.uid?.let { uid ->
        firestore.collection(USERS_COLLECTION).document(uid).collection(SUBCOLLECTION)
    }

    private fun load() {
        val ref = collectionRef() ?: run {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        viewModelScope.launch {
            try {
                val snapshot = ref.get().await()
                val contacts = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    TrustedContact(
                        id = doc.id,
                        name = name,
                        phone = doc.getString("phone") ?: "",
                        relationship = doc.getString("relationship") ?: ""
                    )
                }
                _uiState.update { it.copy(contacts = contacts, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onOpenAddDialog() = _uiState.update { it.copy(showAddDialog = true, newName = "", newPhone = "", newRelationship = "") }
    fun onDismissAddDialog() = _uiState.update { it.copy(showAddDialog = false) }
    fun onNewNameChange(v: String) = _uiState.update { it.copy(newName = v) }
    fun onNewPhoneChange(v: String) = _uiState.update { it.copy(newPhone = v) }
    fun onNewRelationshipChange(v: String) = _uiState.update { it.copy(newRelationship = v) }

    fun addContact() {
        val ref = collectionRef() ?: return
        val state = _uiState.value
        if (state.newName.isBlank() || state.newPhone.isBlank()) {
            _uiState.update { it.copy(error = "Name and phone are required") }
            return
        }
        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                ref.add(
                    hashMapOf(
                        "name" to state.newName.trim(),
                        "phone" to state.newPhone.trim(),
                        "relationship" to state.newRelationship.trim()
                    )
                ).await()
                _uiState.update { it.copy(isSaving = false, showAddDialog = false) }
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save") }
            }
        }
    }

    fun deleteContact(id: String) {
        val ref = collectionRef() ?: return
        viewModelScope.launch {
            try {
                ref.document(id).delete().await()
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
