package com.aistudio.recepcontrol.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.recepcontrol.R
import com.aistudio.recepcontrol.model.UserRole
import com.aistudio.recepcontrol.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginVigilante: (email: String, password: String, phone: String) -> Unit,
    onLoginResident: (email: String, password: String, phone: String) -> Unit,
    onRegisterResident: (email: String, password: String, fullName: String, phone: String, apartment: String) -> Unit = { _, _, _, _, _ -> },
    isLoading: Boolean = false,
    errorMessage: String? = null,
    successMessage: String? = null,
    onDismissError: () -> Unit = {}
) {
    var selectedRole by remember { mutableStateOf(UserRole.VIGILANTE) }
    var isRegistering by remember { mutableStateOf(false) }

    // Input fields for Correo, Contraseña, Celular
    var email by remember { mutableStateOf("carlos.mendoza@recepcontrol.com") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("3104567890") }
    var fullName by remember { mutableStateOf("") }
    var apartment by remember { mutableStateOf("Torre 1 - 302") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // 1. Centered App Logo / Emblem with modern distinctive icon
        Surface(
            modifier = Modifier
                .size(96.dp)
                .shadow(8.dp, CircleShape),
            shape = CircleShape,
            color = Slate900,
            border = BorderStroke(2.5.dp, Sky500)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_login_emblem),
                contentDescription = "Emblema RecepControl",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "RecepControl",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            ),
            color = Slate950
        )

        Text(
            text = "Recepción de correspondencia y novedades",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = Slate600,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
        )

        // 2. High-Contrast Two Distinct Demo Access Buttons (prominently placed)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedLargeCardShape,
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = BorderStroke(1.5.dp, Slate300),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Accesos Rápidos de Prueba",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Slate900
                    )
                    Surface(
                        color = Amber100,
                        shape = RoundedPillShape,
                        border = BorderStroke(1.dp, Amber600)
                    ) {
                        Text(
                            text = "1 Toque",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Amber900,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Demo Button 1: Demo Vigilante (High-Contrast Navy/Dark theme)
                    Surface(
                        onClick = {
                            selectedRole = UserRole.VIGILANTE
                            email = "carlos.mendoza@recepcontrol.com"
                            phone = "3104567890"
                            password = ""
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(68.dp),
                        shape = RoundedCardShape,
                        color = Slate900,
                        border = BorderStroke(1.5.dp, Sky500),
                        shadowElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Sky600,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Demo Vigilante",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = PureWhite
                                )
                                Text(
                                    text = "Ingresar como Vigilante",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = PureWhite
                                )
                            }
                        }
                    }

                    // Demo Button 2: Demo Residente (High-Contrast Emerald/Green theme)
                    Surface(
                        onClick = {
                            selectedRole = UserRole.RESIDENT
                            email = "maria.gomez@recepcontrol.com"
                            phone = "3201234567"
                            password = ""
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(68.dp),
                        shape = RoundedCardShape,
                        color = Emerald900,
                        border = BorderStroke(1.5.dp, Emerald500),
                        shadowElevation = 3.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Emerald600,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Home,
                                    contentDescription = null,
                                    tint = PureWhite,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Demo Residente",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = PureWhite
                                )
                                Text(
                                    text = "Ingresar como Residente",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = PureWhite
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Credential Inputs Form (Correo, Contraseña, Celular)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedLargeCardShape,
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            border = BorderStroke(1.5.dp, Slate300),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // High-contrast dark header for "Iniciar Sesión" with PURE WHITE text
                Surface(
                    color = Slate900,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Sky600,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.padding(7.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Iniciar Sesión",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = PureWhite
                            )
                            Text(
                                text = "Seleccione su perfil de acceso",
                                style = MaterialTheme.typography.bodySmall,
                                color = Sky200
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Role Selector with high-contrast pills and pure white text
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val isVigilante = selectedRole == UserRole.VIGILANTE
                        Surface(
                            onClick = {
                                selectedRole = UserRole.VIGILANTE
                                email = "carlos.mendoza@recepcontrol.com"
                                phone = "3104567890"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isVigilante) Slate900 else PureWhite,
                            border = BorderStroke(2.dp, if (isVigilante) Sky500 else Slate400),
                            shadowElevation = if (isVigilante) 3.dp else 1.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (isVigilante) PureWhite else Slate800,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Vigilante",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isVigilante) PureWhite else Slate800,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        val isResident = selectedRole == UserRole.RESIDENT
                        Surface(
                            onClick = {
                                selectedRole = UserRole.RESIDENT
                                email = "maria.gomez@recepcontrol.com"
                                phone = "3201234567"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isResident) Emerald900 else PureWhite,
                            border = BorderStroke(2.dp, if (isResident) Emerald500 else Slate400),
                            shadowElevation = if (isResident) 3.dp else 1.dp
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Home,
                                    contentDescription = null,
                                    tint = if (isResident) PureWhite else Slate800,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Residente",
                                    fontWeight = FontWeight.Bold,
                                    color = if (isResident) PureWhite else Slate800,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = Slate200)

                // Input 1: Correo
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Correo",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate800
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("ejemplo@recepcontrol.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Slate600)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = Slate50,
                            focusedBorderColor = Slate950,
                            unfocusedBorderColor = Slate300
                        )
                    )
                }

                // Input 2: Contraseña
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Contraseña",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate800
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("••••••••") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Slate600)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                                    tint = Slate600
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = Slate50,
                            focusedBorderColor = Slate950,
                            unfocusedBorderColor = Slate300
                        )
                    )
                }

                // Input 3: Celular
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Celular",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = Slate800
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        placeholder = { Text("ej. 310 123 4567") },
                        leadingIcon = {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = Slate600)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = PureWhite,
                            unfocusedContainerColor = Slate50,
                            focusedBorderColor = Slate950,
                            unfocusedBorderColor = Slate300
                        )
                    )
                }

                if (isRegistering) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Nombre completo") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = Slate600) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    OutlinedTextField(
                        value = apartment,
                        onValueChange = { apartment = it },
                        label = { Text("Apartamento") },
                        leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = Slate600) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onDismissError)
                    )
                }
                if (successMessage != null) {
                    Text(
                        text = successMessage,
                        color = Emerald900,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Submit Button with High Contrast and guaranteed PURE WHITE text
                Button(
                    onClick = {
                        if (isRegistering) {
                            onRegisterResident(email, password, fullName, phone, apartment)
                        } else if (selectedRole == UserRole.VIGILANTE) {
                            onLoginVigilante(email, password, phone)
                        } else {
                            onLoginResident(email, password, phone)
                        }
                    },
                    enabled = !isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedRole == UserRole.VIGILANTE) Slate900 else Emerald900,
                        contentColor = PureWhite
                    ),
                    border = BorderStroke(1.5.dp, if (selectedRole == UserRole.VIGILANTE) Sky500 else Emerald500),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = PureWhite,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Icon(
                                imageVector = if (selectedRole == UserRole.VIGILANTE) Icons.Default.Security else Icons.Default.Home,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRegistering) "Crear cuenta de residente" else if (selectedRole == UserRole.VIGILANTE) "Ingresar como Vigilante" else "Ingresar como Residente",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = PureWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = PureWhite,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                TextButton(
                    onClick = {
                        isRegistering = !isRegistering
                        if (isRegistering) selectedRole = UserRole.RESIDENT
                    },
                    enabled = !isLoading
                ) {
                    Text(if (isRegistering) "Ya tengo una cuenta" else "Crear cuenta de residente")
                }
            }
        }
    }

        Spacer(modifier = Modifier.height(18.dp))

        // High-contrast security banner footer
        Surface(
            shape = RoundedPillShape,
            color = PureWhite,
            border = BorderStroke(1.5.dp, Slate300),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Security,
                    contentDescription = null,
                    tint = Slate700,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sistema de Control de Portería • RecepControl",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate700
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
