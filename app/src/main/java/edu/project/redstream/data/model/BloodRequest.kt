package edu.project.redstream.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class BloodRequest(
    @DocumentId val id: String = "",
    val recipientId: String = "",
    val bloodGroupNeeded: String = "",
    val unitsNeeded: Int = 1,
    val urgency: String = "Medium",
    val hospitalName: String = "",
    val contactPhone: String = "",
    val notes: String = "",
    val status: String = "OPEN",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val expiresAt: Timestamp? = null,       // auto 24h expiry
    val neededByHours: Int = 24,            // ← NEW: recipient sets deadline in hours
    val donationDeadlineAt: Timestamp? = null, // ← NEW: set when application approved
    val moderation: Map<String, Any>? = null
)