package app.sefer.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun SeferIcon(
    res: Int,
    contentDescription: String?,
    tint: Color = LocalContentColor.current,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    Icon(
        painter = painterResource(res),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier.size(size),
    )
}

/** Bolds and tints every case-insensitive occurrence of [terms] inside [text]. */
fun highlightTerms(text: String, terms: List<String>, color: Color, background: Color): AnnotatedString {
    val cleaned = terms.filter { it.isNotBlank() }.sortedByDescending { it.length }
    if (cleaned.isEmpty()) return AnnotatedString(text)
    val regex = Regex(cleaned.joinToString("|") { Regex.escape(it) }, RegexOption.IGNORE_CASE)
    return buildAnnotatedString {
        var last = 0
        for (m in regex.findAll(text)) {
            if (m.range.first < last) continue
            append(text.substring(last, m.range.first))
            pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = color, background = background))
            append(m.value)
            pop()
            last = m.range.last + 1
        }
        append(text.substring(last))
    }
}
