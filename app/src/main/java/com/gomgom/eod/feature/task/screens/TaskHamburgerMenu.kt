package com.gomgom.eod.feature.task.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gomgom.eod.R

@Composable
fun TaskHamburgerMenuButton(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    iconTint: Color,
    menuBackgroundColor: Color,
    dividerColor: Color,
    textColor: Color,
    onHomeClick: () -> Unit,
    onKorClick: () -> Unit,
    onEngClick: () -> Unit,
    onAppInfoClick: () -> Unit,
    onGuideClick: () -> Unit,
    onContactClick: () -> Unit,
    onExitClick: () -> Unit
) {
    Box(
        modifier = Modifier.size(42.dp),
        contentAlignment = Alignment.Center
    ) {
        TextButton(
            onClick = { onExpandedChange(true) },
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Menu,
                contentDescription = stringResource(R.string.common_home),
                tint = iconTint,
                modifier = Modifier.size(56.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(menuBackgroundColor)
        ) {
            DropdownMenuItem(
                text = { TaskHamburgerMenuText(stringResource(R.string.home_menu_home), textColor) },
                onClick = {
                    onExpandedChange(false)
                    onHomeClick()
                }
            )
            HorizontalDivider(color = dividerColor)
            DropdownMenuItem(
                text = { TaskHamburgerMenuText(stringResource(R.string.home_menu_kor), textColor) },
                onClick = {
                    onExpandedChange(false)
                    onKorClick()
                }
            )
            DropdownMenuItem(
                text = { TaskHamburgerMenuText(stringResource(R.string.home_menu_eng), textColor) },
                onClick = {
                    onExpandedChange(false)
                    onEngClick()
                }
            )
            HorizontalDivider(color = dividerColor)
            DropdownMenuItem(
                text = { TaskHamburgerMenuText(stringResource(R.string.home_menu_app_info), textColor) },
                onClick = {
                    onExpandedChange(false)
                    onAppInfoClick()
                }
            )
            DropdownMenuItem(
                text = { TaskHamburgerMenuText(stringResource(R.string.home_menu_guide), textColor) },
                onClick = {
                    onExpandedChange(false)
                    onGuideClick()
                }
            )
            DropdownMenuItem(
                text = { TaskHamburgerMenuText(stringResource(R.string.home_menu_contact), textColor) },
                onClick = {
                    onExpandedChange(false)
                    onContactClick()
                }
            )
            HorizontalDivider(color = dividerColor)
            DropdownMenuItem(
                text = { TaskHamburgerMenuText(stringResource(R.string.home_menu_exit), textColor) },
                onClick = {
                    onExpandedChange(false)
                    onExitClick()
                }
            )
        }
    }
}

@Composable
private fun TaskHamburgerMenuText(
    text: String,
    textColor: Color
) {
    Text(
        text = text,
        color = textColor,
        fontSize = 15.sp,
        fontWeight = FontWeight.Medium
    )
}
