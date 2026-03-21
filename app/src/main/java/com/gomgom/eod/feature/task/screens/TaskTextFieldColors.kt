package com.gomgom.eod.feature.task.screens

import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
internal fun taskOutlinedTextFieldColors(
    primaryText: Color,
    secondaryText: Color
): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = primaryText,
    unfocusedTextColor = primaryText,
    disabledTextColor = primaryText,
    focusedPlaceholderColor = secondaryText,
    unfocusedPlaceholderColor = secondaryText,
    disabledPlaceholderColor = secondaryText,
    cursorColor = primaryText,
    focusedBorderColor = primaryText.copy(alpha = 0.42f),
    unfocusedBorderColor = primaryText.copy(alpha = 0.32f),
    disabledBorderColor = primaryText.copy(alpha = 0.22f),
    focusedLabelColor = primaryText,
    unfocusedLabelColor = secondaryText,
    disabledLabelColor = secondaryText
)
