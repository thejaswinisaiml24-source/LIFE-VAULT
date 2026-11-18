package com.lifevault.app.presentation.insurance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class InsuranceRecord(
    val id: String,
    val type: String,
    val company: String,
    val policyNumber: String,
    val validTill: String
)

data class InsuranceUiState(
    val policies: List<InsuranceRecord> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val newType: String = "",
    val newCompany: String = "",
    val newPolicyNumber: String = "",
    val newValidTill: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

private const val USERS_COLLECTION = "users"
private const val SUBCOLLECTION = "insurance"

@HiltViewModel
class InsuranceViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsuranceUiState())
    val uiState: StateFlow<InsuranceUiState> = _uiState

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
                val snapshot = ref.orderBy("createdAtEpochMillis", Query.Direction.DESCENDING).get().await()
                val policies = snapshot.documents.mapNotNull { doc ->
                    val type = doc.getString("type") ?: return@mapNotNull null
                    InsuranceRecord(
                        id = doc.id,
                        type = type,
                        company = doc.getString("company") ?: "",
                        policyNumber = doc.getString("policyNumber") ?: "",
                        validTill = doc.getString("validTill") ?: ""
                    )
                }
                _uiState.update { it.copy(policies = policies, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onOpenAddDialog() = _uiState.update { it.copy(showAddDialog = true, newType = "", newCompany = "", newPolicyNumber = "", newValidTill = "") }
    fun onDismissAddDialog() = _uiState.update { it.copy(showAddDialog = false) }
    fun onNewTypeChange(v: String) = _uiState.update { it.copy(newType = v) }
    fun onNewCompanyChange(v: String) = _uiState.update { it.copy(newCompany = v) }
    fun onNewPolicyNumberChange(v: String) = _uiState.update { it.copy(newPolicyNumber = v) }
    fun onNewValidTillChange(v: String) = _uiState.update { it.copy(newValidTill = v) }

    fun addPolicy() {
        val ref = collectionRef() ?: return
        val state = _uiState.value
        if (state.newType.isBlank() || state.newCompany.isBlank()) {
            _uiState.update { it.copy(error = "Type and company are required") }
            return
        }
        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val data = hashMapOf(
                    "type" to state.newType.trim(),
                    "company" to state.newCompany.trim(),
                    "policyNumber" to state.newPolicyNumber.trim(),
                    "validTill" to state.newValidTill.trim(),
                    "createdAtEpochMillis" to System.currentTimeMillis()
                )
                ref.add(data).await()
                _uiState.update { it.copy(isSaving = false, showAddDialog = false) }
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save") }
            }
        }
    }

    fun deletePolicy(id: String) {
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
