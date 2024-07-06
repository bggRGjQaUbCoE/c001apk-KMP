package ui.component

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import be.digitalia.compose.htmlconverter.HtmlStyle
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString

/**
 * Created by bggRGjQaUbCoE on 2024/6/30
 */
@OptIn(ExperimentalTextApi::class)
@Composable
fun LinkText(
    modifier: Modifier = Modifier,
    text: String?,
    onOpenLink: ((String, String?) -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    onShowTotalReply: (() -> Unit)? = null,
    onViewImage: (() -> Unit)? = null,
    maxLines: Int? = null,
    color: Color? = null,
    textSize: TextUnit? = null,
) {
    val primary = MaterialTheme.colorScheme.primary
    val convertedText = remember(text) {
        htmlToAnnotatedString(
            text?.replace("\n", "<br/>").orEmpty(),
            style = HtmlStyle(linkSpanStyle = SpanStyle(color = primary))
        )
    }
    ClickableText(
        text = convertedText,
        modifier = modifier,
        style = TextStyle.Default.copy(
            fontSize = textSize ?: 15.sp,
            color = color ?: MaterialTheme.colorScheme.onSurface,
            lineHeight = ((textSize?.value ?: 15f) + 10f).sp
        ),
        maxLines = maxLines ?: Int.MAX_VALUE,
        onClick = { position ->
            convertedText
                .getUrlAnnotations(position, position)
                .firstOrNull()?.let { range ->
                    val url = range.item.url
                    if (url.contains("/feed/replyList")) {
                        onShowTotalReply?.let { it() }
                    } else if (url.contains("image.coolapk.com")) {
                        onViewImage?.let { it() }
                    } else {
                        onOpenLink?.let { it(range.item.url, null) }
                    }
                } ?: onClick?.let { it() }
        }
    )
}