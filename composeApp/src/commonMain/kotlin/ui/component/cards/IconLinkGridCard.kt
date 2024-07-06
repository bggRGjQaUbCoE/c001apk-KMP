package ui.component.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import logic.model.HomeFeedResponse
import ui.component.KamelLoader
import ui.theme.cardBg

/**
 * Created by bggRGjQaUbCoE on 2024/6/5
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IconLinkGridCard(
    modifier: Modifier = Modifier,
    entities: List<HomeFeedResponse.Entities>?,
    onOpenLink: (String, String?) -> Unit
) {

    entities?.let {
        val pagerState = rememberPagerState { it.size / 5 }
        val scope = rememberCoroutineScope()
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(cardBg())
        ) {
            HorizontalPager(
                modifier = Modifier
                    .fillMaxWidth()
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { detail ->
                            if ((it.size / 5) > 1) {
                                scope.launch {
                                    if (detail < 0 && pagerState.currentPage == 0) {
                                        pagerState.animateScrollToPage(1)
                                    }
                                    if (detail > 0 && pagerState.currentPage == 1) {
                                        pagerState.animateScrollToPage(0)
                                    }
                                }
                            }
                        }),
                state = pagerState
            ) { index ->

                Row(modifier = Modifier.fillMaxWidth()) {
                    (0..4).forEach {
                        IconLinkGridCardItem(
                            Modifier.weight(1f),
                            entities.getOrNull(index * 5 + it)?.pic.orEmpty(),
                            entities.getOrNull(index * 5 + it)?.url.orEmpty(),
                            entities.getOrNull(index * 5 + it)?.title.orEmpty(),
                            onOpenLink
                        )
                    }
                }
            }

            if (pagerState.pageCount > 1) {
                CardIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    dimension = 5.dp,
                    defWidth = 1.5f,
                    selectedWidth = 2f,
                    defColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    pagerState = pagerState,
                )
            }

        }
    }

}

@Composable
fun IconLinkGridCardItem(
    modifier: Modifier = Modifier,
    pic: String,
    url: String,
    title: String,
    onOpenLink: (String, String?) -> Unit
) {

    Column(
        modifier = modifier
            .clickable {
                onOpenLink(url, title)
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        KamelLoader(
            url = pic,
            modifier = Modifier
                .padding(top = 4.dp)
                .size(36.dp),
        )

        Text(
            text = title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 2.dp)
        )
    }

}