package com.lifevault.app.presentation.passwordvault

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.lifevault.app.data.crypto.VaultCrypto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

private const val VERIFY_TOKEN = "LIFEVAULT_VERIFY_TOKEN"

data class VaultEntry(
    val id: String,
    val siteName: String,
    val username: String,
    val encryptedPassword: String,
    val decryptedPassword: String? = null,
    val revealed: Boolean = false
)

data class PasswordVaultUiState(
    val isLoading: Boolean = true,
    val isVaultSetUp: Boolean = false,
    val isUnlocked: Boolean = false,
    val masterPasswordInput: String = "",
    val confirmMasterPasswordInput: String = "",
    val unlockError: String? = null,
    val entries: List<VaultEntry> = emptyList(),
    val showAddDialog: Boolean = false,
    val newSiteName: String = "",
    val newUsername: String = "",
    val newPassword: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

/**
 * Master password is held ONLY in memory for this ViewModel's lifetime
 * (cleared when the screen/ViewModel is destroyed) — never persisted,
 * never sent anywhere. If forgotten, entries are unrecoverable by design.
 */
@HiltViewModel
class PasswordVaultViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(PasswordVaultUiState())
    val uiState: StateFlow<PasswordVaultUiState> = _uiState

    private var sessionMasterPassword: String? = null
    private var salt: String? = null

    init { checkSetup() }

    private fun configRef() = firebaseAuth.currentUser?.uid?.let { uid ->
        firestore.collection("users").document(uid).collection("vaultMeta").document("config")
    }
    private fun entriesRef() = firebaseAuth.currentUser?.uid?.let { uid ->
        firestore.collection("users").document(uid).collection("passwordVault")
    }

    private fun checkSetup() {
        val ref = configRef() ?: run {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        viewModelScope.launch {
            try {
                val doc = ref.get().await()
                if (doc.exists()) {
                    salt = doc.getString("salt")
                    _uiState.update { it.copy(isVaultSetUp = true, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isVaultSetUp = false, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onMasterPasswordChange(v: String) = _uiState.update { it.copy(masterPasswordInput = v, unlockError = null) }
    fun onConfirmMasterPasswordChange(v: String) = _uiState.update { it.copy(confirmMasterPasswordInput = v, unlockError = null) }

    fun setUpVault() {
        val state = _uiState.value
        if (state.masterPasswordInput.length < 6) {
            _uiState.update { it.copy(unlockError = "Master password must be at least 6 characters") }
            return
        }
        if (state.masterPasswordInput != state.confirmMasterPasswordInput) {
            _uiState.update { it.copy(unlockError = "Passwords do not match") }
            return
        }
        val ref = configRef() ?: return

        viewModelScope.launch {
            try {
                val newSalt = VaultCrypto.generateSalt()
                val verifyToken = VaultCrypto.encrypt(VERIFY_TOKEN, state.masterPasswordInput, newSalt)
                ref.set(mapOf("salt" to newSalt, "verifyToken" to verifyToken)).await()

                salt = newSalt
                sessionMasterPassword = state.masterPasswordInput
                _uiState.update { it.copy(isVaultSetUp = true, isUnlocked = true, masterPasswordInput = "", confirmMasterPasswordInput = "") }
                loadEntries()
            } catch (e: Exception) {
                _uiState.update { it.copy(unlockError = e.message ?: "Failed to set up vault") }
            }
        }
    }

    fun unlock() {
        val state = _uiState.value
        val ref = configRef() ?: return

        viewModelScope.launch {
            try {
                val doc = ref.get().await()
                val storedSalt = doc.getString("salt") ?: return@launch
                val verifyToken = doc.getString("verifyToken") ?: return@launch

                val decrypted = VaultCrypto.decrypt(verifyToken, state.masterPasswordInput, storedSalt)
                if (decrypted != VERIFY_TOKEN) {
                    _uiState.update { it.copy(unlockError = "Incorrect master password") }
                    return@launch
                }

                salt = storedSalt
                sessionMasterPassword = state.masterPasswordInput
                _uiState.update { it.copy(isUnlocked = true, masterPasswordInput = "") }
                loadEntries()
            } catch (e: Exception) {
                _uiState.update { it.copy(unlockError = e.message ?: "Failed to unlock") }
            }
        }
    }

    private fun loadEntries() {
        val ref = entriesRef() ?: return
        viewModelScope.launch {
            try {
                val snapshot = ref.get().await()
                val entries = snapshot.documents.mapNotNull { doc ->
                    val site = doc.getString("siteName") ?: return@mapNotNull null
                    VaultEntry(
                        id = doc.id,
                        siteName = site,
                        username = doc.getString("username") ?: "",
                        encryptedPassword = doc.getString("encryptedPassword") ?: ""
                    )
                }
                _uiState.update { it.copy(entries = entries) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun onOpenAddDialog() = _uiState.update { it.copy(showAddDialog = true, newSiteName = "", newUsername = "", newPassword = "") }
    fun onDismissAddDialog() = _uiState.update { it.copy(showAddDialog = false) }
    fun onNewSiteNameChange(v: String) = _uiState.update { it.copy(newSiteName = v) }
    fun onNewUsernameChange(v: String) = _uiState.update { it.copy(newUsername = v) }
    fun onNewPasswordChange(v: String) = _uiState.update { it.copy(newPassword = v) }

    fun addEntry() {
        val ref = entriesRef() ?: return
        val masterPw = sessionMasterPassword ?: return
        val currentSalt = salt ?: return
        val state = _uiState.value

        if (state.newSiteName.isBlank() || state.newPassword.isBlank()) {
            _uiState.update { it.copy(error = "Site name and password are required") }
            return
        }
        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                val encrypted = VaultCrypto.encrypt(state.newPassword, masterPw, currentSalt)
                ref.add(
                    mapOf(
                        "siteName" to state.newSiteName.trim(),
                        "username" to state.newUsername.trim(),
                        "encryptedPassword" to encrypted
                    )
                ).await()
                _uiState.update { it.copy(isSaving = false, showAddDialog = false) }
                loadEntries()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save") }
            }
        }
    }

    fun toggleReveal(entryId: String) {
        val masterPw = sessionMasterPassword ?: return
        val currentSalt = salt ?: return
        _uiState.update { state ->
            state.copy(
                entries = state.entries.map { entry ->
                    if (entry.id == entryId) {
                        if (entry.revealed) {
                            entry.copy(revealed = false, decryptedPassword = null)
                        } else {
                            val decrypted = VaultCrypto.decrypt(entry.encryptedPassword, masterPw, currentSalt)
                            entry.copy(revealed = true, decryptedPassword = decrypted)
                        }
                    } else entry
                }
            )
        }
    }

    fun deleteEntry(entryId: String) {
        val ref = entriesRef() ?: return
        viewModelScope.launch {
            try {
                ref.document(entryId).delete().await()
                loadEntries()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sessionMasterPassword = null
    }
}
