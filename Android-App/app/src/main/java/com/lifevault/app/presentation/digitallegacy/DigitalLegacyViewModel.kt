package com.lifevault.app.presentation.digitallegacy

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

enum class LegacyCategory(val label: String) {
    PERSONAL_MESSAGES("Personal Messages"),
    IMPORTANT_ACCOUNTS("Important Accounts"),
    LETTERS("Letters"),
    WILL_AND_DOCUMENTS("Will & Documents"),
    PHOTOS_VIDEOS("Photos & Videos")
}

data class LegacyItem(val id: String, val title: String, val notes: String, val fileUrl: String? = null)

data class DigitalLegacyUiState(
    val counts: Map<LegacyCategory, Int> = emptyMap(),
    val selectedCategory: LegacyCategory? = null,
    val itemsInCategory: List<LegacyItem> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val newTitle: String = "",
    val newNotes: String = "",
    val pickedFileUri: android.net.Uri? = null,
    val pickedFileName: String? = null,
    val isSaving: Boolean = false,
    val error: String? = null
)

private const val USERS_COLLECTION = "users"
private const val SUBCOLLECTION = "digitalLegacy"

/**
 * Photos & Videos isn't included as a category here — that needs real file
 * storage (Firebase Storage), same scope note as Documents. The other four
 * categories are text-record based and fully real (Firestore CRUD).
 */
@HiltViewModel
class DigitalLegacyViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: com.google.firebase.storage.FirebaseStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(DigitalLegacyUiState())
    val uiState: StateFlow<DigitalLegacyUiState> = _uiState

    init { loadCounts() }

    private fun categoryCollectionRef(category: LegacyCategory) = firebaseAuth.currentUser?.uid?.let { uid ->
        firestore.collection(USERS_COLLECTION).document(uid)
            .collection(SUBCOLLECTION).document(category.name)
            .collection("items")
    }

    private fun loadCounts() {
        val uid = firebaseAuth.currentUser?.uid ?: run {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        viewModelScope.launch {
            try {
                val counts = mutableMapOf<LegacyCategory, Int>()
                LegacyCategory.entries.forEach { category ->
                    val snapshot = firestore.collection(USERS_COLLECTION).document(uid)
                        .collection(SUBCOLLECTION).document(category.name)
                        .collection("items").get().await()
                    counts[category] = snapshot.size()
                }
                _uiState.update { it.copy(counts = counts, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onSelectCategory(category: LegacyCategory) {
        _uiState.update { it.copy(selectedCategory = category, isLoading = true) }
        val ref = categoryCollectionRef(category) ?: return
        viewModelScope.launch {
            try {
                val snapshot = ref.get().await()
                val items = snapshot.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    LegacyItem(id = doc.id, title = title, notes = doc.getString("notes") ?: "", fileUrl = doc.getString("fileUrl"))
                }
                _uiState.update { it.copy(itemsInCategory = items, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onBackToCategories() = _uiState.update { it.copy(selectedCategory = null, itemsInCategory = emptyList()) }
    fun onOpenAddDialog() = _uiState.update { it.copy(showAddDialog = true, newTitle = "", newNotes = "", pickedFileUri = null, pickedFileName = null) }
    fun onDismissAddDialog() = _uiState.update { it.copy(showAddDialog = false) }
    fun onNewTitleChange(v: String) = _uiState.update { it.copy(newTitle = v) }
    fun onNewNotesChange(v: String) = _uiState.update { it.copy(newNotes = v) }

    fun onFilePicked(uri: android.net.Uri, contentResolver: android.content.ContentResolver) {
        val name = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
        } ?: uri.lastPathSegment
        _uiState.update { it.copy(pickedFileUri = uri, pickedFileName = name) }
    }

    fun addItem() {
        val category = _uiState.value.selectedCategory ?: return
        val ref = categoryCollectionRef(category) ?: return
        val uid = firebaseAuth.currentUser?.uid ?: return
        val state = _uiState.value
        if (state.newTitle.isBlank()) {
            _uiState.update { it.copy(error = "Title is required") }
            return
        }
        _uiState.update { it.copy(isSaving = true, error = null) }

        viewModelScope.launch {
            try {
                var fileUrl: String? = null
                val pickedUri = state.pickedFileUri
                if (pickedUri != null) {
                    val storageRef = firebaseStorage.reference
                        .child("users/$uid/digitalLegacy/${category.name}/${System.currentTimeMillis()}_${state.pickedFileName ?: "file"}")
                    storageRef.putFile(pickedUri).await()
                    fileUrl = storageRef.downloadUrl.await().toString()
                }

                val data = hashMapOf(
                    "title" to state.newTitle.trim(),
                    "notes" to state.newNotes.trim(),
                    "createdAtEpochMillis" to System.currentTimeMillis()
                )
                if (fileUrl != null) data["fileUrl"] = fileUrl

                ref.add(data).await()
                _uiState.update { it.copy(isSaving = false, showAddDialog = false) }
                onSelectCategory(category)
                loadCounts()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message ?: "Failed to save") }
            }
        }
    }

    fun deleteItem(id: String) {
        val category = _uiState.value.selectedCategory ?: return
        val ref = categoryCollectionRef(category) ?: return
        viewModelScope.launch {
            try {
                ref.document(id).delete().await()
                onSelectCategory(category)
                loadCounts()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
