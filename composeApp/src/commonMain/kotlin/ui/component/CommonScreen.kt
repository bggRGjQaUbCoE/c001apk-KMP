package ui.component

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
import ui.base.BaseViewModel
import util.ReportType
import util.isScrollingUp

/**
 * Created by bggRGjQaUbCoE on 2024/6/10
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommonScreen(
    modifier: Modifier = Modifier,
    viewModel: BaseViewModel,
    refreshState: Boolean?,
    resetRefreshState: () -> Unit,
    paddingValues: PaddingValues = PaddingValues(),
    needTopPadding: Boolean = false,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    isHomeFeed: Boolean = false,
    onReport: ((String, ReportType) -> Unit)? = null,
    onViewFFFList: ((String?, String, String?, String?) -> Unit)? = null,
    onHandleRecent: ((String, String, String, Int) -> Unit)? = null,
    onHandleMessage: ((String, Int) -> Unit)? = null,
    onViewChat: ((String, String, String) -> Unit)? = null,
    onDeleteNotice: ((String) -> Unit)? = null,
    isScrollingUp: ((Boolean) -> Unit)? = null,
    onViewImage: (List<String>, Int) -> Unit,
) {

    val view = LocalLifecycleOwner.current
    val layoutDirection = LocalLayoutDirection.current
    val lazyListState = rememberLazyListState()
    val state = rememberPullToRefreshState()

    LaunchedEffect(refreshState) {
        if (refreshState == true) {
            resetRefreshState()
            if (/*isActive*/view.lifecycle.currentState == Lifecycle.State.RESUMED) {
                viewModel.refresh()
                lazyListState.scrollToItem(0)
            }
        }
    }

    isScrollingUp?.let {
        it(lazyListState.isScrollingUp())
    }

    PullToRefreshBox(
        modifier = modifier.padding(
            start = paddingValues.calculateLeftPadding(layoutDirection),
            end = paddingValues.calculateRightPadding(layoutDirection),
            top = if (needTopPadding) paddingValues.calculateTopPadding() else 0.dp
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
                isHomeFeed = isHomeFeed,
                onReport = onReport,
                onViewFFFList = onViewFFFList,
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
                onHandleRecent = onHandleRecent,
                onHandleMessage = onHandleMessage,
                onViewChat = onViewChat,
                onDeleteNotice = onDeleteNotice,
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

}