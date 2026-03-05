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
    val expiresAt: Timestamp? = null,
    val moderation: Map<String, Any>? = null
)