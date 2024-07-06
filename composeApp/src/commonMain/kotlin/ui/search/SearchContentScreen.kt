package ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import ui.component.FooterCard
import ui.component.ItemCard
import util.ReportType

/**
 * Created by bggRGjQaUbCoE on 2024/6/9
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContentScreen(
    childComponentContext: ComponentContext,
    searchType: SearchType,
    keyword: String,
    pageType: String?,
    pageParam: String?,
    refreshState: Boolean,
    resetRefreshState: () -> Unit,
    feedType: SearchFeedType,
    orderType: SearchOrderType,
    paddingValues: PaddingValues,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    updateInitPage: () -> Unit,
    onReport: (String, ReportType) -> Unit,
    onViewImage: (List<String>, Int) -> Unit,
) {

    val view = LocalLifecycleOwner.current
    if (view.lifecycle.currentState == Lifecycle.State.RESUMED) {
        updateInitPage()
    }

    val viewModel =
        childComponentContext.instanceKeeper.getOrCreate(key = searchType.name + keyword + pageType) {
            koinInject<SearchContentViewModel> {
                parametersOf(
                    when (searchType) {
                        SearchType.FEED -> "feed"
                        SearchType.APP -> "apk"
                        SearchType.GAME -> "game"
                        SearchType.PRODUCT -> "product"
                        SearchType.USER -> "user"
                        SearchType.TOPIC -> "feedTopic"
                    },
                    keyword, pageType, pageParam,
                )
            }
        }

    val lazyListState = rememberLazyListState()

    if (searchType == SearchType.FEED) {
        LaunchedEffect(feedType) {
            if (feedType != viewModel.searchFeedType) {
                viewModel.searchFeedType = feedType
                viewModel.feedType = when (feedType) {
                    SearchFeedType.ALL -> "all"
                    SearchFeedType.FEED -> "feed"
                    SearchFeedType.ARTICLE -> "feedArticle"
                    SearchFeedType.COOLPIC -> "picture"
                    SearchFeedType.COMMENT -> "comment"
                    SearchFeedType.RATING -> "rating"
                    SearchFeedType.ANSWER -> "answer"
                    SearchFeedType.QUESTION -> "question"
                    SearchFeedType.VOTE -> "vote"
                }
                lazyListState.scrollToItem(0)
                viewModel.refresh()
            }
        }

        LaunchedEffect(orderType) {
            if (orderType != viewModel.sortType) {
                viewModel.sortType = orderType
                viewModel.sort = when (orderType) {
                    SearchOrderType.DATELINE -> "default"
                    SearchOrderType.HOT -> "hot"
                    SearchOrderType.REPLY -> "reply"
                }
                lazyListState.scrollToItem(0)
                viewModel.refresh()
            }
        }
    }

    val layoutDirection = LocalLayoutDirection.current
    val state = rememberPullToRefreshState()

    LaunchedEffect(refreshState) {
        if (refreshState) {
            resetRefreshState()
            if (/*isActive*/view.lifecycle.currentState == Lifecycle.State.RESUMED) {
                viewModel.refresh()
                lazyListState.scrollToItem(0)
            }
        }
    }


    PullToRefreshBox(
        modifier = Modifier.padding(
            start = paddingValues.calculateLeftPadding(layoutDirection),
            end = paddingValues.calculateRightPadding(layoutDirection),
        ),
        state = state,
        isRefreshing = viewModel.isRefreshing,
        onRefresh = viewModel::refresh,
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = viewModel.isRefreshing,
                state = state,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(
                top = 10.dp,
                bottom = 10.dp + paddingValues.calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            state = lazyListState
        ) {
            ItemCard(
                loadingState = viewModel.loadingState,
                loadMore = viewModel::loadMore,
                isEnd = viewModel.isEnd,
                onViewUser = onViewUser,
                onViewFeed = onViewFeed,
                onOpenLink = onOpenLink,
                onCopyText = onCopyText,
                onReport = onReport,
                onLike = { id, like, likeType ->
                    viewModel.onLike(id, like, likeType)
                },
                onDelete = { id, deleteType, _ ->
                    viewModel.onDelete(id, deleteType)
                },
                onBlockUser = { uid, _ ->
                    viewModel.onBlockUser(uid)
                },
                onFollowUser = { uid, isFollow ->
                    viewModel.onFollowUser(uid, isFollow)
                },
                onViewImage = onViewImage,
            )

            FooterCard(
                modifier = Modifier.padding(horizontal = 10.dp),
                footerState = viewModel.footerState,
                loadMore = viewModel::loadMore,
                isFeed = false
            )

        }
    }

    val toaster = rememberToasterState()
    Toaster(
        state = toaster,
        alignment = Alignment.BottomCenter
    )

    viewModel.toastText?.let {
        viewModel.resetToastText()
        toaster.show(it)
    }

}