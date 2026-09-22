package com.aistudio.recepcontrol.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aistudio.recepcontrol.R
import com.aistudio.recepcontrol.model.Announcement
import com.aistudio.recepcontrol.model.PhysicalReceipt
import com.aistudio.recepcontrol.model.PhysicalReceiptStatus
import com.aistudio.recepcontrol.model.User
import com.aistudio.recepcontrol.ui.components.*
import com.aistudio.recepcontrol.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VigilanteScreen(
    user: User,
    receipts: List<PhysicalReceipt>,
    announcements: List<Announcement>,
    onNavigateToAddReceipt: () -> Unit,
    onMarkAsDelivered: (String) -> Unit,
    onPostAnnouncement: (String, String) -> Unit,
    onUpdateProfile: (name: String, email: String, phone: String) -> Unit = { _, _, _ -> },
    onLogout: () -> Unit
) {
    // Tabs: 0 -> Principal, 1 -> Notificaciones, 2 -> Perfil / Configuración
    var selectedNavTab by remember { mutableIntStateOf(0) }
    var statusFilter by remember { mutableStateOf<PhysicalReceiptStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedReceiptForDetail by remember { mutableStateOf<PhysicalReceipt?>(null) }
    var showPostDialog by remember { mutableStateOf(false) }
    var annTitle by remember { mutableStateOf("") }
    var annContent by remember { mutableStateOf("") }

    // Profile Edit State
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(user.name) }
    var editEmail by remember { mutableStateOf(user.email) }
    var editPhone by remember { mutableStateOf(user.phone ?: "") }

    // Settings Toggles
    var pushAlertsEnabled by remember { mutableStateOf(true) }
    var soundAlertsEnabled by remember { mutableStateOf(true) }

    val filteredReceipts = receipts.filter { receipt ->
        (statusFilter == null || receipt.status == statusFilter) &&
                (searchQuery.isBlank() ||
                        receipt.apartment.contains(searchQuery, ignoreCase = true) ||
                        receipt.serviceCategory.contains(searchQuery, ignoreCase = true) ||
                        receipt.referenceCode.contains(searchQuery, ignoreCase = true))
    }

    val inPorteriaCount = receipts.count { it.status == PhysicalReceiptStatus.EN_PORTERIA }
    val deliveredCount = receipts.count { it.status == PhysicalReceiptStatus.ENTREGADO }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Sky600,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Security,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier
                                    .padding(9.dp)
                                    .fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "Puesto de Portería",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite
                            )
                            Text(
                                "Guarda: ${user.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Sky200
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { selectedNavTab = 2 }) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración", tint = PureWhite)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Cerrar sesión", tint = Color(0xFFFDA4AF))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Slate900,
                    titleContentColor = PureWhite,
                    actionIconContentColor = PureWhite
                )
            )
        },
        floatingActionButton = {
            if (selectedNavTab == 0) {
                ExtendedFloatingActionButton(
                    onClick = onNavigateToAddReceipt,
                    containerColor = Sky600,
                    contentColor = PureWhite,
                    shape = RoundedButtonShape,
                    icon = { Icon(Icons.Default.CameraAlt, contentDescription = null) },
                    text = { Text("Registrar Evento", style = MaterialTheme.typography.labelLarge) }
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = PureWhite,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedNavTab == 0,
                    onClick = { selectedNavTab = 0 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (inPorteriaCount > 0) {
                                    Badge(containerColor = Sky600) { Text("$inPorteriaCount") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Inbox, contentDescription = "Principal")
                        }
                    },
                    label = { Text("Principal") }
                )

                NavigationBarItem(
                    selected = selectedNavTab == 1,
                    onClick = { selectedNavTab = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = Emerald600) { Text("${announcements.size}") }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                        }
                    },
                    label = { Text("Notificaciones") }
                )

                NavigationBarItem(
                    selected = selectedNavTab == 2,
                    onClick = { selectedNavTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil / Config") }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        when (selectedNavTab) {
            0 -> {
                // TAB 0: PRINCIPAL
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))

                        // High-contrast Rounded Metric Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DashboardMetricCard(
                                title = "En Portería",
                                count = inPorteriaCount,
                                subtitle = "Por reclamar",
                                icon = Icons.Default.Inbox,
                                accentColor = Amber600,
                                containerColor = Amber100,
                                textColor = Amber900,
                                modifier = Modifier.weight(1f)
                            )

                            DashboardMetricCard(
                                title = "Entregados",
                                count = deliveredCount,
                                subtitle = "En puerta",
                                icon = Icons.Default.CheckCircle,
                                accentColor = Emerald600,
                                containerColor = Emerald100,
                                textColor = Emerald900,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Clean Search Field
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar por apartamento, recibo o evento...") },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = Slate500)
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Limpiar")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(RecepCornerRadius.Chip),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PureWhite,
                                unfocusedContainerColor = PureWhite,
                                focusedBorderColor = Slate950,
                                unfocusedBorderColor = Slate400
                            )
                        )
                    }

                    // Modern High-Contrast Filter Group
                    item {
                        DashboardFilterGroup(
                            options = listOf(
                                "Todos (${receipts.size})" to null,
                                "En Portería ($inPorteriaCount)" to PhysicalReceiptStatus.EN_PORTERIA,
                                "Entregados ($deliveredCount)" to PhysicalReceiptStatus.ENTREGADO
                            ),
                            selected = statusFilter,
                            onSelected = { statusFilter = it }
                        )
                    }

                    if (filteredReceipts.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp),
                                shape = RoundedCardShape,
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, Slate200)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.Inbox,
                                        contentDescription = null,
                                        tint = Slate400,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        "No hay eventos ni correspondencia que coincida",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Slate700
                                    )
                                }
                            }
                        }
                    } else {
                        items(filteredReceipts, key = { it.id }) { itemReceipt ->
                            DashboardEventCard(
                                receipt = itemReceipt,
                                onInspectPhoto = { selectedReceiptForDetail = itemReceipt },
                                onMarkDelivered = { onMarkAsDelivered(itemReceipt.id) }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }

            1 -> {
                // TAB 1: NOTIFICACIONES
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Card(
                            shape = RoundedCardShape,
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Avisos y Novedades",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Slate950
                                    )
                                    Text(
                                        "Comunicados sobre ascensores, piscina y portería",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Slate500
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = { showPostDialog = true },
                                    shape = RoundedButtonShape,
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald600)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Publicar", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    items(announcements, key = { it.id }) { ann ->
                        Card(
                            shape = RoundedCardShape,
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = BorderStroke(1.dp, Slate200),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(18.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        ann.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Slate950
                                    )
                                    Surface(
                                        color = Sky100,
                                        shape = RoundedPillShape
                                    ) {
                                        Text(
                                            ann.date,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Sky700,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    ann.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Slate700
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    "Por: ${ann.author}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Slate500
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }

            2 -> {
                // TAB 2: PERFIL Y CONFIGURACIÓN
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))

                        // Perfil del Vigilante Card
                        Card(
                            shape = RoundedLargeCardShape,
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = BorderStroke(1.dp, Slate200),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    modifier = Modifier.size(80.dp),
                                    shape = CircleShape,
                                    color = Sky100,
                                    border = BorderStroke(2.dp, Sky600)
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Avatar Guarda",
                                        tint = Sky700,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = user.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Slate950
                                )

                                Surface(
                                    color = Emerald100,
                                    shape = RoundedPillShape,
                                    modifier = Modifier.padding(top = 6.dp)
                                ) {
                                    Text(
                                        text = "Vigilante • Seguridad y Control de Acceso",
                                        color = Emerald900,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                HorizontalDivider(color = Slate200)
                                Spacer(modifier = Modifier.height(16.dp))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Email, contentDescription = null, tint = Slate500, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Correo: ${user.email}", style = MaterialTheme.typography.bodyMedium, color = Slate700)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Phone, contentDescription = null, tint = Slate500, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text("Celular: ${user.phone ?: "No registrado"}", style = MaterialTheme.typography.bodyMedium, color = Slate700)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedButton(
                                    onClick = {
                                        editName = user.name
                                        editEmail = user.email
                                        editPhone = user.phone ?: ""
                                        showEditProfileDialog = true
                                    },
                                    shape = RoundedButtonShape,
                                    border = BorderStroke(1.dp, Slate300),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Editar Datos del Guarda", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }

                    // Configuration Card
                    item {
                        Card(
                            shape = RoundedLargeCardShape,
                            colors = CardDefaults.cardColors(containerColor = PureWhite),
                            border = BorderStroke(1.dp, Slate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Ajustes de la Aplicación",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Slate950
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Notificaciones Push Inmediatas", style = MaterialTheme.typography.titleSmall, color = Slate900)
                                        Text("Avisar a los residentes al registrar evento", style = MaterialTheme.typography.bodySmall, color = Slate500)
                                    }
                                    Switch(
                                        checked = pushAlertsEnabled,
                                        onCheckedChange = { pushAlertsEnabled = it }
                                    )
                                }

                                HorizontalDivider(color = Slate200)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Sonidos de Confirmación", style = MaterialTheme.typography.titleSmall, color = Slate900)
                                        Text("Emitir tono al digitalizar recibo o entrega", style = MaterialTheme.typography.bodySmall, color = Slate500)
                                    }
                                    Switch(
                                        checked = soundAlertsEnabled,
                                        onCheckedChange = { soundAlertsEnabled = it }
                                    )
                                }

                                HorizontalDivider(color = Slate200)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Diseño Visual", style = MaterialTheme.typography.titleSmall, color = Slate900)
                                        Text("Esquema de alto contraste claro", style = MaterialTheme.typography.bodySmall, color = Slate500)
                                    }
                                    Surface(
                                        color = Emerald100,
                                        shape = RoundedPillShape
                                    ) {
                                        Text("Modo Light", color = Emerald900, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Logout Button
                    item {
                        Button(
                            onClick = onLogout,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Rose100,
                                contentColor = Rose600
                            ),
                            shape = RoundedButtonShape,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Icon(Icons.Default.ExitToApp, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Cerrar Sesión del Puesto de Portería", style = MaterialTheme.typography.labelLarge)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }

    // Modal para Inspección de Foto HD de Recibo o Evento
    if (selectedReceiptForDetail != null) {
        Dialog(onDismissRequest = { selectedReceiptForDetail = null }) {
            Card(
                shape = RoundedLargeCardShape,
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                border = BorderStroke(1.dp, Slate200),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedReceiptForDetail!!.serviceCategory,
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate950
                        )
                        IconButton(onClick = { selectedReceiptForDetail = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Slate200, RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_receipt_sample),
                            contentDescription = "Foto en HD",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Destino: ${selectedReceiptForDetail!!.apartment}", style = MaterialTheme.typography.titleSmall, color = Slate900)
                    Text("Guarda receptor: ${selectedReceiptForDetail!!.receivedByGuard}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    Text("Fecha/Hora: ${selectedReceiptForDetail!!.receivedTimestamp}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    Text("Nota: ${selectedReceiptForDetail!!.guardNote}", style = MaterialTheme.typography.bodyMedium, color = Slate700, modifier = Modifier.padding(top = 4.dp))

                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedReceiptForDetail!!.status == PhysicalReceiptStatus.EN_PORTERIA) {
                        Button(
                            onClick = {
                                onMarkAsDelivered(selectedReceiptForDetail!!.id)
                                selectedReceiptForDetail = null
                            },
                            shape = RoundedButtonShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald600),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Entregar en Puerta al Residente", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    }

    // Modal Editar Perfil
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Editar Datos del Guarda", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Nombre Completo") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Correo") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Celular") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(editName, editEmail, editPhone)
                        showEditProfileDialog = false
                    },
                    shape = RoundedButtonShape
                ) {
                    Text("Guardar", style = MaterialTheme.typography.labelMedium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Dialog Publicar Anuncio
    if (showPostDialog) {
        AlertDialog(
            onDismissRequest = { showPostDialog = false },
            title = { Text("Publicar Novedad o Notificación", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = annTitle,
                        onValueChange = { annTitle = it },
                        label = { Text("Título (ej. Mantenimiento de Ascensores)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = annContent,
                        onValueChange = { annContent = it },
                        label = { Text("Detalle de la novedad") },
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (annTitle.isNotBlank()) {
                            onPostAnnouncement(annTitle, annContent)
                            annTitle = ""
                            annContent = ""
                            showPostDialog = false
                        }
                    },
                    shape = RoundedButtonShape
                ) {
                    Text("Publicar", style = MaterialTheme.typography.labelMedium)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPostDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
