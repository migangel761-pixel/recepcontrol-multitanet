package com.aistudio.recepcontrol.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileRow(
    val id: String,
    @SerialName("full_name") val fullName: String,
    val email: String,
    val phone: String? = null,
    val role: String,
    val apartment: String? = null,
    @SerialName("complex_id") val complexId: String? = null,
    @SerialName("unit_id") val unitId: String? = null
)

@Serializable
data class ReceiptRow(
    val id: String,
    @SerialName("service_category") val serviceCategory: String,
    val apartment: String,
    val status: String,
    @SerialName("reference_code") val referenceCode: String,
    @SerialName("received_by") val receivedBy: String,
    @SerialName("received_at") val receivedAt: String,
    @SerialName("delivered_at") val deliveredAt: String? = null,
    @SerialName("guard_note") val guardNote: String,
    @SerialName("photo_path") val photoPath: String? = null,
    @SerialName("complex_id") val complexId: String? = null,
    @SerialName("unit_id") val unitId: String? = null,
    @SerialName("resident_id") val residentId: String? = null
)

@Serializable
data class ReceiptInsert(
    @SerialName("service_category") val serviceCategory: String,
    val apartment: String,
    @SerialName("reference_code") val referenceCode: String,
    @SerialName("received_by") val receivedBy: String,
    @SerialName("guard_note") val guardNote: String,
    @SerialName("photo_path") val photoPath: String? = null,
    @SerialName("complex_id") val complexId: String? = null,
    @SerialName("unit_id") val unitId: String? = null,
    @SerialName("resident_id") val residentId: String? = null
)

@Serializable
data class AnnouncementRow(
    val id: String,
    val title: String,
    val content: String,
    @SerialName("author_id") val authorId: String,
    @SerialName("created_at") val createdAt: String
)

@Serializable
data class AnnouncementInsert(
    val title: String,
    val content: String,
    @SerialName("author_id") val authorId: String
)

@Serializable
data class NotificationRow(
    val id: String,
    val title: String,
    val message: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("receipt_id") val receiptId: String? = null
)
