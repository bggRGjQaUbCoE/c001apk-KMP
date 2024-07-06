package ui.carousel

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import kotlinx.coroutines.launch
import logic.model.TopicBean
import logic.state.LoadingState
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import ui.component.BackButton
import ui.component.CommonScreen
import ui.component.cards.LoadingCard
import util.ReportType
import util.decode

/**
 * Created by bggRGjQaUbCoE on 2024/6/11
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CarouselScreen(
    childComponentContext: ComponentContext,
    onBackClick: () -> Unit,
    url: String,
    title: String,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onViewImage: (List<String>, Int) -> Unit,
) {

    val viewModel = childComponentContext.instanceKeeper.getOrCreate(url + title) {
        koinInject<CarouselViewModel> {
            parametersOf(true, url.decode, title)
        }
    }

    val layoutDirection = LocalLayoutDirection.current
    val scope = rememberCoroutineScope()
    var refreshState by remember { mutableStateOf(false) }

    var pagerState: PagerState

    val toaster = rememberToasterState()
    Toaster(
        state = toaster,
        alignment = Alignment.BottomCenter
    )

    viewModel.toastText?.let {
        viewModel.resetToastText()
        toaster.show(it)
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
                        text = viewModel.pageTitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    ) { paddingValues ->

        Column(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
            when (viewModel.loadingState) {
                LoadingState.Loading, LoadingState.Empty, is LoadingState.Error -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LoadingCard(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 10.dp),
                            state = viewModel.loadingState,
                            onClick = if (viewModel.loadingState is LoadingState.Loading) null
                            else viewModel::loadMore
                        )
                    }
                }

                is LoadingState.Success -> {
                    val dataList =
                        (viewModel.loadingState as LoadingState.Success).response

                    val isIconTabLinkGridCard =
                        dataList.find { it.entityTemplate == "iconTabLinkGridCard" }
                    if (isIconTabLinkGridCard == null) {
                        HorizontalDivider()
                        CommonScreen(
                            viewModel = viewModel,
                            refreshState = null,
                            resetRefreshState = {},
                            paddingValues = PaddingValues(
                                start = paddingValues.calculateLeftPadding(layoutDirection)
                            ),
                            onViewUser = onViewUser,
                            onViewFeed = onViewFeed,
                            onOpenLink = onOpenLink,
                            onCopyText = onCopyText,
                            onReport = onReport,
                            onViewImage = onViewImage,
                        )
                    } else {
                        isIconTabLinkGridCard.entities?.map {
                            TopicBean(it.url.orEmpty(), it.title.orEmpty())
                        }?.let { tabList ->
                            pagerState = rememberPagerState(pageCount = { tabList.size })
                            SecondaryScrollableTabRow(
                                modifier = Modifier.padding(
                                    start = paddingValues.calculateLeftPadding(layoutDirection),
                                ),
                                selectedTabIndex = pagerState.currentPage,
                                indicator = {
                                    TabRowDefaults.SecondaryIndicator(
                                        Modifier
                                            .tabIndicatorOffset(
                                                it[pagerState.currentPage],
                                            )
                                            .clip(
                                                RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                                            )
                                    )
                                },
                                divider = {}
                            ) {
                                tabList.forEachIndexed { index, tab ->
                                    Tab(
                                        selected = pagerState.currentPage == index,
                                        onClick = {
                                            if (pagerState.currentPage == index) {
                                                refreshState = true
                                            }
                                            scope.launch { pagerState.animateScrollToPage(index) }
                                        },
                                        text = { Text(text = tab.title) }
                                    )
                                }
                            }

                            HorizontalDivider()

                            HorizontalPager(
                                state = pagerState,
                            ) { index ->
                                CarouselContentScreen(
                                    childComponentContext = childComponentContext,
                                    url = tabList[index].url,
                                    title = tabList[index].title,
                                    paddingValues = PaddingValues(
                                        start = paddingValues.calculateLeftPadding(layoutDirection)
                                    ),
                                    refreshState = refreshState,
                                    resetRefreshState = { refreshState = false },
                                    onViewUser = onViewUser,
                                    onViewFeed = onViewFeed,
                                    onOpenLink = onOpenLink,
                                    onCopyText = onCopyText,
                                    onReport = onReport,
                                    onViewImage = onViewImage,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}