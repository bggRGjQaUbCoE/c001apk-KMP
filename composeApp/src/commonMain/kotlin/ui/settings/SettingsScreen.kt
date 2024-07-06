package ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Feed
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.AllInclusive
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DeveloperMode
import androidx.compose.material.icons.outlined.FormatColorFill
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ImageAspectRatio
import androidx.compose.material.icons.outlined.InvertColors
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import be.digitalia.compose.htmlconverter.HtmlStyle
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import c001apk_kmp.composeapp.BuildConfig
import c001apk_kmp.composeapp.generated.resources.Res
import c001apk_kmp.composeapp.generated.resources.app_name
import c001apk_kmp.composeapp.generated.resources.ic_launcher_round
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import constant.Constants.URL_SOURCE_CODE
import logic.datastore.FollowType
import logic.datastore.ThemeMode
import logic.datastore.ThemeType
import openInBrowser
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import ui.blacklist.BlackListType
import ui.component.settings.BasicListItem
import ui.component.settings.DropdownListItem
import ui.component.settings.SelectionItem
import ui.component.settings.StateDropdownListItem
import ui.component.settings.SwitchListItem
import ui.root.LocalUserPreferences
import util.DeviceUtil.randomMacAddress
import util.TokenDeviceUtils.encodeDevice

