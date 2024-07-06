package ui.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import constant.Constants.EMPTY_STRING
import copyToClipboard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import logic.state.LoadingState
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import ui.component.BackButton
import ui.component.cards.CardIndicator
import ui.component.cards.ChatLeftCard
import ui.component.cards.ChatRightCard
import ui.component.cards.ChatTimeCard
import ui.component.cards.LoadingCard
import ui.theme.cardBg
import util.DeviceUtil
import util.EmojiUtils
import util.EmojiUtils.coolBList
import util.EmojiUtils.emojiList
import util.ReportType

/**
 * Created by bggRGjQaUbCoE on 2024/6/19
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    childComponentContext: ComponentContext,
    onBackClick: () -> Unit,
    ukey: String,
    uid: String,
    username: String,
    onViewUser: (String) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onViewImage: (List<String>, Int) -> Unit,
) {

    val viewModel = childComponentContext.instanceKeeper.getOrCreate(key = ukey) {
        koinInject<ChatViewModel> {
            parametersOf(ukey)
        }
    }

    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    val lazyListState = rememberLazyListState()
    var clearText by remember { mutableStateOf(false) }
    val windowInsets = WindowInsets.navigationBars

    LaunchedEffect(key1 = viewModel.scroll) {
        if (viewModel.scroll) {
            clearText = true
            delay(150)
            lazyListState.scrollToItem(0)
            viewModel.reset()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                windowInsets = WindowInsets.systemBars
                    .only(WindowInsetsSides.Start + WindowInsetsSides.Top),
                navigationIcon = {
                    BackButton {
                        onBackClick()
                    }
                },
                title = { Text(text = username) },
                actions = {
                    Box {
                        IconButton(onClick = { dropdownMenuExpanded = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = dropdownMenuExpanded,
                            onDismissRequest = { dropdownMenuExpanded = false }) {
                            listOf("Check", "Block", "Report").forEachIndexed { index, menu ->
                                DropdownMenuItem(
                                    text = { Text(text = menu) },
                                    onClick = {
                                        dropdownMenuExpanded = false
                                        when (index) {
                                            0 -> {
                                                onViewUser(uid)
                                            }

                                            1 -> viewModel.onBlockUser(uid)
                                            2 -> {
                                                onReport(uid, ReportType.USER)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        contentWindowInsets = ScaffoldDefaults
            .contentWindowInsets
            .exclude(WindowInsets.navigationBars)
            .exclude(WindowInsets.ime),
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                )
                .windowInsetsPadding(windowInsets.only(WindowInsetsSides.Start))
        ) {
            HorizontalDivider()

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                reverseLayout = true,
                state = lazyListState,
            ) {
                when (viewModel.loadingState) {
                    LoadingState.Loading, LoadingState.Empty, is LoadingState.Error -> {
                        item(key = "loadingState") {
                            Box(modifier = Modifier.fillParentMaxSize()) {
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
                    }

                    is LoadingState.Success -> {
                        itemsIndexed(
                            items = (viewModel.loadingState as LoadingState.Success).response,
                            key = { _, item -> item.entityId + item.dateline },
                        ) { index, item ->
                            when (item.entityType) {
                                "message" -> when (item.fromuid) {
                                    DeviceUtil.uid ->
                                        ChatRightCard(
                                            data = item,
                                            onGetImageUrl = viewModel::onGetImageUrl,
                                            onLongClick = { id, msg, url ->
                                                viewModel.deleteId = id
                                                viewModel.message = msg
                                                viewModel.pic = url
                                                showDialog = true
                                            },
                                            onViewUser = onViewUser,
                                            onViewImage = onViewImage,
                                        )

                                    else ->
                                        ChatLeftCard(
                                            data = item,
                                            onGetImageUrl = viewModel::onGetImageUrl,
                                            onLongClick = { id, msg, url ->
                                                viewModel.deleteId = id
                                                viewModel.message = msg
                                                viewModel.pic = url
                                                showDialog = true
                                            },
                                            onViewUser = onViewUser,
                                            onViewImage = onViewImage,
                                        )
                                }

                                "messageExtra" -> ChatTimeCard(title = item.title.orEmpty())
                            }

                            if (index == (viewModel.loadingState as LoadingState.Success).response.lastIndex && !viewModel.isEnd) {
                                viewModel.loadMore()
                            }
                        }
                    }
                }
            }

            ChatBottom(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBg())
                    .imePadding()
                    .windowInsetsPadding(windowInsets.only(WindowInsetsSides.Start + WindowInsetsSides.Bottom)),
                onPickImage = {

                },
                onSendMessage = {
                    viewModel.onSendMessage(uid, it, EMPTY_STRING)
                },
                clearText = clearText,
                resetClearText = {
                    clearText = false
                },
                viewModel = viewModel,
            )
        }

    }

    when {
        viewModel.showUploadDialog -> {
            LoadingDialog()
        }

        showDialog -> {
            Dialog(onDismissRequest = { showDialog = false }) {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.elevatedCardColors()
                        .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "删除",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showDialog = false
                                    viewModel.onDeleteMsg()
                                }
                                .padding(horizontal = 24.dp, vertical = 14.dp),
                            style = MaterialTheme.typography.titleSmall
                        )
                        if (viewModel.pic.isEmpty()) {
                            Text(
                                text = "复制",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showDialog = false
                                        copyToClipboard(viewModel.message)
                                    }
                                    .padding(horizontal = 24.dp, vertical = 14.dp),
                                style = MaterialTheme.typography.titleSmall
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

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBottom(
    modifier: Modifier = Modifier,
    viewModel: ChatViewModel,
    onPickImage: () -> Unit,
    onSendMessage: (String) -> Unit,
    clearText: Boolean,
    resetClearText: () -> Unit,
) {
    val recentList by viewModel.recentEmojiData.collectAsStateWithLifecycle(initialValue = emptyList())
    var showEmojiPanel by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf(EMPTY_STRING) }
    var pagerState: PagerState
    val focusRequest = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try {
            focusRequest.requestFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    LaunchedEffect(clearText) {
        if (clearText) {
            textInput = EMPTY_STRING
            resetClearText()
        }
    }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable {
                        showEmojiPanel = !showEmojiPanel
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    imageVector = Icons.Outlined.EmojiEmotions,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            TextField(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .focusRequester(focusRequest),
                placeholder = {
                    Text("写私信...")
                },
                value = textInput,
                onValueChange = {
                    if (it.length <= 500) {
                        textInput = it
                    }
                },
                maxLines = 4,
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable {
                        if (textInput.isEmpty())
                            onPickImage()
                        else
                            onSendMessage(textInput)
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    imageVector =
                    if (textInput.isEmpty())
                        Icons.Outlined.AddPhotoAlternate
                    else
                        Icons.AutoMirrored.Default.Send,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
        if (showEmojiPanel) {
            pagerState = rememberPagerState(
                initialPage = if (recentList.isEmpty()) 1 else 0,
                pageCount = { 3 }
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
            ) { index ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    val pagerState1 = rememberPagerState {
                        when (index) {
                            0 -> 1
                            1 -> 4
                            2 -> 2
                            else -> 0
                        }
                    }
                    HorizontalPager(
                        state = pagerState1,
                        modifier = Modifier.fillMaxWidth()
                            .draggable(
                                orientation = Orientation.Horizontal,
                                state = rememberDraggableState { detail ->
                                    scope.launch {
                                        if (detail < 0) {
                                            pagerState1.animateScrollToPage(pagerState1.currentPage + 1)
                                        } else {
                                            pagerState1.animateScrollToPage(pagerState1.currentPage - 1)
                                        }
                                    }
                                })
                    ) { index0 ->
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(28) {
                                val emojiName = when (index) {
                                    0 -> recentList.getOrNull(it)?.data
                                    1 -> emojiList.getOrNull(index0)?.getOrNull(it)?.first
                                    2 -> coolBList.getOrNull(index0)?.getOrNull(it)?.first
                                    else -> null
                                }
                                val emojiRes = when (index) {
                                    0 -> emojiName?.let { name ->
                                        EmojiUtils.emojiMap.getValue(name)
                                    }

                                    1 -> emojiList.getOrNull(index0)?.getOrNull(it)?.second
                                    2 -> coolBList.getOrNull(index0)?.getOrNull(it)?.second
                                    else -> null
                                }
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable {
                                            /*emojiName?.let { name ->
                                                clickedEmoji = name
                                                if (pagerState.currentPage != 0) {
                                                    viewModel.updateRecentEmoji(
                                                        name,
                                                        recentList.size,
                                                        recentList.lastOrNull()?.data
                                                    )
                                                }
                                            }
                                            if (it == 27) clickedEmoji = "[c001apk]"*/
                                            emojiName?.let { name ->
                                                if ((textInput + name).length <= 500) {
                                                    textInput += name
                                                    if (pagerState.currentPage != 0) {
                                                        viewModel.updateRecentEmoji(
                                                            name,
                                                            recentList.size,
                                                            recentList.lastOrNull()?.data
                                                        )
                                                    }
                                                }
                                            }
                                            if (it == 27 && textInput.isNotEmpty()) {
                                                textInput =
                                                    textInput.substring(0, textInput.length - 1)
                                            }
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (emojiRes != null) {
                                        Image(
                                            painter = painterResource(emojiRes),
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    } else if (it == 27) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.Backspace,
                                            contentDescription = null,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                            }
                        }
                    }
                    CardIndicator(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .padding(top = 10.dp),
                        pagerState = pagerState1,
                        defColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                    )
                }
            }
            HorizontalDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                TextIndicator(
                    index = 0,
                    title = "最近",
                    pagerState = pagerState,
                )
                VerticalDivider()
                TextIndicator(
                    index = 1,
                    title = "默认",
                    pagerState = pagerState,
                )
                VerticalDivider()
                TextIndicator(
                    index = 2,
                    title = "酷币",
                    pagerState = pagerState,
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowScope.TextIndicator(
    modifier: Modifier = Modifier,
    index: Int,
    title: String,
    pagerState: PagerState,
) {
    val scope = rememberCoroutineScope()
    Text(
        text = title,
        modifier = modifier
            .weight(1f)
            .background(
                if (pagerState.currentPage == index)
                    MaterialTheme.colorScheme.primary
                else
                    Color.Transparent
            )
            .clickable {
                scope.launch {
                    pagerState.scrollToPage(index)
                }
            }
            .padding(vertical = 10.dp),
        color = if (pagerState.currentPage == index)
            MaterialTheme.colorScheme.onPrimary
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.titleSmall,
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun LoadingDialog() {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnClickOutside = false, dismissOnBackPress = false)
    ) {
        Surface(
            modifier = Modifier.size(100.dp), shape = RoundedCornerShape(20.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}