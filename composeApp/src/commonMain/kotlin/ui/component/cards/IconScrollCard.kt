package ui.component.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import logic.model.HomeFeedResponse
import ui.component.KamelLoader
import ui.theme.cardBg

/**
 * Created by bggRGjQaUbCoE on 2024/6/6
 */
@Composable
fun IconScrollCard(
    modifier: Modifier = Modifier,
    data: HomeFeedResponse.Data,
    onOpenLink: (String, String?) -> Unit,
) {

    BoxWithConstraints {

        val itemWidth by lazy { (maxWidth - 30.dp) / 9f * 2 }

        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(cardBg())
        ) {

            if (!data.title.isNullOrEmpty()) {
                TitleCard(
                    modifier = Modifier.padding(top = 10.dp),
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
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    it.forEach { item ->
                        item(key = item.uid) {
                            IconScrollCardItem(
                                url = item.url.orEmpty(),
                                avatar = item.userAvatar.orEmpty(),
                                username = item.username.orEmpty(),
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
fun IconScrollCardItem(
    modifier: Modifier = Modifier,
    url: String,
    avatar: String,
    username: String,
    onOpenLink: (String, String?) -> Unit,
    itemWidth: Dp,
) {

    Column(
        modifier = modifier
            .width(itemWidth)
            .clip(MaterialTheme.shapes.medium)
            .clickable {
                onOpenLink(url, null)
            }
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        KamelLoader(
            url = avatar,
            modifier = Modifier
                .size(itemWidth / 3f * 2)
                .clip(CircleShape)
        )

        Text(
            text = username,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 5.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

    }

}