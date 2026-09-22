package com.aistudio.recepcontrol.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant

class SupabaseRepository(
    private val client: SupabaseClient = SupabaseClientProvider.client
) {
    suspend fun signIn(email: String, password: String): ProfileRow {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        val userId = client.auth.currentUserOrNull()?.id
            ?: error("Supabase no devolvió un usuario autenticado")
        return getProfile(userId)
    }

    suspend fun signUpResident(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        apartment: String
    ) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = buildJsonObject {
                put("full_name", fullName)
                put("phone", phone)
                put("apartment", apartment)
            }
        }
    }

    suspend fun signOut() {
        client.auth.signOut()
    }

    fun currentUserId(): String =
        client.auth.currentUserOrNull()?.id
            ?: error("Supabase no devolvió un usuario autenticado")

    suspend fun getProfile(userId: String): ProfileRow =
        client.from("profiles").select {
            filter { eq("id", userId) }
        }.decodeSingle()

    suspend fun loadReceipts(): List<ReceiptRow> =
        client.from("receipts").select().decodeList()

    suspend fun loadAnnouncements(): List<AnnouncementRow> =
        client.from("announcements").select().decodeList()

    suspend fun loadNotifications(): List<NotificationRow> =
        client.from("notifications").select().decodeList()

    suspend fun createReceipt(
        serviceCategory: String,
        apartment: String,
        guardNote: String,
        receivedBy: String
    ) {
        val guardProfile = getProfile(receivedBy)
        val unit = guardProfile.complexId?.let { complexId ->
            client.from("units").select {
                filter { eq("complex_id", complexId); eq("unit_number", apartment) }
            }.decodeList<UnitRow>().firstOrNull()
        }
        val residentId = unit?.id?.let { unitId ->
            client.from("profiles").select {
                filter { eq("unit_id", unitId); eq("role", "RESIDENT") }
            }.decodeList<ProfileRow>().firstOrNull()?.id
        }
        client.from("receipts").insert(
            ReceiptInsert(
                serviceCategory = serviceCategory,
                apartment = apartment,
                referenceCode = "REC-${(100000..999999).random()}",
                receivedBy = receivedBy,
                guardNote = guardNote.ifBlank { "Registrado en bitácora de portería." },
                complexId = guardProfile.complexId,
                unitId = unit?.id,
                residentId = residentId
            )
        )
    }

    suspend fun markAsDelivered(receiptId: String) {
        client.from("receipts").update({
            set("status", "ENTREGADO")
            set("delivered_at", Instant.now().toString())
        }) {
            filter { eq("id", receiptId) }
        }
    }

    suspend fun createAnnouncement(title: String, content: String, authorId: String) {
        client.from("announcements").insert(
            AnnouncementInsert(title = title, content = content, authorId = authorId)
        )
    }

    suspend fun updateProfile(
        userId: String,
        name: String,
        email: String,
        phone: String,
        apartment: String?
    ) {
        client.from("profiles").update({
            set("full_name", name)
            set("email", email)
            set("phone", phone)
        }) {
            filter { eq("id", userId) }
        }
    }
}

@kotlinx.serialization.Serializable
private data class UnitRow(val id: String)
