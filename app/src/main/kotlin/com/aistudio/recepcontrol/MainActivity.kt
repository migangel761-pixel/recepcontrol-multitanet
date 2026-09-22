package com.aistudio.recepcontrol

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.aistudio.recepcontrol.model.UserRole
import com.aistudio.recepcontrol.ui.screens.*
import com.aistudio.recepcontrol.ui.theme.RecepControlTheme
import com.aistudio.recepcontrol.viewmodel.RecepViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RecepViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecepControlTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val currentUser by viewModel.currentUser.collectAsState()
                    val receipts by viewModel.receipts.collectAsState()
                    val announcements by viewModel.announcements.collectAsState()
                    val notifications by viewModel.notifications.collectAsState()
                    val activeAlert by viewModel.activeAlert.collectAsState()
                    val isLoading by viewModel.isLoading.collectAsState()
                    val errorMessage by viewModel.errorMessage.collectAsState()
                    val successMessage by viewModel.successMessage.collectAsState()
                    var currentScreen by remember { mutableStateOf("main") }

                    when {
                        currentUser == null -> {
                            LoginScreen(
                                onLoginVigilante = { email, password, phone ->
                                    viewModel.loginAsVigilante(email = email, phone = phone, password = password)
                                    currentScreen = "main"
                                },
                                onLoginResident = { email, password, phone ->
                                    viewModel.loginAsResident(email = email, phone = phone, password = password)
                                    currentScreen = "main"
                                },
                                onRegisterResident = { email, password, fullName, phone, apartment ->
                                    viewModel.registerResident(email, password, fullName, phone, apartment)
                                },
                                isLoading = isLoading,
                                errorMessage = errorMessage,
                                successMessage = successMessage,
                                onDismissError = viewModel::clearError
                            )
                        }
                        currentUser!!.role == UserRole.VIGILANTE || currentUser!!.role == UserRole.ADMINISTRADOR -> {
                            if (currentScreen == "add_receipt") {
                                AddReceiptScreen(
                                    onBack = { currentScreen = "main" },
                                    onConfirmRegistration = { service, apt, note ->
                                        viewModel.registerReceipt(service, apt, note)
                                        currentScreen = "main"
                                    }
                                )
                            } else {
                                VigilanteScreen(
                                    user = currentUser!!,
                                    receipts = receipts,
                                    announcements = announcements,
                                    onNavigateToAddReceipt = { currentScreen = "add_receipt" },
                                    onMarkAsDelivered = { receiptId -> viewModel.markAsDelivered(receiptId) },
                                    onPostAnnouncement = { title, content -> viewModel.postAnnouncement(title, content) },
                                    onUpdateProfile = { name, email, phone -> viewModel.updateProfile(name, email, phone) },
                                    onLogout = { viewModel.logout() }
                                )
                            }
                        }
                        currentUser!!.role == UserRole.RESIDENT -> {
                            ResidentScreen(
                                user = currentUser!!,
                                receipts = receipts,
                                announcements = announcements,
                                notifications = notifications,
                                activeAlert = activeAlert,
                                onDismissAlert = { viewModel.dismissAlert() },
                                onUpdateProfile = { name, email, phone, apt -> viewModel.updateProfile(name, email, phone, apt) },
                                onLogout = { viewModel.logout() }
                            )
                        }
                    }
                }
            }
        }
    }
}
