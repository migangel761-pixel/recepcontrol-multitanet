package com.aistudio.recepcontrol.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object RecepCornerRadius {
    val Pill = 10.dp
    val Chip = 12.dp
    val Button = 14.dp
    val Card = 20.dp
    val LargeCard = 24.dp
    val Dialog = 24.dp
    val Sheet = 28.dp
}

val RoundedCardShape = RoundedCornerShape(RecepCornerRadius.Card)
val RoundedLargeCardShape = RoundedCornerShape(RecepCornerRadius.LargeCard)
val RoundedPillShape = RoundedCornerShape(RecepCornerRadius.Pill)
val RoundedButtonShape = RoundedCornerShape(RecepCornerRadius.Button)

val RecepShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(RecepCornerRadius.Pill),
    medium = RoundedCornerShape(RecepCornerRadius.Button),
    large = RoundedCornerShape(RecepCornerRadius.Card),
    extraLarge = RoundedCornerShape(RecepCornerRadius.LargeCard)
)
