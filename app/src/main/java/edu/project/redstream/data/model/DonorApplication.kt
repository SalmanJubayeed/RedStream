package edu.project.redstream.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class DonorApplication(
    @DocumentId val id: String = "",
    val donorId: String = "",
    val status: String = "PENDING",
    val message: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val donationDeadlineAt: Timestamp? = null

)