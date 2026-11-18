package com.lifevault.app.presentation.medicinereminder

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lifevault.app.data.notification.MedicineReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class MedicineRecord(
    val id: String,
    val name: String,
    val dose: String,
    val time: String,
    val frequency: String,
    val taken: Boolean
)

data class MedicineReminderUiState(
    val medicines: List<MedicineRecord> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val newName: String = "",
    val newDose: String = "",
    val newTime: String = "",
    val newFrequency: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

private const val USERS_COLLECTION = "users"
private const val SUBCOLLECTION = "medicines"

@HiltViewModel
class MedicineReminderViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicineReminderUiState())
    val uiState: StateFlow<MedicineReminderUiState> = _uiState

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
                val meds = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("name") ?: return@mapNotNull null
                    MedicineRecord(
                        id = doc.id,
                        name = name,
                        dose = doc.getString("dose") ?: "",
                        time = doc.getString("time") ?: "",
                        frequency = doc.getString("frequency") ?: "",
                        taken = doc.getBoolean("taken") ?: false
                    )
                }
                _uiState.update { it.copy(medicines = meds, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onOpenAddDialog() = _uiState.update { it.copy(showAddDialog = true, newName = "", newDose = "", newTime = "", newFrequency = "") }
    fun onDismissAddDialog() = _uiState.update { it.copy(showAddDialog = false) }
    fun onNewNameChange(v: String) = _uiState.update { it.copy(newName = v) }
    fun onNewDoseChange(v: String) = _uiState.update { it.copy(newDose = v) }
    fun onNewTimeChange(v: String) = _uiState.update { it.copy(newTime = v) }
    fun onNewFrequencyChange(v: String) = _uiState.update { it.copy(newFrequency = v) }

    fun addMedicine() {
        val ref = collectionRef() ?: return
        val state = _uiState.value
        if (state.newName.isBlank()) {
            _uiState.update { it.copy(error = "Medicine name is required") }
            return
        }
        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val data = hashMapOf(
                    "name" to state.newName.trim(),
                    "dose" to state.newDose.trim(),
                    "time" to state.newTime.trim(),
                    "frequency" to state.newFrequency.trim(),
                    "taken" to false,
                    "createdAtEpochMillis" to System.currentTimeMillis()
                )
                val docRef = ref.add(data).await()

                var reminderScheduled = false
                if (state.newTime.isNotBlank()) {
                    reminderScheduled = MedicineReminderScheduler.schedule(
                        context = appContext,
                        medicineId = docRef.id,
                        name = state.newName.trim(),
                        dose = state.newDose.trim(),
                        timeText = state.newTime.trim()
                    )
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        showAddDialog = false,
                        error = if (state.newTime.isNotBlank() && !reminderScheduled) {
                            "Saved, but reminder time wasn't understood — use format like 08:00 AM"
                        } else null
                    )
                }
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save") }
            }
        }
    }

    fun toggleTaken(id: String, currentlyTaken: Boolean) {
        val ref = collectionRef() ?: return
        viewModelScope.launch {
            try {
                ref.document(id).update("taken", !currentlyTaken).await()
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteMedicine(id: String) {
        val ref = collectionRef() ?: return
        viewModelScope.launch {
            try {
                ref.document(id).delete().await()
                MedicineReminderScheduler.cancel(appContext, id)
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
