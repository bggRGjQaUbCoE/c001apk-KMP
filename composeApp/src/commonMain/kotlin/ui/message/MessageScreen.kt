package ui.message

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import ui.component.FooterCard
import ui.component.ItemCard
import ui.component.cards.MessageFFFCard
import ui.component.cards.MessageHeaderCard
import ui.component.cards.MessageListCard
import ui.component.cards.MessageWidgetCard
import ui.component.cards.backgroundList
import ui.component.cards.iconList
import ui.component.cards.titleList
import ui.notification.NoticeType
import ui.root.LocalUserPreferences
import util.ReportType

/**
 * Created by bggRGjQaUbCoE on 2024/6/2
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageScreen(
    childComponentContext: ComponentContext,
    refreshState: Boolean,
    resetRefreshState: () -> Unit,
    onLogin: () -> Unit,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onViewFFFList: (String?, String, String?, String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onViewNotice: (String) -> Unit,
    onViewHistory: (String) -> Unit,
    onViewImage: (List<String>, Int) -> Unit,
) {

    val layoutDirection = LocalLayoutDirection.current
    val prefs = LocalUserPreferences.current

    val viewModel = childComponentContext.instanceKeeper.getOrCreate {
        koinInject<MessageViewModel> {
            parametersOf("/v6/notification/list")
        }
    }

    LaunchedEffect(refreshState) {
        if (refreshState) {
            viewModel.refresh()
            resetRefreshState()
        }
    }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val state = rememberPullToRefreshState()

    LaunchedEffect(prefs.isLogin) {
        if (prefs.isLogin && viewModel.fffList.isEmpty()) {
            viewModel.refresh()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding())
        ) {
            MessageHeaderCard(
                modifier = Modifier.padding(
                    start = paddingValues.calculateLeftPadding(layoutDirection),
                ),
                isLogin = prefs.isLogin,
                userAvatar = prefs.userAvatar,
                userName = prefs.username,
                level = prefs.level,
                experience = prefs.experience,
                nextLevelExperience = prefs.nextLevelExperience,
                onLogin = onLogin,
                onLogout = {
                    showLogoutDialog = true
                },
                onViewUser = onViewUser
            )

            HorizontalDivider()

            PullToRefreshBox(
                modifier = Modifier.padding(
                    start = paddingValues.calculateLeftPadding(layoutDirection),
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
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item(key = "fff") {
                        MessageFFFCard(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            fffList = viewModel.fffList,
                            onViewFFFList = onViewFFFList,
                        )
                    }

                    item(key = "widget") {
                        MessageWidgetCard(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            onViewFFFList = onViewFFFList,
                            onViewHistory = onViewHistory,
                        )
                    }

                    items(5) { index ->
                        MessageListCard(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            background = backgroundList[index],
                            imageVector = iconList[index],
                            title = titleList[index],
                            count = viewModel.badgeList.getOrNull(index),
                            onViewNotice = {
                                viewModel.clearBadge(index)
                                onViewNotice(NoticeType.entries[index].name)
                            }
                        )
                    }

                    ItemCard(
                        loadingState = viewModel.loadingState,
                        loadMore = viewModel::loadMore,
                        isEnd = viewModel.isEnd,
                        onViewUser = onViewUser,
                        onViewFeed = onViewFeed,
                        onOpenLink = onOpenLink,
                        onCopyText = onCopyText,
                        onReport = onReport,
                        onBlockUser = { uid, _ ->
                            viewModel.onBlockUser(uid)
                        },
                        onDeleteNotice = { id ->
                            viewModel.deleteId = id
                            showDeleteDialog = true
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

        }

    }

    when {
        showLogoutDialog -> {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.onLogout()
                            showLogoutDialog = false
                        })
                    {
                        Text(text = "OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showLogoutDialog = false })
                    {
                        Text(text = "Cancel")
                    }
                },
                title = {
                    Text(text = "确定退出登录？", modifier = Modifier.fillMaxWidth())
                },
                /*text = {
                    Text(
                        """
                            uid = ${prefs.uid}
                            username = ${prefs.username}
                            token = ${prefs.token}
                        """.trimIndent()
                    )
                }*/
            )
        }

        showDeleteDialog -> {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.onPostDelete()
                            showDeleteDialog = false
                        })
                    {
                        Text(text = "OK")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showDeleteDialog = false })
                    {
                        Text(text = "Cancel")
                    }
                },
                title = {
                    Text(text = "确定删除此条消息？", modifier = Modifier.fillMaxWidth())
                }
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