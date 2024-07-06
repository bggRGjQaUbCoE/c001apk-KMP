package ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import util.http2https

/**
 * Created by bggRGjQaUbCoE on 2024/6/30
 */
@Composable
fun KamelLoader(
    modifier: Modifier = Modifier,
    url: String?,
    contentScale: ContentScale = ContentScale.Crop,
    isChat: Boolean = false,
) {
    with(if (isChat) url else url?.http2https) {
        this?.let {
            KamelImage(
                modifier = modifier,
                resource = asyncPainterResource(data = it),
                contentScale = contentScale,
                contentDescription = null,
            )
        }
    }

}