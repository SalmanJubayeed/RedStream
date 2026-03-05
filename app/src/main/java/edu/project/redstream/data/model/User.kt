package edu.project.redstream.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

enum class UserRole { DONOR, RECIPIENT, ADMIN }

data class User(
    @DocumentId val uid: String = "",
    val role: String = "donor",
    val name: String = "",
    val email: String = "",
    val phone: String? = null,
    val bloodGroup: String = "",
    val locationText: String = "",
    val verified: Boolean = false,
    val lastDonationAt: Timestamp? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toRole(): UserRole = when (role.lowercase()) {
        "recipient" -> UserRole.RECIPIENT
        "admin"     -> UserRole.ADMIN
        else        -> UserRole.DONOR
    }
}

val BLOOD_GROUPS = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")