/**
 * Created by bggRGjQaUbCoE on 2024/5/30
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    childComponentContext: ComponentContext,
    onParamsClick: () -> Unit,
    onAboutClick: () -> Unit,
    onViewBlackList: (String) -> Unit,
) {

    val viewModel = childComponentContext.instanceKeeper.getOrCreate {
        koinInject<SettingsViewModel>()
    }

    val prefs = LocalUserPreferences.current
    val rememberScrollState = rememberScrollState()
    val layoutDirection = LocalLayoutDirection.current

    val imageQualityList by lazy { listOf("网络自适应", "原图", "普清") }
    val themeList by lazy { listOf("跟随系统", "总是开启", "总是关闭") }
    val followList by lazy { listOf("全部", "好友", "话题", "数码", "应用") }

    var dropdownMenuExpanded by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSZLMIDDialog by remember { mutableStateOf(false) }
    var showFontScaleDialog by remember { mutableStateOf(false) }
    var showContentScaleDialog by remember { mutableStateOf(false) }
    var showImageQualityDialog by remember { mutableStateOf(false) }
    var showThemeTypeDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Settings") },
                actions = {
                    Box(Modifier.wrapContentSize(Alignment.TopEnd)) {
                        IconButton(onClick = { dropdownMenuExpanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = Icons.Default.MoreVert.name
                            )
                        }

                        DropdownMenu(
                            expanded = dropdownMenuExpanded,
                            onDismissRequest = { dropdownMenuExpanded = false }
                        ) {
                            listOf("Feedback", "About").forEachIndexed { index, menu ->
                                DropdownMenuItem(
                                    text = { Text(menu) },
                                    onClick = {
                                        dropdownMenuExpanded = false
                                        when (index) {
                                            0 -> openInBrowser("$URL_SOURCE_CODE/issues")

                                            1 -> showAboutDialog = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateLeftPadding(layoutDirection),
                    end = paddingValues.calculateRightPadding(layoutDirection)
                )
                .verticalScroll(rememberScrollState)
        ) {

            BasicListItem(leadingText = stringResource(resource = Res.string.app_name))
            BasicListItem(
                leadingImageVector = Icons.Outlined.Smartphone,
                headlineText = "数字联盟ID",
                supportingText = prefs.szlmId.ifEmpty { null }
            ) {
                showSZLMIDDialog = true
            }
            BasicListItem(
                leadingImageVector = Icons.Outlined.DeveloperMode,
                headlineText = "机型参数",
            ) {
                onParamsClick()
            }

            BasicListItem(leadingText = "主题")
            /*if (isAndroid()) {
                SwitchListItem(
                    value = prefs.materialYou,
                    leadingImageVector = Icons.Outlined.Palette,
                    headlineText = "系统主题色",
                ) {
                    viewModel.setMaterialYou(it)
                }
            }*/
            StateDropdownListItem(
                isEnable = true,
                leadingImageVector = Icons.Outlined.FormatColorFill,
                headlineText = "主题颜色",
                value = prefs.themeType.name,
                selections = ThemeType.entries.map {
                    SelectionItem(it.name, it.name)
                },
                onValueChanged = { index, _ ->
                    if (index == 19)
                        showThemeTypeDialog = true
                    viewModel.setThemeType(index)
                }
            )
            DropdownListItem(
                leadingImageVector = Icons.Outlined.DarkMode,
                headlineText = "深色主题",
                value = prefs.themeMode.name,
                selections = (0..2).map {
                    SelectionItem(
                        themeList[it],
                        ThemeMode.entries[it].name
                    )
                }/*ThemeMode.entries.map {
                    SelectionItem(it.name, it.name)
                }*/,
                onValueChanged = { index, _ ->
                    viewModel.setThemeMode(index)
                }
            )
            SwitchListItem(
                value = prefs.pureBlack,
                leadingImageVector = Icons.Outlined.InvertColors,
                headlineText = "纯黑主题",
            ) {
                viewModel.setPureBlack(it)
            }

            BasicListItem(leadingText = "显示")
            BasicListItem(
                leadingImageVector = Icons.Outlined.Block,
                headlineText = "用户黑名单",
            ) {
                onViewBlackList(BlackListType.USER.name)
            }
            BasicListItem(
                leadingImageVector = Icons.Outlined.Block,
                headlineText = "话题黑名单",
            ) {
                onViewBlackList(BlackListType.TOPIC.name)
            }
            BasicListItem(
                leadingImageVector = Icons.Outlined.TextFields,
                headlineText = "字体大小",
                supportingText = "${String.format("%.2f", prefs.fontScale)}x"
            ) {
                showFontScaleDialog = true
            }
            BasicListItem(
                leadingImageVector = Icons.Outlined.ImageAspectRatio,
                headlineText = "内容大小",
                supportingText = "${String.format("%.2f", prefs.contentScale)}x"
            ) {
                showContentScaleDialog = true
            }
            DropdownListItem(
                leadingImageVector = Icons.Outlined.AddCircleOutline,
                headlineText = "关注分组",
                value = prefs.followType.name,
                selections = followList.mapIndexed { index, label ->
                    SelectionItem(label, FollowType.entries[index].name)
                },
                onValueChanged = { index, _ ->
                    viewModel.setFollowType(index)
                }
            )
            /*DropdownListItem(
                leadingImageVector = Icons.Outlined.Image,
                headlineText = "图片画质",
                value = prefs.imageQuality,
                selections = imageQualityList.mapIndexed { index, label ->
                    SelectionItem(label, index)
                },
                onValueChanged = { index, _ ->
                    viewModel.setImageQuality(index)
                }
            )*/
            /*BasicListItem(
                leadingImageVector = Icons.Outlined.Image,
                headlineText = "Image Quality",
                supportingText = imageQualityList[userPreferences.imageQuality]
            ) {
                showImageQualityDialog = true
            }*/
            /*SwitchListItem(
                value = prefs.imageFilter,
                leadingImageVector = Icons.Outlined.PhotoLibrary,
                headlineText = "压暗图片",
            ) {
                viewModel.setImageFilter(it)
            }*/
            /*SwitchListItem(
                value = prefs.openInBrowser,
                leadingImageVector = Icons.Outlined.TravelExplore,
                headlineText = "使用外部浏览器打开链接",
            ) {
                viewModel.setOpenInBrowser(it)
            }*/
            SwitchListItem(
                value = prefs.showSquare,
                leadingImageVector = Icons.AutoMirrored.Outlined.Feed,
                headlineText = "头条显示广场",
            ) {
                viewModel.setShowSquare(it)
            }
            SwitchListItem(
                value = prefs.recordHistory,
                leadingImageVector = Icons.Outlined.History,
                headlineText = "记录浏览历史",
            ) {
                viewModel.setRecordHistory(it)
            }
            /*SwitchListItem(
                value = prefs.showEmoji,
                leadingImageVector = Icons.Outlined.EmojiEmotions,
                headlineText = "显示表情",
            ) {
                viewModel.setShowEmoji(it)
            }*/
            /*SwitchListItem(
                value = prefs.checkUpdate,
                leadingImageVector = Icons.Outlined.SystemUpdate,
                headlineText = "检查更新",
            ) {
                viewModel.setCheckUpdate(it)
            }*/
            /*SwitchListItem(
                value = prefs.checkCount,
                leadingImageVector = Icons.Outlined.Notifications,
                headlineText = "检查通知",
            ) {
                viewModel.setCheckCount(it)
            }*/

            BasicListItem(leadingText = "其他")
            BasicListItem(
                leadingImageVector = Icons.Outlined.AllInclusive,
                headlineText = "关于",
                supportingText = "${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})"
            ) {
                onAboutClick()
            }
            /*BasicListItem(
                leadingImageVector = Icons.Outlined.CleaningServices,
                headlineText = "清理缓存",
                supportingText = cacheSize
            ) {
                cacheSize = getTotalCacheSize(context)
                showCleanCacheDialog = true
            }*/

        }

        when {
            showAboutDialog -> {
                AboutDialog {
                    showAboutDialog = false
                }
            }

            showImageQualityDialog -> {
                ListItemDialog(
                    title = "Image Quality",
                    list = imageQualityList,
                    selected = prefs.imageQuality,
                    onDismiss = {
                        showImageQualityDialog = false
                    },
                    setData = {
                        showImageQualityDialog = false
                        viewModel.setImageQuality(it)
                    }
                )
            }

            showThemeTypeDialog -> {
                EditTextDialog(
                    hint = "6650A4",
                    maxLength = 6,
                    data = prefs.seedColor,
                    title = "Custom Theme Color",
                    onDismiss = {
                        showThemeTypeDialog = false
                    },
                    setData = {
                        viewModel.setSeedColor(it)
                    }
                )
            }

            showSZLMIDDialog -> {
                EditTextDialog(
                    data = prefs.szlmId,
                    title = "SZLM ID",
                    onDismiss = {
                        showSZLMIDDialog = false
                    },
                    setData = {
                        viewModel.setSZLMId(it)
                        viewModel.setXAppDevice(
                            encodeDevice("$it; ; ; ${randomMacAddress()}; ${prefs.manufacturer}; ${prefs.brand}; ${prefs.model}; ${prefs.buildNumber}; null")
                        )
                    }
                )
            }

            showFontScaleDialog -> {
                SliderDialog(
                    data = prefs.fontScale,
                    title = "Font Scale",
                    hint = "Font Size",
                    onDismiss = {
                        showFontScaleDialog = false
                    },
                    setData = {
                        viewModel.setFontScale(it)
                    }
                )
            }

            showContentScaleDialog -> {
                SliderDialog(
                    data = prefs.contentScale,
                    title = "Content Scale",
                    hint = "Content Size",
                    onDismiss = {
                        showContentScaleDialog = false
                    },
                    setData = {
                        viewModel.setContentScale(it)
                    }
                )
            }

            /*showCleanCacheDialog -> {
                CleanCacheDialog(
                    cacheSize = cacheSize,
                    onDismiss = {
                        showCleanCacheDialog = false
                    },
                    onClean = {
                        clearAllCache(context)
                        cacheSize = "0 B"
                    }
                )
            }*/

        }
    }

}

