package com.lifevault.app.presentation.qrcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class QrCardUiState(
    val qrBitmap: android.graphics.Bitmap? = null,
    val summaryText: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

/**
 * Encodes the user's emergency-relevant info directly into the QR code as
 * plain text (name, blood group, allergies, emergency contact). Scanning it
 * with any generic QR reader shows this text — no app, login, or internet
 * needed to read it. This is real, working QR generation (ZXing), not a
 * placeholder image.
 */
@HiltViewModel
class QrCardViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(QrCardUiState())
    val uiState: StateFlow<QrCardUiState> = _uiState

    init { load() }

    private fun load() {
        val uid = firebaseAuth.currentUser?.uid ?: run {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        viewModelScope.launch {
            try {
                val userDoc = firestore.collection("users").document(uid).get().await()
                val name = userDoc.getString("fullName") ?: "Unknown"
                val phone = userDoc.getString("phoneNumber") ?: ""

                val profileDoc = firestore.collection("users").document(uid)
                    .collection("emergencyProfile").document("profile").get().await()

                val bloodGroup = profileDoc.getString("bloodGroup") ?: "Not set"
                val allergies = profileDoc.getString("allergies") ?: "None listed"
                val contactName = profileDoc.getString("emergencyContactName") ?: ""
                val contactPhone = profileDoc.getString("emergencyContactPhone") ?: ""

                val summary = buildString {
                    append("LIFEVAULT EMERGENCY CARD\n")
                    append("Name: $name\n")
                    append("Phone: $phone\n")
                    append("Blood Group: $bloodGroup\n")
                    append("Allergies: $allergies\n")
                    if (contactName.isNotBlank()) {
                        append("Emergency Contact: $contactName ($contactPhone)")
                    }
                }

                val bitmap = generateQrBitmap(summary)
                _uiState.update { it.copy(qrBitmap = bitmap, summaryText = summary, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun generateQrBitmap(text: String): android.graphics.Bitmap {
        val size = 512
        val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }
}
