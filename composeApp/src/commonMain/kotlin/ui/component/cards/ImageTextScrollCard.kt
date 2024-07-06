package ui.component.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import logic.model.HomeFeedResponse
import ui.component.KamelLoader
import ui.theme.cardBg

/**
 * Created by bggRGjQaUbCoE on 2024/6/6
 */
@Composable
fun ImageTextScrollCard(
    modifier: Modifier = Modifier,
    data: HomeFeedResponse.Data,
    onOpenLink: (String, String?) -> Unit,
) {

    BoxWithConstraints {

        val itemWidth by lazy { (maxWidth - 20.dp) / 3f * 2 }

        Column(
            modifier = modifier.fillMaxWidth()
        ) {
            if (!data.title.isNullOrEmpty()) {
                TitleCard(
                    modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp),
                    url = data.url.orEmpty(),
                    title = data.title,
                    onOpenLink = onOpenLink,
                )
            }

            data.entities?.let {
                val scrollState = rememberLazyListState()
                val scope = rememberCoroutineScope()
                LazyRow(
                    state = scrollState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .draggable(
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { detail ->
                                scope.launch {
                                    scrollState.scrollBy(-detail)
                                }
                            }
                        ),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp)
                ) {
                    it.forEach { item ->
                        item(key = item.id) {
                            ImageTextScrollCardItem(
                                url = item.url.orEmpty(),
                                pic = item.pic.orEmpty(),
                                title = item.title.orEmpty(),
                                onOpenLink = onOpenLink,
                                itemWidth = itemWidth,
                            )
                        }
                    }

                }
            }

        }

    }

}

@Composable
fun ImageTextScrollCardItem(
    modifier: Modifier = Modifier,
    url: String,
    pic: String,
    title: String,
    onOpenLink: (String, String?) -> Unit,
    itemWidth: Dp,
) {

    Column(
        modifier = modifier
            .width(itemWidth)
            .clip(MaterialTheme.shapes.medium)
            .clickable {
                onOpenLink(url, title)
            }
    ) {
        KamelLoader(
            url = pic,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.22f)
        )

        Text(
            text = title,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBg())
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }

}