@Composable
fun CleanCacheDialog(
    cacheSize: String,
    onDismiss: () -> Unit,
    onClean: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }) {
                Text(text = "Cancel")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onClean()
                }
            ) {
                Text(text = "OK")
            }
        },
        text = {
            Text(text = "当前缓存大小: $cacheSize")
        },
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "清理缓存",
                textAlign = TextAlign.Center
            )
        }
    )
}

@Composable
fun ListItemDialog(
    title: String,
    list: List<String>,
    selected: Int,
    onDismiss: () -> Unit,
    setData: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {},
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                textAlign = TextAlign.Center
            )
        },
        text = {
            LazyColumn {
                itemsIndexed(list) { index, title ->
                    ListItemDialogItem(title, selected == index) {
                        setData(index)
                    }
                }
            }
        },
    )
}

@Composable
fun ListItemDialogItem(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.clickable { onClick() }
    ) {
        RadioButton(selected = selected, onClick = { onClick() })
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterVertically),
            text = title,
        )
    }
}

@Composable
fun EditTextDialog(
    data: String,
    title: String,
    onDismiss: () -> Unit,
    setData: (String) -> Unit,
    hint: String? = null,
    maxLength: Int? = null,
) {
    var text by remember {
        mutableStateOf(
            TextFieldValue(
                text = data,
                selection = TextRange(data.length)
            )
        )
    }
    val focusRequest = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try {
            focusRequest.requestFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    AlertDialog(
        onDismissRequest = { onDismiss() },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }) {
                Text(text = "Cancel")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    setData(text.text)
                }) {
                Text(text = "OK")
            }
        },
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                textAlign = TextAlign.Center
            )
        },
        text = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequest),
                value = text,
                onValueChange = {
                    if (maxLength != null) {
                        if (it.text.matches(Regex("^[0-9a-fA-F]{0,6}$")))
                            text = it
                    } else {
                        text = it
                    }
                },
                placeholder = {
                    hint?.let {
                        Text(text = it)
                    }
                }
            )
        },
    )
}

