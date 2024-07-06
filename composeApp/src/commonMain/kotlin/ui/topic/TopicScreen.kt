package ui.topic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import constant.Constants.EMPTY_STRING
import kotlinx.coroutines.launch
import logic.state.LoadingState
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import ui.component.BackButton
import ui.component.CaptchaDialog
import ui.component.CreateFeedDialog
import ui.component.cards.LoadingCard
import util.DeviceUtil.isLogin
import util.ReportType

/**
 * Created by bggRGjQaUbCoE on 2024/6/9
 */

enum class ProductSortType {
    REPLY, HOT, DATELINE
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TopicScreen(
    childComponentContext: ComponentContext,
    onBackClick: () -> Unit,
    tag: String?,
    id: String?,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onSearch: (String, String, String) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onViewImage: (List<String>, Int) -> Unit,
) {

    val viewModel = childComponentContext.instanceKeeper.getOrCreate(key = tag + id) {
        koinInject<TopicViewModel> {
            parametersOf(
                when {
                    !tag.isNullOrEmpty() -> "/v6/topic/newTagDetail"
                    !id.isNullOrEmpty() -> "/v6/product/detail"
                    else -> throw IllegalArgumentException("empty param")
                },
                tag,
                id,
            )
        }
    }

    val layoutDirection = LocalLayoutDirection.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }
    var pagerState: PagerState = rememberPagerState(pageCount = { 0 })
    val scope = rememberCoroutineScope()
    var refreshState by remember { mutableStateOf(false) }
    var sortType by rememberSaveable { mutableStateOf(ProductSortType.REPLY) }
    var isScrollingUp by remember { mutableStateOf(false) }

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
                        text = (viewModel.topicState as? LoadingState.Success)?.response?.title
                            ?: EMPTY_STRING,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    if (viewModel.topicState is LoadingState.Success) {
                        Row(Modifier.wrapContentSize(Alignment.TopEnd)) {
                            IconButton(
                                onClick = {
                                    onSearch(
                                        viewModel.title,
                                        if (viewModel.entityType == "topic") "tag" else "product_phone",
                                        if (viewModel.entityType == "topic") viewModel.title else viewModel.id.orEmpty()
                                    )
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
                                    if (viewModel.entityType == "product"
                                        && viewModel.tabList?.getOrNull(pagerState.currentPage)?.title == "讨论"
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Sort") },
                                            onClick = {
                                                dropdownMenuExpanded = false
                                                sortMenuExpanded = true
                                            },
                                            trailingIcon = {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                                                    contentDescription = null
                                                )
                                            }
                                        )
                                    }
                                    if (isLogin) {
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    if (viewModel.isFollowed) "UnFollow"
                                                    else "Follow"
                                                )
                                            },
                                            onClick = {
                                                dropdownMenuExpanded = false
                                                if (viewModel.entityType == "topic")
                                                    viewModel.onGetFollow()
                                                else
                                                    viewModel.onPostFollow()
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (viewModel.isBlocked) "UnBlock"
                                                else "Block"
                                            )
                                        },
                                        onClick = {
                                            dropdownMenuExpanded = false
                                            viewModel.blockTopic()
                                        }
                                    )
                                }
                                DropdownMenu(
                                    expanded = sortMenuExpanded,
                                    onDismissRequest = { sortMenuExpanded = false }
                                ) {
                                    ProductSortType.entries.forEach { sort ->
                                        Row(
                                            modifier = Modifier
                                                .clickable {
                                                    sortMenuExpanded = false
                                                    sortType = sort
                                                },
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = sort == sortType,
                                                onClick = {
                                                    sortMenuExpanded = false
                                                    sortType = sort
                                                }
                                            )
                                            Text(
                                                text = sort.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(end = 16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            if (isLogin) {
                AnimatedVisibility(
                    visible = isScrollingUp,
                    enter = slideInVertically { it * 2 },
                    exit = slideOutVertically { it * 2 }
                ) {
                    FloatingActionButton(
                        onClick = {
                            viewModel.onCreateFeed = true
                            /*val intent = Intent(context, ReplyActivity::class.java)
                            intent.putExtra("type", "createFeed")
                            intent.putExtra(
                                "targetType",
                                if (viewModel.entityType == "topic") "tag" else "product_phone"
                            )
                            intent.putExtra("targetId", viewModel.id)
                            if (viewModel.entityType == "topic")
                                intent.putExtra("title", viewModel.title)
                            val animationBundle = ActivityOptionsCompat.makeCustomAnimation(
                                context,
                                R.anim.anim_bottom_sheet_slide_up,
                                R.anim.anim_bottom_sheet_slide_down
                            ).toBundle()
                            ContextCompat.startActivity(context, intent, animationBundle)*/
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    ) { paddingValues ->

        Column(modifier = Modifier.padding(top = paddingValues.calculateTopPadding())) {
            when (viewModel.topicState) {
                LoadingState.Loading, LoadingState.Empty, is LoadingState.Error -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        LoadingCard(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 10.dp),
                            state = viewModel.topicState,
                            onClick = if (viewModel.topicState is LoadingState.Loading) null
                            else viewModel::refresh
                        )
                    }
                }

                is LoadingState.Success -> {

                    viewModel.tabList?.let { tabList ->
                        val initialPage =
                            with(tabList.map { it.pageName }.indexOf(viewModel.selectedTab)) {
                                if (this == -1) 0 else this
                            }

                        pagerState = rememberPagerState(
                            initialPage = if (initialPage == -1) 0 else initialPage,
                            pageCount = { tabList.size }
                        )

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
                                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
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
                                    text = { Text(text = tab.title.orEmpty()) }
                                )
                            }
                        }

                        HorizontalDivider()

                        HorizontalPager(
                            state = pagerState
                        ) { index ->
                            TopicContentScreen(
                                childComponentContext = childComponentContext,
                                refreshState = refreshState,
                                resetRefreshState = {
                                    refreshState = false
                                },
                                paddingValues = PaddingValues(
                                    start = paddingValues.calculateLeftPadding(layoutDirection)
                                ),
                                entityType = viewModel.entityType,
                                id = viewModel.id,
                                url = tabList[index].url.orEmpty(),
                                title = tabList[index].title.orEmpty(),
                                sortType = sortType,
                                onViewUser = onViewUser,
                                onViewFeed = onViewFeed,
                                onOpenLink = onOpenLink,
                                onCopyText = onCopyText,
                                onReport = onReport,
                                isScrollingUp = {
                                    isScrollingUp = it
                                },
                                onViewImage = onViewImage,
                            )
                        }

                    }

                }
            }
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

    when {
        viewModel.onCreateFeed -> {
            CreateFeedDialog(
                prefix = if (viewModel.entityType == "topic") "#${viewModel.title}# " else null,
                onDismiss = {
                    viewModel.onCreateFeed = false
                },
                onPostCreateFeed = { message ->
                    viewModel.onPostCreateFeed(
                        targetType = if (viewModel.entityType == "topic") "tag" else "product_phone",
                        targetId = id,
                        message = message
                    )
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

}