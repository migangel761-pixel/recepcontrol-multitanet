package com.aistudio.recepcontrol.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import com.aistudio.recepcontrol.model.ResidentNotification
import com.aistudio.recepcontrol.model.User
import com.aistudio.recepcontrol.ui.components.*
import com.aistudio.recepcontrol.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResidentScreen(
    user: User,
    receipts: List<PhysicalReceipt>,
    announcements: List<Announcement>,
    notifications: List<ResidentNotification>,
    activeAlert: ResidentNotification?,
    onDismissAlert: () -> Unit,
    onUpdateProfile: (name: String, email: String, phone: String, apartment: String) -> Unit = { _, _, _, _ -> },
    onLogout: () -> Unit
) {
    // Tabs: 0 -> Principal, 1 -> Notificaciones, 2 -> Perfil / Configuración
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedFilter by remember { mutableStateOf<PhysicalReceiptStatus?>(null) }
    var selectedReceiptForDetail by remember { mutableStateOf<PhysicalReceipt?>(null) }

    // Profile Edit State
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(user.name) }
    var editEmail by remember { mutableStateOf(user.email) }
    var editPhone by remember { mutableStateOf(user.phone ?: "") }
    var editApartment by remember { mutableStateOf(user.apartment ?: "Torre 1 - 302") }

    // Settings Toggles
    var pushAlertsEnabled by remember { mutableStateOf(true) }
    var soundAlertsEnabled by remember { mutableStateOf(true) }
    var smsAlertsEnabled by remember { mutableStateOf(false) }

    val residentBills = receipts.filter {
        it.apartment.equals(user.apartment ?: "Torre 1 - 302", ignoreCase = true) ||
                it.apartment.contains("Zonas Comunes", ignoreCase = true) ||
                it.apartment.contains("Edificio", ignoreCase = true)
    }

    val inPorteriaCount = residentBills.count { it.status == PhysicalReceiptStatus.EN_PORTERIA }
    val deliveredCount = residentBills.count { it.status == PhysicalReceiptStatus.ENTREGADO }

    val displayedReceipts = residentBills.filter {
        selectedFilter == null || it.status == selectedFilter
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Emerald600,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Default.Home,
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
                                user.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = PureWhite
                            )
                            Text(
                                user.apartment ?: "Torre 1 - 302",
                                style = MaterialTheme.typography.bodySmall,
                                color = Emerald100
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { selectedTab = 2 }) {
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
        bottomBar = {
            NavigationBar(
                containerColor = PureWhite,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (inPorteriaCount > 0) {
                                    Badge(containerColor = Emerald600) { Text("$inPorteriaCount") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Inbox, contentDescription = "Principal")
                        }
                    },
                    label = { Text("Principal") }
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (notifications.isNotEmpty()) {
                                    Badge(containerColor = Sky600) { Text("${notifications.size}") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notificaciones")
                        }
                    },
                    label = { Text("Notificaciones") }
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil / Config") }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Instant Alert Banner when Guard registers a receipt
            AnimatedVisibility(
                visible = activeAlert != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                if (activeAlert != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = RoundedCardShape,
                        colors = CardDefaults.cardColors(containerColor = Emerald100),
                        border = BorderStroke(1.5.dp, Emerald500)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = Emerald900,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    activeAlert.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Emerald900
                                )
                                Text(
                                    activeAlert.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Emerald800
                                )
                            }
                            IconButton(onClick = onDismissAlert) {
                                Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Emerald900)
                            }
                        }
                    }
                }
            }

            when (selectedTab) {
                0 -> {
                    // TAB 0: PRINCIPAL
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))

                            // Hero Section Header
                            DashboardSectionHeader(
                                title = "Correspondencia y Eventos",
                                subtitle = "Recibos físicos en portería para tu inmueble",
                                badgeText = user.apartment ?: "Torre 1 - 302",
                                badgeColor = Sky100,
                                badgeTextColor = Sky700
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // High-Contrast Rounded Metric Cards
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
                                    subtitle = "Reclamados",
                                    icon = Icons.Default.CheckCircle,
                                    accentColor = Emerald600,
                                    containerColor = Emerald100,
                                    textColor = Emerald900,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Filter Group
                        item {
                            DashboardFilterGroup(
                                options = listOf(
                                    "Todos (${residentBills.size})" to null,
                                    "Pendientes ($inPorteriaCount)" to PhysicalReceiptStatus.EN_PORTERIA,
                                    "Entregados ($deliveredCount)" to PhysicalReceiptStatus.ENTREGADO
                                ),
                                selected = selectedFilter,
                                onSelected = { selectedFilter = it }
                            )
                        }

                        if (displayedReceipts.isEmpty()) {
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
                                        Spacer(modifier = Modifier.height(10.dp))
                                        Text(
                                            "No tienes correspondencia en esta sección",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = Slate700
                                        )
                                    }
                                }
                            }
                        } else {
                            items(displayedReceipts, key = { it.id }) { receipt ->
                                DashboardEventCard(
                                    receipt = receipt,
                                    onInspectPhoto = { selectedReceiptForDetail = receipt }
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }

                1 -> {
                    // TAB 1: NOTIFICACIONES
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            DashboardSectionHeader(
                                title = "Notificaciones y Avisos",
                                subtitle = "Historial en tiempo real de correspondencia y novedades"
                            )
                        }

                        // Avisos de correspondencia reciente
                        items(notifications, key = { it.id }) { notif ->
                            Card(
                                shape = RoundedCardShape,
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, Slate200),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Sky100,
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Notifications,
                                            contentDescription = null,
                                            tint = Sky700,
                                            modifier = Modifier
                                                .padding(10.dp)
                                                .fillMaxSize()
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            notif.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Slate950
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            notif.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Slate600
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            notif.timestamp,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate400
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            DashboardSectionHeader(
                                title = "Comunicados del Conjunto",
                                subtitle = "Mantenimientos generales, piscina y avisos de administración"
                            )
                        }

                        items(announcements, key = { it.id }) { ann ->
                            Card(
                                shape = RoundedCardShape,
                                colors = CardDefaults.cardColors(containerColor = PureWhite),
                                border = BorderStroke(1.dp, Slate200),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            ann.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Slate950
                                        )
                                        Text(
                                            ann.date,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Slate500
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        ann.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Slate700
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        "Emitido por: ${ann.author}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Slate400
                                    )
                                }
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }

                2 -> {
                    // TAB 2: PERFIL Y CONFIGURACIÓN DEL RESIDENTE
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(6.dp))

                            // Perfil Card
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
                                        color = Emerald100,
                                        border = BorderStroke(2.dp, Emerald600)
                                    ) {
                                        Icon(
                                            Icons.Default.Person,
                                            contentDescription = "Avatar Residente",
                                            tint = Emerald800,
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
                                        color = Sky100,
                                        shape = RoundedPillShape,
                                        modifier = Modifier.padding(top = 6.dp)
                                    ) {
                                        Text(
                                            text = "Apartamento: ${user.apartment ?: "Torre 1 - 302"}",
                                            color = Sky700,
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
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Home, contentDescription = null, tint = Slate500, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("Inmueble: ${user.apartment ?: "Torre 1 - 302"}", style = MaterialTheme.typography.bodyMedium, color = Slate700)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    OutlinedButton(
                                        onClick = {
                                            editName = user.name
                                            editEmail = user.email
                                            editPhone = user.phone ?: ""
                                            editApartment = user.apartment ?: "Torre 1 - 302"
                                            showEditProfileDialog = true
                                        },
                                        shape = RoundedButtonShape,
                                        border = BorderStroke(1.dp, Slate300),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Configurar Datos de Contacto", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }

                        // App Configuration Card
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
                                        text = "Ajustes de Notificación",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Slate950
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Notificaciones Push", style = MaterialTheme.typography.titleSmall, color = Slate900)
                                            Text("Avisar al recibir correspondencia", style = MaterialTheme.typography.bodySmall, color = Slate500)
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
                                            Text("Sonido de Alerta de Timbre", style = MaterialTheme.typography.titleSmall, color = Slate900)
                                            Text("Tono audible al clasificar nuevo recibo", style = MaterialTheme.typography.bodySmall, color = Slate500)
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
                                            Text("Aviso vía SMS o Celular", style = MaterialTheme.typography.titleSmall, color = Slate900)
                                            Text("Enviar mensaje de texto si no está en casa", style = MaterialTheme.typography.bodySmall, color = Slate500)
                                        }
                                        Switch(
                                            checked = smsAlertsEnabled,
                                            onCheckedChange = { smsAlertsEnabled = it }
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
                                            Text("Modo claro de alto contraste", style = MaterialTheme.typography.bodySmall, color = Slate500)
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
                                Text("Cerrar Sesión de Residente", style = MaterialTheme.typography.labelLarge)
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }

    // Modal para Inspección de Foto HD de Recibo
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

                    Text("Apartamento: ${selectedReceiptForDetail!!.apartment}", style = MaterialTheme.typography.titleSmall, color = Slate900)
                    Text("Guarda que recibió: ${selectedReceiptForDetail!!.receivedByGuard}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    Text("Fecha y hora: ${selectedReceiptForDetail!!.receivedTimestamp}", style = MaterialTheme.typography.bodySmall, color = Slate500)
                    Text("Nota de portería: ${selectedReceiptForDetail!!.guardNote}", style = MaterialTheme.typography.bodyMedium, color = Slate700, modifier = Modifier.padding(top = 4.dp))

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { selectedReceiptForDetail = null },
                        shape = RoundedButtonShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Sky600),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Cerrar Visor", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }

    // Modal Editar Perfil Residente
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Configurar Datos del Residente", style = MaterialTheme.typography.titleMedium) },
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
                    OutlinedTextField(
                        value = editApartment,
                        onValueChange = { editApartment = it },
                        label = { Text("Apartamento") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onUpdateProfile(editName, editEmail, editPhone, editApartment)
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
}