@Composable
fun SliderDialog(
    data: Float,
    title: String,
    hint: String,
    onDismiss: () -> Unit,
    setData: (Float) -> Unit
) {
    var progress by remember { mutableFloatStateOf(data) }
    AlertDialog(
        onDismissRequest = {
            onDismiss()
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    setData(1.0f)
                }) {
                Text(text = "Reset")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    setData(progress)
                }) {
                Text(text = "OK")
            }
        },
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                Modifier.fillMaxWidth()
            ) {
                Slider(
                    value = progress,
                    onValueChange = {
                        progress = it
                    },
                    valueRange = 0.8f..1.3f
                )
                Text(
                    text = "$hint: ${String.format("%.2f", progress)}",
                    modifier = Modifier.fillMaxWidth(),
                    fontSize = 15.sp * progress,
                    textAlign = TextAlign.Center
                )
            }
        }
    )
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = { onDismiss() }) {
        ElevatedCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.elevatedCardColors()
                .copy(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_launcher_round),
                    contentDescription = "icon",
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {

                    Text(
                        stringResource(resource = Res.string.app_name),
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${BuildConfig.VERSION_NAME}(${BuildConfig.VERSION_CODE})",
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val html = "View source code at <b><a href=\"${URL_SOURCE_CODE}\">GitHub</a></b>"
                    val primary = MaterialTheme.colorScheme.primary
                    val convertedText = remember(html) {
                        htmlToAnnotatedString(
                            html,
                            style = HtmlStyle(linkSpanStyle = SpanStyle(color = primary))
                        )
                    }
                    ClickableText(
                        text = convertedText,
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { position ->
                            convertedText
                                .getUrlAnnotations(position, position)
                                .firstOrNull()?.let { range ->
                                    openInBrowser(range.item.url)
                                }
                        }
                    )
                }
            }
        }
    }
}