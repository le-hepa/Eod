package com.gomgom.eod.core.common

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun AutoResizeText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle,
    maxLines: Int = 2,
    minFontSize: TextUnit = 14.sp,
    step: TextUnit = 1.sp,
    textAlign: TextAlign = TextAlign.Center
) {
    var resizedStyle by remember(text, style) { mutableStateOf(style) }
    var readyToDraw by remember(text) { mutableStateOf(false) }

    Text(
        text = text,
        modifier = modifier,
        style = resizedStyle,
        maxLines = maxLines,
        softWrap = true,
        overflow = TextOverflow.Ellipsis,
        textAlign = textAlign,
        onTextLayout = { result: TextLayoutResult ->
            if (result.hasVisualOverflow && resizedStyle.fontSize > minFontSize) {
                val nextSize = (resizedStyle.fontSize.value - step.value).coerceAtLeast(minFontSize.value)
                resizedStyle = resizedStyle.copy(fontSize = nextSize.sp)
            } else {
                readyToDraw = true
            }
        },
        color = style.color.copy(alpha = if (readyToDraw) 1f else 0f)
    )
}