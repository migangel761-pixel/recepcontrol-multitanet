package com.aistudio.recepcontrol.model

enum class UserRole {
    ADMINISTRADOR, VIGILANTE, RESIDENT
}

data class User(
    val id: String,
    val email: String,
    val name: String,
    val role: UserRole,
    val apartment: String? = null,
    val phone: String? = null,
    val complexId: String? = null,
    val unitId: String? = null,
    val shift: String? = null
)

enum class PhysicalReceiptStatus {
    EN_PORTERIA,
    ENTREGADO
}

data class PhysicalReceipt(
    val id: String,
    val serviceCategory: String, // ej. "Energía Eléctrica (Enel)", "Acueducto y Alcantarillado (EAAB)", "Gas Natural (Vanti)", "Telecomunicaciones (Claro)", "Cuota de Administración"
    val apartment: String,
    val status: PhysicalReceiptStatus = PhysicalReceiptStatus.EN_PORTERIA,
    val referenceCode: String = "REC-${(100000..999999).random()}",
    val receivedByGuard: String = "Carlos Mendoza",
    val receivedTimestamp: String = "2026-09-07 14:30",
    val deliveredTimestamp: String? = null,
    val guardNote: String = "Recibo físico clasificado en casillero de portería.",
    val hasPhoto: Boolean = true
)

data class Announcement(
    val id: String,
    val title: String,
    val content: String,
    val date: String,
    val author: String
)

data class ResidentNotification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val apartment: String,
    val receiptId: String
)
