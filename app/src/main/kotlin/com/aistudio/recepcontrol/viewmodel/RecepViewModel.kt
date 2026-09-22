package com.aistudio.recepcontrol.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aistudio.recepcontrol.data.AnnouncementRow
import com.aistudio.recepcontrol.data.NotificationRow
import com.aistudio.recepcontrol.data.ProfileRow
import com.aistudio.recepcontrol.data.ReceiptRow
import com.aistudio.recepcontrol.data.SupabaseRepository
import com.aistudio.recepcontrol.model.Announcement
import com.aistudio.recepcontrol.model.PhysicalReceipt
import com.aistudio.recepcontrol.model.PhysicalReceiptStatus
import com.aistudio.recepcontrol.model.ResidentNotification
import com.aistudio.recepcontrol.model.User
import com.aistudio.recepcontrol.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RecepViewModel(
    private val repository: SupabaseRepository = SupabaseRepository()
) : ViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    private val _notifications = MutableStateFlow<List<ResidentNotification>>(emptyList())
    val notifications: StateFlow<List<ResidentNotification>> = _notifications.asStateFlow()
    private val _activeAlert = MutableStateFlow<ResidentNotification?>(null)
    val activeAlert: StateFlow<ResidentNotification?> = _activeAlert.asStateFlow()
    private val _receipts = MutableStateFlow<List<PhysicalReceipt>>(emptyList())
    val receipts: StateFlow<List<PhysicalReceipt>> = _receipts.asStateFlow()
    private val _announcements = MutableStateFlow<List<Announcement>>(emptyList())
    val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()
    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    fun dismissAlert() { _activeAlert.value = null }
    fun clearError() { _errorMessage.value = null }
    fun clearSuccess() { _successMessage.value = null }

    fun loginAsVigilante(email: String, phone: String, password: String) {
        authenticate(email, password, UserRole.VIGILANTE)
    }

    fun loginAsResident(email: String, phone: String, password: String) {
        authenticate(email, password, UserRole.RESIDENT)
    }

    fun registerResident(
        email: String,
        password: String,
        fullName: String,
        phone: String,
        apartment: String
    ) {
        if (email.isBlank() || password.length < 6 || fullName.isBlank() || apartment.isBlank()) {
            _errorMessage.value = "Completa nombre, apartamento, correo y una contraseña de mínimo 6 caracteres."
            return
        }
        execute {
            repository.signUpResident(
                email.trim(), password, fullName.trim(), phone.trim(), apartment.trim()
            )
            _successMessage.value = "Cuenta creada. Revisa tu correo para confirmar la cuenta y luego inicia sesión."
        }
    }

    private fun authenticate(email: String, password: String, expectedRole: UserRole) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Escribe tu correo y contraseña."
            return
        }
        execute {
            val profile = repository.signIn(email.trim(), password)
            check(profile.toUser().role == expectedRole) {
                "Este usuario no tiene el rol seleccionado."
            }
            _currentUser.value = profile.toUser()
            refreshData()
        }
    }

    fun updateProfile(name: String, email: String, phone: String, apartment: String? = null) {
        val current = _currentUser.value ?: return
        execute {
            repository.updateProfile(current.id, name, email, phone, apartment)
            _currentUser.value = repository.getProfile(current.id).toUser()
        }
    }

    fun logout() {
        execute {
            repository.signOut()
            _currentUser.value = null
            _receipts.value = emptyList()
            _announcements.value = emptyList()
            _notifications.value = emptyList()
            _activeAlert.value = null
        }
    }

    fun registerReceipt(serviceCategory: String, apartment: String, guardNote: String) {
        val guardId = _currentUser.value?.id ?: return
        execute {
            repository.createReceipt(serviceCategory, apartment, guardNote, guardId)
            refreshData()
        }
    }

    fun markAsDelivered(receiptId: String) {
        execute {
            repository.markAsDelivered(receiptId)
            refreshData()
        }
    }

    fun postAnnouncement(title: String, content: String) {
        val authorId = _currentUser.value?.id ?: return
        execute {
            repository.createAnnouncement(title, content, authorId)
            refreshData()
        }
    }

    private suspend fun refreshData() {
        _receipts.value = repository.loadReceipts().map(ReceiptRow::toDomain)
        _announcements.value = repository.loadAnnouncements().map(AnnouncementRow::toDomain)
        _notifications.value = repository.loadNotifications().map(NotificationRow::toDomain)
    }

    private fun execute(block: suspend () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _successMessage.value = null
            try { block() }
            catch (error: Exception) { _errorMessage.value = error.message ?: "No fue posible completar la operación." }
            finally { _isLoading.value = false }
        }
    }
}

private fun ProfileRow.toUser(): User = User(
    id = id,
    email = email,
    name = fullName.ifBlank { email.substringBefore("@") },
    role = when (role) {
        "ADMINISTRADOR" -> UserRole.ADMINISTRADOR
        "VIGILANTE" -> UserRole.VIGILANTE
        else -> UserRole.RESIDENT
    },
    apartment = apartment,
    phone = phone,
    complexId = complexId,
    unitId = unitId
)

private fun ReceiptRow.toDomain(): PhysicalReceipt = PhysicalReceipt(
    id = id,
    serviceCategory = serviceCategory,
    apartment = apartment,
    status = if (status == "ENTREGADO") PhysicalReceiptStatus.ENTREGADO else PhysicalReceiptStatus.EN_PORTERIA,
    referenceCode = referenceCode,
    receivedByGuard = receivedBy,
    receivedTimestamp = receivedAt,
    deliveredTimestamp = deliveredAt,
    guardNote = guardNote,
    hasPhoto = !photoPath.isNullOrBlank()
)

private fun AnnouncementRow.toDomain(): Announcement = Announcement(
    id = id,
    title = title,
    content = content,
    date = createdAt,
    author = "Portería"
)

private fun NotificationRow.toDomain(): ResidentNotification = ResidentNotification(
    id = id,
    title = title,
    message = message,
    timestamp = createdAt,
    apartment = "",
    receiptId = receiptId.orEmpty()
)
