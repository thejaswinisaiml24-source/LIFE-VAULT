package com.lifevault.app.presentation.documents

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class DocumentRecord(
    val id: String,
    val title: String,
    val category: String,
    val notes: String,
    val fileUrl: String? = null,
    val createdAtEpochMillis: Long
)

data class DocumentsUiState(
    val documents: List<DocumentRecord> = emptyList(),
    val isLoading: Boolean = true,
    val showAddDialog: Boolean = false,
    val newTitle: String = "",
    val newCategory: String = "",
    val newNotes: String = "",
    val newExpiryDate: String = "",
    val pickedFileUri: Uri? = null,
    val pickedFileName: String? = null,
    val isSaving: Boolean = false,
    val uploadProgress: Float? = null,
    val error: String? = null
)

private const val USERS_COLLECTION = "users"
private const val SUBCOLLECTION = "documents"

/**
 * Real file upload: picked file bytes go to Firebase Storage under
 * users/{uid}/documents/{autoId}, and the resulting download URL is saved
 * alongside the metadata in Firestore. Encryption-at-rest for the file
 * itself is NOT implemented here — Storage's transport is HTTPS, but the
 * file isn't additionally client-side encrypted, unlike what "end-to-end
 * encrypted" in the mockup implies. Flagging that honestly rather than
 * faking a lock icon with no real encryption behind it.
 */
@HiltViewModel
class DocumentsViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(DocumentsUiState())
    val uiState: StateFlow<DocumentsUiState> = _uiState

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
                val docs = snapshot.documents.mapNotNull { doc ->
                    val title = doc.getString("title") ?: return@mapNotNull null
                    DocumentRecord(
                        id = doc.id,
                        title = title,
                        category = doc.getString("category") ?: "",
                        notes = doc.getString("notes") ?: "",
                        fileUrl = doc.getString("fileUrl"),
                        createdAtEpochMillis = doc.getLong("createdAtEpochMillis") ?: 0L
                    )
                }
                _uiState.update { it.copy(documents = docs, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onOpenAddDialog() = _uiState.update {
        it.copy(showAddDialog = true, newTitle = "", newCategory = "", newNotes = "", newExpiryDate = "", pickedFileUri = null, pickedFileName = null)
    }
    fun onDismissAddDialog() = _uiState.update { it.copy(showAddDialog = false) }
    fun onNewTitleChange(v: String) = _uiState.update { it.copy(newTitle = v) }
    fun onNewCategoryChange(v: String) = _uiState.update { it.copy(newCategory = v) }
    fun onNewNotesChange(v: String) = _uiState.update { it.copy(newNotes = v) }
    fun onNewExpiryDateChange(v: String) = _uiState.update { it.copy(newExpiryDate = v) }

    fun onFilePicked(uri: Uri, contentResolver: ContentResolver) {
        val name = contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) cursor.getString(nameIndex) else null
        } ?: uri.lastPathSegment
        _uiState.update { it.copy(pickedFileUri = uri, pickedFileName = name) }
    }

    fun addDocument() {
        val ref = collectionRef() ?: return
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
                    _uiState.update { it.copy(uploadProgress = 0f) }
                    val storageRef = firebaseStorage.reference
                        .child("users/$uid/documents/${System.currentTimeMillis()}_${state.pickedFileName ?: "file"}")
                    storageRef.putFile(pickedUri).await()
                    fileUrl = storageRef.downloadUrl.await().toString()
                    _uiState.update { it.copy(uploadProgress = null) }
                }

                val data = hashMapOf(
                    "title" to state.newTitle.trim(),
                    "category" to state.newCategory.trim(),
                    "notes" to state.newNotes.trim(),
                    "expiryDate" to state.newExpiryDate.trim(),
                    "createdAtEpochMillis" to System.currentTimeMillis()
                )
                if (fileUrl != null) data["fileUrl"] = fileUrl

                ref.add(data).await()

                firestore.collection(USERS_COLLECTION).document(uid).collection("activities")
                    .add(
                        hashMapOf(
                            "description" to "Document added: ${state.newTitle.trim()}",
                            "timestamp" to System.currentTimeMillis()
                        )
                    ).await()

                _uiState.update { it.copy(isSaving = false, showAddDialog = false, uploadProgress = null) }
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, uploadProgress = null, error = e.message ?: "Failed to save") }
            }
        }
    }

    fun deleteDocument(id: String) {
        val ref = collectionRef() ?: return
        viewModelScope.launch {
            try {
                ref.document(id).delete().await()
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Failed to delete") }
            }
        }
    }
}
