package ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import ui.component.SlideTransition
import ui.home.HomeScreen
import ui.message.MessageScreen
import ui.settings.SettingsScreen
import util.ReportType

/**
 * Created by bggRGjQaUbCoE on 2024/7/4
 */
@Composable
fun MainScreen(
    childComponentContext: ComponentContext,
    isCompat: Boolean,
    onParamsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onSearch: (String?, String?, String?) -> Unit,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onLogin: () -> Unit,
    onViewFFFList: (String?, String, String?, String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onViewNotice: (String) -> Unit,
    onViewBlackList: (String) -> Unit,
    onViewHistory: (String) -> Unit,
    onViewImage: (List<String>, Int) -> Unit,
) {

    val viewModel = childComponentContext.instanceKeeper.getOrCreate {
        koinInject<MainViewModel>()
    }

    val screens = listOf(
        Router.HOME,
        Router.MESSAGE,
        Router.SETTINGS
    )

    var selectIndex by rememberSaveable { mutableIntStateOf(0) }
    val savableStateHolder = rememberSaveableStateHolder()
    var refreshState by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (isCompat) {
                NavigationBar {
                    screens.forEachIndexed { index, screen ->
                        NavigationBarItem(
                            icon = {
                                BadgedBox(
                                    badge = {
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = if (index == 1) viewModel.badge > 0
                                            else false,
                                            enter = scaleIn(animationSpec = tween(250)),
                                            exit = scaleOut(animationSpec = tween(250))
                                        ) {
                                            Badge(
                                                modifier = Modifier
                                                    .padding(start = 15.dp, bottom = 10.dp)
                                            ) {
                                                Text(text = viewModel.badge.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector =
                                        if (selectIndex == screens.indexOf(screen)) {
                                            screen.selectedIcon!!
                                        } else {
                                            screen.unselectedIcon!!
                                        },
                                        contentDescription = null
                                    )
                                }
                            },
                            label = { Text(text = stringResource(resource = screen.stringRes!!)) },
                            selected = selectIndex == screens.indexOf(screen),
                            onClick = {
                                if (selectIndex == 0 && index == 0) {
                                    refreshState = true
                                } else if (index == 1 && viewModel.badge != 0) {
                                    viewModel.resetBadge()
                                }
                                selectIndex = index
                            },
                            alwaysShowLabel = false
                        )
                    }
                }
            }
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.exclude(WindowInsets.statusBars)
    ) { paddingValues ->
        Row(modifier = Modifier.fillMaxSize()) {
            if (!isCompat) {
                NavigationRail {
                    screens.forEachIndexed { index, screen ->
                        NavigationRailItem(
                            icon = {
                                BadgedBox(
                                    badge = {
                                        androidx.compose.animation.AnimatedVisibility(
                                            visible = if (index == 1) viewModel.badge > 0
                                            else false,
                                            enter = scaleIn(animationSpec = tween(250)),
                                            exit = scaleOut(animationSpec = tween(250))
                                        ) {
                                            Badge(
                                                modifier = Modifier
                                                    .padding(start = 15.dp, bottom = 10.dp)
                                            ) {
                                                Text(text = viewModel.badge.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector =
                                        if (selectIndex == screens.indexOf(screen)) {
                                            screen.selectedIcon!!
                                        } else {
                                            screen.unselectedIcon!!
                                        },
                                        contentDescription = null
                                    )
                                }
                            },
                            label = { Text(text = stringResource(resource = screen.stringRes!!)) },
                            selected = selectIndex == screens.indexOf(screen),
                            onClick = {
                                if ((selectIndex == 0 && index == 0) || (selectIndex == 1 && index == 1)) {
                                    refreshState = true
                                } else if (index == 1 && viewModel.badge != 0) {
                                    viewModel.resetBadge()
                                }
                                selectIndex = index
                            },
                            alwaysShowLabel = false
                        )

                    }
                }
            }
            AnimatedContent(
                modifier = Modifier.weight(1f).fillMaxSize().padding(paddingValues),
                label = "home-content",
                targetState = selectIndex,
                transitionSpec = {
                    SlideTransition.slideLeft.enterTransition()
                        .togetherWith(SlideTransition.slideLeft.exitTransition())
                },
            ) { page ->
                savableStateHolder.SaveableStateProvider(
                    key = page,
                    content = {
                        when (page) {
                            0 -> HomeScreen(
                                childComponentContext = childComponentContext,
                                refreshState = refreshState,
                                onRefresh = {
                                    refreshState = true
                                },
                                resetRefreshState = {
                                    refreshState = false
                                },
                                onViewUser = onViewUser,
                                onViewFeed = onViewFeed,
                                onSearch = onSearch,
                                onOpenLink = onOpenLink,
                                onCopyText = onCopyText,
                                onReport = onReport,
                                onViewImage = onViewImage,
                            )

                            1 -> MessageScreen(
                                childComponentContext = childComponentContext,
                                refreshState = refreshState,
                                resetRefreshState = {
                                    refreshState = false
                                },
                                onLogin = onLogin,
                                onViewUser = onViewUser,
                                onViewFeed = onViewFeed,
                                onOpenLink = onOpenLink,
                                onCopyText = onCopyText,
                                onViewFFFList = onViewFFFList,
                                onReport = onReport,
                                onViewNotice = onViewNotice,
                                onViewHistory = onViewHistory,
                                onViewImage = onViewImage,
                            )

                            2 -> SettingsScreen(
                                childComponentContext = childComponentContext,
                                onParamsClick = onParamsClick,
                                onAboutClick = onAboutClick,
                                onViewBlackList = onViewBlackList,
                            )
                        }
                    }
                )
            }
        }
    }

    val toaster = rememberToasterState()
    Toaster(
        state = toaster,
        alignment = Alignment.BottomCenter
    )

}
