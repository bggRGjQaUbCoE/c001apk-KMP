package ui.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import constant.Constants.EMPTY_STRING
import copyToClipboard
import logic.state.LoadingState
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import ui.component.BackButton
import ui.component.FooterCard
import ui.component.ItemCard
import ui.component.cards.LoadingCard
import ui.component.cards.UserInfoCard
import util.DateUtils.timeStamp2Date
import util.DeviceUtil.isLogin
import util.ReportType
import util.ShareType
import util.getShareText

/**
 * Created by bggRGjQaUbCoE on 2024/6/4
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserScreen(
    childComponentContext: ComponentContext,
    uid: String,
    onBackClick: () -> Unit,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onSearch: (String, String, String) -> Unit,
    onViewFFFList: (String?, String, String?, String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onPMUser: (String, String) -> Unit,
    onViewImage: (List<String>, Int) -> Unit,
) {

    val layoutDirection = LocalLayoutDirection.current

    val state = rememberPullToRefreshState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showUserInfoDialog by remember { mutableStateOf(false) }
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()
    val firstVisibleItemIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }

    val viewModel = childComponentContext.instanceKeeper.getOrCreate(key = uid) {
        koinInject<UserViewModel> {
            parametersOf(uid)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.systemBars
                    .only(WindowInsetsSides.Start + WindowInsetsSides.Top),
                navigationIcon = {
                    BackButton { onBackClick() }
                },
                title = {
                    Text(
                        text = if (firstVisibleItemIndex > 0)
                            (viewModel.userState as? LoadingState.Success)?.response?.username
                                ?: uid
                        else EMPTY_STRING,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    if (viewModel.userState is LoadingState.Success) {
                        Row(Modifier.wrapContentSize(Alignment.TopEnd)) {
                            IconButton(
                                onClick = {
                                    onSearch(viewModel.username, "user", viewModel.uid)
                                }
                            ) {
                                Icon(Icons.Default.Search, contentDescription = null)
                            }
                            Box {
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
                                    listOf("Copy", "User Info")
                                        .forEachIndexed { index, menu ->
                                            DropdownMenuItem(
                                                text = { Text(menu) },
                                                onClick = {
                                                    dropdownMenuExpanded = false
                                                    when (index) {
                                                        0 -> copyToClipboard(
                                                            getShareText(ShareType.USER, uid)
                                                        )

                                                        1 -> showUserInfoDialog = true
                                                    }
                                                }
                                            )
                                        }
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (viewModel.isBlocked) "UnBlock" else "Block"
                                            )
                                        },
                                        onClick = {
                                            dropdownMenuExpanded = false
                                            viewModel.onBlockUser(viewModel.uid)
                                        }
                                    )
                                    if (isLogin) {
                                        DropdownMenuItem(
                                            text = { Text("Report") },
                                            onClick = {
                                                dropdownMenuExpanded = false
                                                onReport(uid, ReportType.USER)
                                            }
                                        )
                                    }
                                }
                            }

                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->

        PullToRefreshBox(
            modifier = Modifier.padding(
                start = paddingValues.calculateLeftPadding(layoutDirection),
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
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(bottom = 10.dp + paddingValues.calculateBottomPadding()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                state = lazyListState
            ) {

                when (viewModel.userState) {
                    LoadingState.Loading, LoadingState.Empty, is LoadingState.Error -> {
                        item(key = "userState") {
                            Box(modifier = Modifier.fillParentMaxSize()) {
                                LoadingCard(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(horizontal = 10.dp),
                                    state = viewModel.userState,
                                    onClick = if (viewModel.userState is LoadingState.Loading) null
                                    else viewModel::refresh
                                )
                            }
                        }
                    }

                    is LoadingState.Success -> {
                        item(key = "userInfo") {
                            UserInfoCard(
                                data = (viewModel.userState as LoadingState.Success).response,
                                onFollow = { uid, isFollow ->
                                    viewModel.onFollowUser(uid, isFollow)
                                },
                                onPMUser = onPMUser,
                                onViewFFFList = onViewFFFList,
                                onViewImage = onViewImage,
                            )
                        }
                    }
                }

                if (viewModel.userState is LoadingState.Success) {

                    ItemCard(
                        loadingState = viewModel.loadingState,
                        loadMore = viewModel::loadMore,
                        isEnd = viewModel.isEnd,
                        onViewUser = { uid ->
                            if (uid != viewModel.uid) {
                                onViewUser(uid)
                            }
                        },
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
                        onViewImage = onViewImage,
                    )

                    FooterCard(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        footerState = viewModel.footerState,
                        loadMore = viewModel::loadMore,
                    )
                }

            }
        }


    }

    when {
        showUserInfoDialog -> {
            val data = (viewModel.userState as LoadingState.Success).response
            AlertDialog(
                onDismissRequest = { showUserInfoDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = { showUserInfoDialog = false }) {
                        Text(text = "OK")
                    }
                },
                title = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = data.username.orEmpty(),
                    )
                },
                text = {
                    Text(
                        text = """
                                uid: ${data.uid}
                                
                                等级: Lv.${data.level}
                                
                                性别: ${if (data.gender == 0) "女" else if (data.gender == 1) "男" else "未知"}
                                
                                注册时长: ${((System.currentTimeMillis() / 1000 - (data.regdate ?: 0)) / 24 / 3600)} 天
                                
                                注册时间: ${timeStamp2Date(data.regdate ?: 0)}
                            """.trimIndent()
                    )
                },
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