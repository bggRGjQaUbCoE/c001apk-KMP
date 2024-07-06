package ui.component.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import constant.Constants.EMPTY_STRING
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import logic.model.HomeFeedResponse
import ui.component.KamelLoader
import ui.theme.cardBg

/**
 * Created by bggRGjQaUbCoE on 2024/6/3
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CarouselCard(
    modifier: Modifier = Modifier,
    entities: List<HomeFeedResponse.Entities>?,
    onOpenLink: (String, String) -> Unit
) {

    entities?.let {
        Box {
            val dataList = it.toMutableList()
            if (it.size > 1) {
                val tmp = it.first()
                dataList.add(0, it.last())
                dataList.add(dataList.size, tmp)
            }

            val pagerState =
                rememberPagerState(initialPage = if (it.size > 1) 1 else 0) { dataList.size }

            if (it.size > 1) {
                LaunchedEffect(pagerState.currentPage) {
                    if (pagerState.currentPage == 0) {
                        delay(500)
                        pagerState.scrollToPage(pagerState.pageCount - 2)
                    } else if (pagerState.currentPage == pagerState.pageCount - 1) {
                        delay(500)
                        pagerState.scrollToPage(1)
                    }
                }
            }
            val scope = rememberCoroutineScope()

            HorizontalPager(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(MaterialTheme.shapes.medium)
                    .background(cardBg())
                    .draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { detail ->
                            if (it.size > 1) {
                                scope.launch {
                                    if (detail < 0) {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    } else {
                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                    }
                                }
                            }
                        }),
                state = pagerState
            ) { index ->
                val item = dataList[index]
                KamelLoader(
                    url = item.pic,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f)
                        .clickable {
                            onOpenLink(
                                item.url.orEmpty(),
                                item.title?.replace("_首页轮播", EMPTY_STRING).orEmpty()
                            )
                        },
                )
            }

            if (pagerState.pageCount > 1) {
                CardIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 5.dp, end = 20.dp),
                    pagerState = pagerState,
                    isCarousel = true
                )
            }

        }
    }
}