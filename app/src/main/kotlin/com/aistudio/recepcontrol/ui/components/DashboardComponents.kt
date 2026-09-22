package com.aistudio.recepcontrol.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aistudio.recepcontrol.R
import com.aistudio.recepcontrol.model.PhysicalReceipt
import com.aistudio.recepcontrol.model.PhysicalReceiptStatus
import com.aistudio.recepcontrol.ui.theme.*

/**
 * Modern High-Contrast Metric Counter Card for Dashboard Views.
 */
@Composable
fun DashboardMetricCard(
    title: String,
    count: Int,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCardShape,
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(2.dp, accentColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Slate950,
                    fontWeight = FontWeight.ExtraBold
                )
                Surface(
                    shape = CircleShape,
                    color = containerColor,
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(7.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$count",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 30.sp,
                    color = textColor,
                    fontWeight = FontWeight.Black
                )
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = Slate700
            )
        }
    }
}

/**
 * Clean Section Header with High-Contrast Typography.
 */
@Composable
fun DashboardSectionHeader(
    title: String,
    subtitle: String? = null,
    badgeText: String? = null,
    badgeColor: Color = Sky100,
    badgeTextColor: Color = Sky700,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = Slate950,
                fontWeight = FontWeight.Bold
            )
            if (badgeText != null) {
                Surface(
                    color = badgeColor,
                    shape = RoundedPillShape
                ) {
                    Text(
                        text = badgeText,
                        color = badgeTextColor,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Slate600
            )
        }
    }
}

/**
 * Modern High-Contrast Rounded Event & Receipt Card for Dashboards.
 */
@Composable
fun DashboardEventCard(
    receipt: PhysicalReceipt,
    onInspectPhoto: () -> Unit,
    onMarkDelivered: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isPending = receipt.status == PhysicalReceiptStatus.EN_PORTERIA

    Card(
        shape = RoundedCardShape,
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        border = BorderStroke(1.5.dp, Slate300),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Apartment / Location pill + Status pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Sky100,
                    shape = RoundedPillShape,
                    border = BorderStroke(1.5.dp, Sky600)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Home,
                            contentDescription = null,
                            tint = Sky700,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = receipt.apartment,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Sky700
                        )
                    }
                }

                Surface(
                    color = if (isPending) Amber100 else Emerald100,
                    shape = RoundedPillShape,
                    border = BorderStroke(1.5.dp, if (isPending) Amber600 else Emerald600)
                ) {
                    Text(
                        text = if (isPending) "En Portería" else "Entregado",
                        color = if (isPending) Amber900 else Emerald900,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body: Thumbnail + High-Contrast Metadata
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(2.dp, Slate300, RoundedCornerShape(14.dp))
                        .clickable { onInspectPhoto() }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_receipt_sample),
                        contentDescription = "Foto recibo físico",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Surface(
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = CircleShape,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.ZoomIn,
                            contentDescription = "Ver HD",
                            tint = PureWhite,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = receipt.serviceCategory,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate950
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Recibido: ${receipt.receivedTimestamp}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Slate600
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = receipt.guardNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate800,
                        maxLines = 2
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onInspectPhoto,
                    shape = RoundedButtonShape,
                    border = BorderStroke(1.5.dp, Slate400),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Slate900),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ZoomIn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ver Foto HD", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                }

                if (isPending && onMarkDelivered != null) {
                    Button(
                        onClick = onMarkDelivered,
                        shape = RoundedButtonShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Emerald600,
                            contentColor = PureWhite
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Entregar", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}

/**
 * Filter Chip Group with high-contrast pills.
 */
@Composable
fun <T> DashboardFilterGroup(
    options: List<Pair<String, T?>>,
    selected: T?,
    onSelected: (T?) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (label, value) ->
            val isSelected = selected == value
            Surface(
                onClick = { onSelected(value) },
                shape = RoundedPillShape,
                color = if (isSelected) Slate950 else PureWhite,
                border = BorderStroke(1.5.dp, if (isSelected) Slate950 else Slate400),
                shadowElevation = if (isSelected) 3.dp else 1.dp
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isSelected) PureWhite else Slate800,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }
    }
}
