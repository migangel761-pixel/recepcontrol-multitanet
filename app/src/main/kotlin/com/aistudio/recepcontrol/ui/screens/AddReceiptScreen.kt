package com.aistudio.recepcontrol.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhotoLibrary
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
import com.aistudio.recepcontrol.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReceiptScreen(
    onBack: () -> Unit,
    onConfirmRegistration: (serviceCategory: String, apartment: String, guardNote: String) -> Unit
) {
    val eventTypes = listOf(
        "Energía Eléctrica (Enel)",
        "Acueducto y Alcantarillado (EAAB)",
        "Gas Natural (Vanti)",
        "Telecomunicaciones (Claro / Movistar / Tigo)",
        "Cuota de Administración Mensual",
        "Mantenimiento de Ascensores",
        "Mantenimiento Zonas Comunes / Piscina",
        "Mantenimiento Motobombas y Red Hidráulica",
        "Paquete o Encomienda en Portería",
        "Comunicado o Novedad General"
    )

    val destinationList = listOf(
        "Torre 1 - 302",
        "Torre 1 - 101",
        "Torre 1 - 102",
        "Torre 1 - 201",
        "Torre 1 - 202",
        "Torre 1 - 301",
        "Torre 2 - 101",
        "Torre 2 - 104",
        "Torre 2 - 201",
        "Torre 2 - 301",
        "Torre 2 - 501",
        "Torre 3 - 101",
        "Torre 3 - 203",
        "Zonas Comunes / Edificio Completo",
        "Administración / Consejo"
    )

    var selectedEventType by remember { mutableStateOf(eventTypes[0]) }
    var eventTypeDropdownExpanded by remember { mutableStateOf(false) }

    var selectedApartment by remember { mutableStateOf(destinationList[0]) }
    var apartmentDropdownExpanded by remember { mutableStateOf(false) }
    var apartmentSearchText by remember { mutableStateOf("") }

    var guardObservation by remember { mutableStateOf("Recibo físico verificado y clasificado en casillero de portería.") }
    var isSimulatingCamera by remember { mutableStateOf(false) }

    val filteredDestinations = remember(apartmentSearchText) {
        if (apartmentSearchText.isBlank()) destinationList
        else destinationList.filter { it.contains(apartmentSearchText, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Registrar Evento / Correspondencia", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                        Text("Puesto de Portería • Registro fotográfico y notificación", fontSize = 12.sp, color = Color(0xFFBAE6FD))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Fotografía / Captura de Evidencia (Light card)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Fotografía / Evidencia Física",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        }

                        Surface(
                            color = Color(0xFFD1FAE5),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Captura Lista",
                                color = Color(0xFF065F46),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Photo Viewer Frame
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .background(Color(0xFF0F172A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_receipt_sample),
                            contentDescription = "Foto recibo físico o evento",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Camera overlay badge
                        Surface(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Digitalizado HD • Portería",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Simulated Camera Re-take action
                    OutlinedButton(
                        onClick = { isSimulatingCamera = !isSimulatingCamera },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSimulatingCamera) "Guardar Nueva Foto" else "Tomar / Repetir Fotografía",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Datos del Registro: Desplegables y Observación (Light card)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Datos del Evento o Correspondencia",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )

                    // 1. DESPLEGABLE: Tipo de Recibo / Evento
                    ExposedDropdownMenuBox(
                        expanded = eventTypeDropdownExpanded,
                        onExpandedChange = { eventTypeDropdownExpanded = !eventTypeDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedEventType,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Recibo / Evento") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = eventTypeDropdownExpanded) },
                            leadingIcon = {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = eventTypeDropdownExpanded,
                            onDismissRequest = { eventTypeDropdownExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            eventTypes.forEach { type ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            type,
                                            fontWeight = if (type == selectedEventType) FontWeight.Bold else FontWeight.Normal,
                                            color = if (type == selectedEventType) MaterialTheme.colorScheme.primary else Color(0xFF0F172A)
                                        )
                                    },
                                    onClick = {
                                        selectedEventType = type
                                        eventTypeDropdownExpanded = false
                                        // Update default suggestion for observation if it's maintenance
                                        if (type.contains("Mantenimiento", ignoreCase = true)) {
                                            guardObservation = "Mantenimiento realizado por personal técnico autorizado. Registro en portería."
                                        } else if (type.contains("Recibo", ignoreCase = true) || type.contains("Energía", ignoreCase = true)) {
                                            guardObservation = "Recibo físico clasificado en casillero de portería."
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // 2. DESPLEGABLE: Apartamento o Área Destino
                    ExposedDropdownMenuBox(
                        expanded = apartmentDropdownExpanded,
                        onExpandedChange = { apartmentDropdownExpanded = !apartmentDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedApartment,
                            onValueChange = {
                                selectedApartment = it
                                apartmentSearchText = it
                            },
                            label = { Text("Apartamento o Área Destino (Buscar o desplegar)") },
                            placeholder = { Text("Selecciona o escribe el apartamento...") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = apartmentDropdownExpanded) },
                            leadingIcon = {
                                Icon(Icons.Default.Home, contentDescription = null, tint = Color(0xFF059669))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC)
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = apartmentDropdownExpanded,
                            onDismissRequest = { apartmentDropdownExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            filteredDestinations.forEach { apt ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            apt,
                                            fontWeight = if (apt == selectedApartment) FontWeight.Bold else FontWeight.Normal,
                                            color = if (apt == selectedApartment) Color(0xFF059669) else Color(0xFF0F172A)
                                        )
                                    },
                                    onClick = {
                                        selectedApartment = apt
                                        apartmentDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // 3. ESPACIO PARA OBSERVACIÓN
                    OutlinedTextField(
                        value = guardObservation,
                        onValueChange = { guardObservation = it },
                        label = { Text("Observación / Novedad de Portería") },
                        placeholder = { Text("Detalles del casillero, novedad técnica, empresa de mantenimiento, etc.") },
                        leadingIcon = {
                            Icon(Icons.Default.EditNote, contentDescription = null, tint = Color(0xFF64748B))
                        },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8FAFC),
                            unfocusedContainerColor = Color(0xFFF8FAFC)
                        )
                    )
                }
            }

            // Notification Info Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFE0F2FE),
                border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Notificación Automática Instantánea",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0369A1)
                        )
                        Text(
                            text = "Al registrar este evento, se enviará una notificación con la fotografía al residente o al conjunto.",
                            fontSize = 12.sp,
                            color = Color(0xFF0C4A6E)
                        )
                    }
                }
            }

            // Botón Confirmar Registro
            Button(
                onClick = {
                    onConfirmRegistration(
                        selectedEventType,
                        selectedApartment,
                        guardObservation
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Confirmar Registro de Evento",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
