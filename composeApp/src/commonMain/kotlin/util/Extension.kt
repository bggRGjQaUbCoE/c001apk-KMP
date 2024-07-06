package util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import constant.Constants.EMPTY_STRING
import constant.Constants.PREFIX_APP
import constant.Constants.PREFIX_FEED
import constant.Constants.PREFIX_TOPIC
import constant.Constants.PREFIX_USER
import constant.Constants.SUFFIX_THUMBNAIL
import constant.Constants.UTF8
import org.jsoup.nodes.Document
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.regex.Pattern

/**
 * Created by bggRGjQaUbCoE on 2024/6/30
 */
inline val String.http2https: String
    get() = if (this.getOrElse(4) { 's' } == 's') this
    else StringBuilder(this).insert(4, 's').toString()

fun Modifier.noRippleToggleable(
    value: Boolean,
    onValueChange: (Boolean) -> Unit
): Modifier = composed {
    toggleable(
        value = value,
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onValueChange = onValueChange
    )
}

inline fun Modifier.noRippleClickable(
    enabled: Boolean = true,
    crossinline onClick: () -> Unit
): Modifier = composed {
    clickable(
        enabled = enabled,
        indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}

enum class ShareType {
    FEED, APP, TOPIC, USER
}

fun getShareText(type: ShareType, id: String): String {
    val prefix = when (type) {
        ShareType.APP -> PREFIX_APP
        ShareType.FEED -> PREFIX_FEED
        ShareType.TOPIC -> PREFIX_TOPIC
        ShareType.USER -> PREFIX_USER
    }
    return "https://www.coolapk1s.com$prefix$id"
}

inline val String.getAllLinkAndText: String
    get() = if (isEmpty()) EMPTY_STRING else
        Pattern.compile("<a class=\"feed-link-url\"\\s+href=\"([^<>\"]*)\"[^<]*[^>]*>")
            .matcher(this).replaceAll(" $1 ")

// onDoubleClick = {} //双击时回调
// onPress = {} //按下时回调
// onLongPress = {} //长按时回调
// onTap = {} //轻触时回调(按下并抬起)
fun Modifier.doubleClick(onDoubleClick: (Offset) -> Unit): Modifier =
    pointerInput(this) {
        detectTapGestures(
            onDoubleTap = onDoubleClick
        )
    }

fun Modifier.longClick(onLongClick: (Offset) -> Unit): Modifier =
    pointerInput(this) {
        detectTapGestures(
            onLongPress = onLongClick
        )
    }

@Composable
inline fun composeClick(
    time: Int = 500,
    crossinline onClick: () -> Unit
): () -> Unit {
    var lastClickTime by remember { mutableLongStateOf(value = 0L) }
    return {
        val currentTimeMillis = System.currentTimeMillis()
        if (currentTimeMillis - lastClickTime >= time) {
            onClick()
            lastClickTime = currentTimeMillis
        }
    }
}

@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

inline val String?.encode: String
    get() = URLEncoder.encode(this?.replace("%", "%25")?.replace("+", "%2B"), UTF8)
inline val String.decode: String
    get() = URLDecoder.decode(this, UTF8)

fun Document.createRequestHash(): String =
    this.getElementsByTag("Body").attr("data-request-hash")

fun createRandomNumber() = Math.random().toString().replace(".", "undefined")

enum class ReportType {
    FEED, REPLY, USER
}

fun getReportUrl(id: String, type: ReportType): String =
    when (type) {
        ReportType.FEED -> "https://m.coolapk.com/mp/do?c=feed&m=report&type=feed&id=$id"
        ReportType.REPLY -> "https://m.coolapk.com/mp/do?c=feed&m=report&type=feed_reply&id=$id"
        ReportType.USER -> "https://m.coolapk.com/mp/do?c=user&m=report&id=$id"
    }

fun getImageLp(url: String): Pair<Float, Float> {
    var imgWidth = 1f
    var imgHeight = 1f
    val at = url.lastIndexOf("@")
    val x = url.lastIndexOf("x")
    val dot = url.lastIndexOf(".")
    if (at != -1 && x != -1 && dot != -1) {
        imgWidth = url.substring(at + 1, x).toFloat()
        imgHeight = url.substring(x + 1, dot).toFloat()
    }
    return Pair(imgWidth, imgHeight)
}

inline val String.toThumb
    get() = "$this$SUFFIX_THUMBNAIL"