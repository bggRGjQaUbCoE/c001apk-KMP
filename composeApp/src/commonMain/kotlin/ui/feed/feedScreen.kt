package ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import be.digitalia.compose.htmlconverter.htmlToString
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import constant.Constants.EMPTY_STRING
import copyToClipboard
import kotlinx.coroutines.launch
import logic.state.LoadingState
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import ui.base.LikeType
import ui.component.ArticleItem
import ui.component.BackButton
import ui.component.CaptchaDialog
import ui.component.CreateFeedDialog
import ui.component.FooterCard
import ui.component.ItemCard
import ui.component.cards.FeedCard
import ui.component.cards.FeedHeader
import ui.component.cards.FeedReplyCard
import ui.component.cards.FeedReplySortCard
import ui.component.cards.LoadingCard
import util.DeviceUtil.isLogin
import util.ReportType
import util.ShareType
import util.getAllLinkAndText
import util.getShareText
import util.isScrollingUp
import util.noRippleClickable

/**
 * Created by bggRGjQaUbCoE on 2024/6/4
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    childComponentContext: ComponentContext,
    modifier: Modifier = Modifier,
    isCompat: Boolean = true,
    onBackClick: () -> Unit,
    id: String,
    isViewReply: Boolean,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onViewImage: (List<String>, Int) -> Unit,
) {

    val viewModel =
        childComponentContext.instanceKeeper.getOrCreate(key = id) {
            koinInject<FeedViewModel> { parametersOf(id, isViewReply) }
        }

    val layoutDirection = LocalLayoutDirection.current
    val state = rememberPullToRefreshState()
    val lazyListState = rememberLazyListState()
    var dropdownMenuExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val bottomSheetState = rememberModalBottomSheetState(true)
    var openBottomSheet by remember { mutableStateOf(false) }
    var viewReply by remember { mutableStateOf(false) }
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val shouldShowSortCard by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex > viewModel.itemSize - 1 }
    }
    val shouldShowTopCard by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex > 0 }
    }

    fun setReplyType(index: Int) {
        selected = index
        viewModel.listType = when (index) {
            0 -> "lastupdate_desc"
            1 -> "dateline_desc"
            2 -> "popular"
            else -> EMPTY_STRING
        }
        viewModel.fromFeedAuthor = if (index == 3) 1 else 0
    }

    val articleList = remember(key1 = viewModel.feedState) { viewModel.articleList }

    fun launchReply() {
        viewModel.onReply = true
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.systemBars
                    .only(
                        WindowInsetsSides.Top
                                + if (isCompat) WindowInsetsSides.Start else WindowInsetsSides.End
                    ),
                navigationIcon = {
                    BackButton {
                        viewModel.onDestroy()
                        if (!isCompat) {
                            viewModel.resetState()
                        }
                        onBackClick()
                    }
                },
                title = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .noRippleClickable {
                                scope.launch {
                                    lazyListState.scrollToItem(
                                        if (shouldShowSortCard || viewReply) {
                                            viewReply = false
                                            0
                                        } else {
                                            viewReply = true
                                            viewModel.itemSize
                                        }
                                    )
                                }
                            },
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        AnimatedVisibility(
                            visible = !shouldShowTopCard,
                            enter = fadeIn(animationSpec = spring(stiffness = StiffnessLow)),
                            exit = fadeOut(animationSpec = spring(stiffness = StiffnessLow)),
                        ) {
                            Text(text = viewModel.feedTypeName)
                        }
                        if (viewModel.feedState is LoadingState.Success) {
                            AnimatedVisibility(
                                visible = shouldShowTopCard,
                                enter = fadeIn(animationSpec = spring(stiffness = StiffnessLow)),
                                exit = fadeOut(animationSpec = spring(stiffness = StiffnessLow)),
                            ) {
                                FeedHeader(
                                    data = (viewModel.feedState as LoadingState.Success).response,
                                    onViewUser = onViewUser,
                                    isFeedContent = true,
                                    onReport = onReport,
                                    isFeedTop = true,
                                    onBlockUser = {},
                                )
                            }
                        }
                    }
                },
                actions = {
                    if (viewModel.feedState is LoadingState.Success) {
                        Box(Modifier.wrapContentSize(Alignment.TopEnd)) {
                            IconButton(onClick = { dropdownMenuExpanded = true }) {
                                Icon(
                                    Icons.Default.MoreVert,
                                    contentDescription = null
                                )
                            }
                            DropdownMenu(
                                expanded = dropdownMenuExpanded,
                                onDismissRequest = { dropdownMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Copy") },
                                    onClick = {
                                        dropdownMenuExpanded = false
                                        copyToClipboard(getShareText(ShareType.FEED, id))
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (viewModel.isFav) "UnFav" else "Fav") },
                                    onClick = {
                                        dropdownMenuExpanded = false
                                        viewModel.onFav()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (viewModel.isBlocked) "UnBlock" else "Block") },
                                    onClick = {
                                        dropdownMenuExpanded = false
                                        viewModel.blockUser()
                                    }
                                )
                                if (isLogin) {
                                    DropdownMenuItem(
                                        text = { Text("Report") },
                                        onClick = {
                                            dropdownMenuExpanded = false
                                            onReport(id, ReportType.FEED)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (isLogin && viewModel.feedState is LoadingState.Success) {
                AnimatedVisibility(
                    modifier = if (!isCompat) Modifier.navigationBarsPadding() else Modifier,
                    visible = lazyListState.isScrollingUp(),
                    enter = slideInVertically { it * 2 },
                    exit = slideOutVertically { it * 2 }
                ) {
                    FloatingActionButton(
                        onClick = {
                            viewModel.replyId = viewModel.id
                            viewModel.replyUid = viewModel.feedUid
                            viewModel.replyName = viewModel.feedUsername
                            viewModel.replyType = "feed"
                            launchReply()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Reply,
                            contentDescription = null
                        )
                    }
                }
            }
        },
    ) { paddingValues ->

        PullToRefreshBox(
            modifier = Modifier.padding(
                top = paddingValues.calculateTopPadding(),
                end = if (isCompat) 0.dp else paddingValues.calculateRightPadding(layoutDirection),
                start = if (isCompat) paddingValues.calculateLeftPadding(layoutDirection) else 0.dp
            ),
            state = state,
            isRefreshing = viewModel.isRefreshing,
            onRefresh = {
                viewModel.isPull = true
                viewModel.refresh()
            },
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
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding()),
                state = lazyListState
            ) {
                when (viewModel.feedState) {
                    LoadingState.Loading, LoadingState.Empty, is LoadingState.Error -> {
                        item(key = "feedState") {
                            Box(modifier = Modifier.fillParentMaxSize()) {
                                LoadingCard(
                                    modifier = Modifier.align(Alignment.Center),
                                    state = viewModel.feedState,
                                    onClick = if (viewModel.feedState is LoadingState.Loading) null
                                    else viewModel::refresh,
                                    isFeed = true,
                                )
                            }
                        }
                    }

                    is LoadingState.Success -> {
                        val response = (viewModel.feedState as LoadingState.Success).response
                        if (!articleList.isNullOrEmpty()) {
                            ArticleItem(
                                response = response,
                                articleList = articleList,
                                onOpenLink = onOpenLink,
                                onCopyText = onCopyText,
                                onLike = {
                                    if (isLogin) {
                                        viewModel.onLike(
                                            response.id.orEmpty(),
                                            response.userAction?.like ?: 0,
                                            LikeType.FEED
                                        )
                                    }
                                },
                                onViewUser = onViewUser,
                                onViewImage = onViewImage,
                            )
                        } else {
                            item(key = "header") {
                                FeedHeader(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    data = response,
                                    onViewUser = onViewUser,
                                    isFeedContent = true,
                                    isFeedTop = false,
                                )
                            }
                            item(key = "feed") {
                                FeedCard(
                                    isFeedContent = true,
                                    data = response,
                                    onViewUser = onViewUser,
                                    onViewFeed = onViewFeed,
                                    onOpenLink = onOpenLink,
                                    onCopyText = onCopyText,
                                    onReport = onReport,
                                    onLike = { id, like, likeType ->
                                        viewModel.onLike(id, like, likeType)
                                    },
                                    onDelete = { id, deleteType, frid ->
                                        viewModel.frid = frid
                                        viewModel.onDelete(id, deleteType)
                                    },
                                    onBlockUser = {},
                                    onViewImage = onViewImage,
                                )
                            }
                        }

                        item(key = "sort") {
                            FeedReplySortCard(
                                replyCount = viewModel.replyCount,
                                selected = selected,
                                updateSortReply = { index ->
                                    setReplyType(index)
                                    if (shouldShowSortCard)
                                        viewModel.isViewReply = true
                                    viewModel.refresh()
                                }
                            )
                            HorizontalDivider()
                        }

                        if (viewModel.listType == "lastupdate_desc") {
                            if (!response.topReplyRows.isNullOrEmpty()) {
                                response.topReplyRows?.getOrNull(0)?.let { reply ->
                                    item(key = "topReplyRows") {
                                        FeedReplyCard(
                                            data = reply,
                                            onViewUser = onViewUser,
                                            onShowTotalReply = { id, uid, frid ->
                                                openBottomSheet = true
                                                viewModel.replyId = id
                                                viewModel.replyUid = uid
                                                viewModel.frid = frid
                                                viewModel.fetchTotalReply()
                                            },
                                            onOpenLink = onOpenLink,
                                            onCopyText = onCopyText,
                                            onReport = onReport,
                                            onLike = { id, like, likeType ->
                                                viewModel.onLike(id, like, likeType)
                                            },
                                            onDelete = { id, deleteType, frid ->
                                                viewModel.frid = frid
                                                viewModel.onDelete(id, deleteType)
                                            },
                                            onBlockUser = { uid, frid ->
                                                viewModel.frid = frid
                                                viewModel.onBlockUser(uid)
                                            },
                                            onReply = { rid, uid, username, frid ->
                                                viewModel.replyId = rid
                                                viewModel.replyUid = uid
                                                viewModel.replyName = username
                                                viewModel.frid = frid
                                                viewModel.replyType = "reply"
                                                launchReply()
                                            },
                                            onViewImage = onViewImage,
                                        )
                                        HorizontalDivider()
                                    }
                                }
                            }

                            if (!response.replyMeRows.isNullOrEmpty()) {
                                response.replyMeRows?.getOrNull(0)?.let { reply ->
                                    item(key = "replyMeRows") {
                                        FeedReplyCard(
                                            data = reply,
                                            onViewUser = onViewUser,
                                            onShowTotalReply = { id, uid, frid ->
                                                openBottomSheet = true
                                                viewModel.replyId = id
                                                viewModel.replyUid = uid
                                                viewModel.frid = frid
                                                viewModel.fetchTotalReply()
                                            },
                                            onOpenLink = onOpenLink,
                                            onCopyText = onCopyText,
                                            onReport = onReport,
                                            onLike = { id, like, likeType ->
                                                viewModel.onLike(id, like, likeType)
                                            },
                                            onDelete = { id, deleteType, frid ->
                                                viewModel.frid = frid
                                                viewModel.onDelete(id, deleteType)
                                            },
                                            onBlockUser = { uid, frid ->
                                                viewModel.frid = frid
                                                viewModel.onBlockUser(uid)
                                            },
                                            onReply = { rid, uid, username, frid ->
                                                viewModel.replyId = rid
                                                viewModel.replyUid = uid
                                                viewModel.replyName = username
                                                viewModel.frid = frid
                                                viewModel.replyType = "reply"
                                                launchReply()
                                            },
                                            onViewImage = onViewImage,
                                        )
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                        if (viewModel.isViewReply) {
                            viewModel.isViewReply = false
                            scope.launch {
                                lazyListState.scrollToItem(viewModel.itemSize)
                            }
                        }
                    }
                }

                if (viewModel.feedState is LoadingState.Success) {

                    ItemCard(
                        loadingState = viewModel.loadingState,
                        loadMore = viewModel::loadMore,
                        isEnd = viewModel.isEnd,
                        onViewUser = onViewUser,
                        onViewFeed = onViewFeed,
                        onOpenLink = onOpenLink,
                        onCopyText = onCopyText,
                        onShowTotalReply = { id, uid, frid ->
                            openBottomSheet = true
                            viewModel.replyId = id
                            viewModel.replyUid = uid
                            viewModel.frid = frid
                            viewModel.fetchTotalReply()
                        },
                        onReport = onReport,
                        onLike = { id, like, likeType ->
                            viewModel.onLike(id, like, likeType)
                        },
                        onDelete = { id, deleteType, frid ->
                            viewModel.frid = frid
                            viewModel.onDelete(id, deleteType)
                        },
                        onBlockUser = { uid, frid ->
                            viewModel.frid = frid
                            viewModel.onBlockUser(uid)
                        },
                        onFollowUser = { uid, isFollow ->
                            viewModel.onFollowUser(uid, isFollow)
                        },
                        onReply = { rid, uid, username, frid ->
                            viewModel.replyId = rid
                            viewModel.replyUid = uid
                            viewModel.replyName = username
                            viewModel.frid = frid
                            viewModel.replyType = "reply"
                            launchReply()
                        },
                        onViewImage = onViewImage,
                    )

                    FooterCard(
                        footerState = viewModel.footerState,
                        loadMore = viewModel::loadMore,
                        isFeed = true
                    )

                }

            }
            if (shouldShowSortCard) {
                FeedReplySortCard(
                    replyCount = viewModel.replyCount,
                    selected = selected,
                    updateSortReply = { index ->
                        setReplyType(index)
                        viewModel.isViewReply = true
                        viewModel.refresh()
                    }
                )
            }

        }

    }

    fun resetBottomSheet() {
        openBottomSheet = false
        viewModel.resetReplyState()
    }

    if (openBottomSheet) {

        ModalBottomSheet(
            onDismissRequest = {
                resetBottomSheet()
            },
            sheetState = bottomSheetState,
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {

                ItemCard(
                    loadingState = viewModel.replyLoadingState,
                    loadMore = viewModel::loadMoreReply,
                    isEnd = viewModel.isEndReply,
                    onViewUser = { uid ->
                        resetBottomSheet()
                        onViewUser(uid)
                    },
                    onViewFeed = { id, isViewReply ->
                        resetBottomSheet()
                        onViewFeed(id, isViewReply)
                    },
                    onOpenLink = { url, title ->
                        resetBottomSheet()
                        onOpenLink(url, title)
                    },
                    onCopyText = {
                        copyToClipboard(htmlToString(it?.getAllLinkAndText.orEmpty()))
                    },
                    onReport = { id, type ->
                        resetBottomSheet()
                        onReport(id, type)
                    },
                    isTotalReply = true,
                    onLike = { id, like, likeType ->
                        viewModel.onLikeReply(id, like, likeType)
                    },
                    onDelete = { id, deleteType, _ ->
                        viewModel.onDeleteRely(id, deleteType)
                    },
                    onBlockUser = { uid, _ ->
                        viewModel.onBlockReplyUser(uid)
                    },
                    isReply2Reply = !viewModel.frid.isNullOrEmpty(),
                    onShowTotalReply = { id, uid, frid ->
                        viewModel.replyId = id
                        viewModel.replyUid = uid
                        viewModel.frid = frid
                        viewModel.resetReplyState()
                        viewModel.fetchTotalReply()
                    },
                    onReply = { rid, uid, username, _ ->
                        viewModel.isSheet = true
                        viewModel.replyId = rid
                        viewModel.replyUid = uid
                        viewModel.replyName = username
                        viewModel.frid = null
                        viewModel.replyType = "reply"
                        launchReply()
                    },
                    onViewImage = onViewImage,
                )

                FooterCard(
                    footerState = viewModel.replyFooterState,
                    loadMore = viewModel::loadMoreReply,
                    isFeed = true
                )
            }
        }
    }

    when {
        viewModel.onReply -> {
            CreateFeedDialog(
                title = "回复: ${viewModel.replyName}",
                onDismiss = {
                    viewModel.onReply = false
                },
                onPostCreateFeed = { message ->
                    viewModel.onPostReply(message)
                }
            )
        }
    }

    viewModel.captchaImg?.let {
        CaptchaDialog(
            image = it,
            onDismiss = {
                viewModel.resetCaptcha()
            },
            onValidateCaptcha = { captcha ->
                viewModel.onPostRequestValidate(captcha)
            }
        )
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