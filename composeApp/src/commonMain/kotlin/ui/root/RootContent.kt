package ui.root

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.scale
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterState
import com.dokar.sonner.rememberToasterState
import di.appModule
import di.httpModule
import logic.datastore.ThemeMode
import logic.repository.UserPreferencesRepo
import logic.repository.UserPreferencesRepo.Companion.defaultPrefs
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject
import ui.app.AppScreen
import ui.blacklist.BlackListScreen
import ui.carousel.CarouselScreen
import ui.chat.ChatScreen
import ui.collection.CollectionScreen
import ui.coolpic.CoolPicScreen
import ui.dyh.DyhScreen
import ui.feed.FeedScreen
import ui.ffflist.FFFListScreen
import ui.history.HistoryScreen
import ui.login.LoginScreen
import ui.main.MainScreen
import ui.notification.NoticeScreen
import ui.others.CopyTextScreen
import ui.others.ImageScreen
import ui.root.RootComponent.Child
import ui.root.RootComponent.Config
import ui.search.SearchResultScreen
import ui.search.SearchScreen
import ui.settings.AboutScreen
import ui.settings.LicenseScreen
import ui.settings.ParamsScreen
import ui.theme.C001apkKMPTheme
import ui.topic.TopicScreen
import ui.user.UserScreen
import util.DeviceUtil
import util.DeviceUtil.apiVersion
import util.DeviceUtil.isLogin
import util.DeviceUtil.recordHistory
import util.DeviceUtil.sdkInt
import util.DeviceUtil.showSquare
import util.DeviceUtil.szlmId
import util.DeviceUtil.token
import util.DeviceUtil.uid
import util.DeviceUtil.userAgent
import util.DeviceUtil.username
import util.DeviceUtil.versionCode
import util.DeviceUtil.versionName
import util.DeviceUtil.xAppDevice
import kotlin.math.min


/**
 * Created by bggRGjQaUbCoE on 2024/7/4
 */

val LocalUserPreferences = staticCompositionLocalOf { defaultPrefs }

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun RootContent(
    component: RootComponent,
) {
    val toasterState = rememberToasterState()

    val windowSizeClass = calculateWindowSizeClass()
    val isCompat = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact

    val navigation = StackNavigation<Config>()

    KoinApplication(
        application = { modules(appModule, httpModule) }
    ) {
        val userPreferencesRepo = koinInject<UserPreferencesRepo>()
        val prefs by userPreferencesRepo.prefs.collectAsStateWithLifecycle(initialValue = defaultPrefs)

        szlmId = prefs.szlmId
        versionName = prefs.versionName
        versionCode = prefs.versionCode
        userAgent = prefs.userAgent
        sdkInt = prefs.sdkInt
        uid = prefs.uid
        username = prefs.username
        token = prefs.token
        apiVersion = prefs.apiVersion
        xAppDevice = prefs.xAppDevice
        showSquare = prefs.showSquare
        isLogin = prefs.isLogin
        recordHistory = prefs.recordHistory
        DeviceUtil.openInBrowser = prefs.openInBrowser

        CompositionLocalProvider(
            LocalUserPreferences provides prefs
        ) {
            C001apkKMPTheme(
                darkTheme = when (prefs.themeMode) {
                    ThemeMode.FOLLOW_SYSTEM -> isSystemInDarkTheme()
                    ThemeMode.ALWAYS_ON -> true
                    ThemeMode.ALWAYS_OFF -> false
                },
                themeType = prefs.themeType,
                seedColor = prefs.seedColor,
                materialYou = prefs.materialYou,
                pureBlack = prefs.pureBlack,
                fontScale = prefs.fontScale,
                contentScale = prefs.contentScale,
                content = {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        Row(modifier = Modifier.fillMaxSize()) {
                            ChildScreen(
                                modifier = Modifier.weight(1f),
                                stack = component.stack,
                                navigation = component.navigation,
                                toasterState = toasterState,
                                isCompat = isCompat,
                                onViewFeedRight = { id, viewReply ->
                                    navigation.replaceAll(Config.Feed(id, viewReply))
                                },
                                onFeedBack = component.navigation::pop
                            )
                            if (!isCompat) {
                                val componentContext =
                                    DefaultComponentContext(lifecycle = LifecycleRegistry())
                                val stack: Value<ChildStack<*, Child>> =
                                    componentContext.childStack(
                                        source = navigation,
                                        serializer = Config.serializer(),
                                        initialConfiguration = Config.NONE,
                                        handleBackButton = true,
                                        childFactory = component::child,
                                    )
                                ChildScreen(
                                    modifier = Modifier.weight(1f),
                                    stack = stack,
                                    navigation = component.navigation,
                                    toasterState = toasterState,
                                    isCompat = false,
                                    onViewFeedRight = navigation::navigateToFeed,
                                    onFeedBack = {
                                        navigation.replaceAll(Config.NONE)
                                    },
                                )
                            }
                        }
                    }
                }
            )
        }
    }

    Toaster(
        state = toasterState,
        alignment = Alignment.BottomCenter
    )

}

@Composable
fun ChildScreen(
    modifier: Modifier = Modifier,
    stack: Value<ChildStack<*, *>>,
    navigation: StackNavigation<Config>,
    toasterState: ToasterState,
    isCompat: Boolean,
    onViewFeedRight: (String, Boolean) -> Unit,
    onFeedBack: () -> Unit,
) {

    fun onViewFeed(id: String, viewReply: Boolean) {
        if (!isCompat) {
            onViewFeedRight(id, viewReply)
        } else {
            navigation.navigateToFeed(id, viewReply)
        }
    }

    var initialPage = 0
    Children(
        modifier = modifier.fillMaxSize(),
        stack = stack,
        animation = stackAnimation(fade() + scale())
    ) {
        when (val instance = it.instance) {
            Child.NONE -> {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AllInclusive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(55.dp)
                    )
                }
            }

            is Child.Main -> MainScreen(
                isCompat = isCompat,
                childComponentContext = instance.childComponentContext,
                onViewUser = navigation::navigateToUser,
                onViewFeed = ::onViewFeed,
                onSearch = { title, pageType, pageParam ->
                    initialPage = 0
                    navigation.navigateToSearch(
                        title,
                        pageType,
                        pageParam
                    )
                },
                onLogin = navigation::navigateToLogin,
                onOpenLink = { url, title ->
                    navigation.onOpenLink(
                        url = url,
                        title = title,
                        onViewFeed = ::onViewFeed,
                        toast = toasterState::show,
                    )
                },
                onCopyText = navigation::navigateToCopyText,
                onViewFFFList = navigation::navigateToFFFList,
                onReport = { _, _ -> },//  navigation::onReport,
                onViewNotice = navigation::navigateToNotice,
                onViewHistory = navigation::navigateToHistory,
                onViewImage = navigation::navigateToImageView,
                onParamsClick = navigation::navigateToParams,
                onAboutClick = navigation::navigateToAbout,
                onViewBlackList = navigation::navigateToBlackList,
            )

            is Child.Feed -> FeedScreen(
                childComponentContext = instance.childComponentContext,
                id = instance.id,
                isViewReply = instance.isViewReply,
                onBackClick = onFeedBack,
                onViewFeed = ::onViewFeed,
                onViewUser = navigation::navigateToUser,
                onOpenLink = { url, title ->
                    navigation.onOpenLink(
                        url = url,
                        title = title,
                        onViewFeed = ::onViewFeed,
                        toast = toasterState::show,
                    )
                },
                onCopyText = navigation::navigateToCopyText,
                onReport = { _, _ -> },
                onViewImage = navigation::navigateToImageView,
            )

            is Child.User -> UserScreen(
                childComponentContext = instance.childComponentContext,
                uid = instance.uid,
                onBackClick = navigation::pop,
                onViewFeed = ::onViewFeed,
                onViewUser = navigation::navigateToUser,
                onOpenLink = { url, title ->
                    navigation.onOpenLink(
                        url = url,
                        title = title,
                        onViewFeed = ::onViewFeed,
                        toast = toasterState::show,
                    )
                },
                onCopyText = navigation::navigateToCopyText,
                onReport = { _, _ -> },
                onViewImage = navigation::navigateToImageView,
                onSearch = { title, pageType, pageParam ->
                    initialPage = 0
                    navigation.navigateToSearch(
                        title,
                        pageType,
                        pageParam
                    )
                },
                onViewFFFList = navigation::navigateToFFFList,
                onPMUser = { viewUid, viewUsername ->
                    navigation.navigateToChat(
                        "${
                            min(
                                viewUid.toLongOrNull() ?: 0,
                                uid.toLongOrNull() ?: 0
                            )
                        }_${
                            maxOf(
                                viewUid.toLongOrNull() ?: 0,
                                uid.toLongOrNull() ?: 0
                            )
                        }",
                        viewUid,
                        viewUsername
                    )
                },
            )

            is Child.Topic -> TopicScreen(
                childComponentContext = instance.childComponentContext,
                tag = instance.tag,
                id = instance.id,
                onBackClick = navigation::pop,
                onViewUser = navigation::navigateToUser,
                onViewFeed = ::onViewFeed,
                onOpenLink = { url, title ->
                    navigation.onOpenLink(
                        url = url,
                        title = title,
                        onViewFeed = ::onViewFeed,
                        toast = toasterState::show,
                    )
                },
                onCopyText = navigation::navigateToCopyText,
                onReport = { _, _ -> },
                onViewImage = navigation::navigateToImageView,
                onSearch = { title, pageType, pageParam ->
                    initialPage = 0
                    navigation.navigateToSearch(
                        title,
                        pageType,
                        pageParam
                    )
                },
            )

            Child.About -> AboutScreen(
                onBackClick = navigation::pop,
                onLicenseClick = navigation::navigateToLicense
            )

            is Child.App -> AppScreen(
                childComponentContext = instance.childComponentContext,
                onBackClick = navigation::pop,
                packageName = instance.packageName,
                onViewUser = navigation::navigateToUser,
                onViewFeed = ::onViewFeed,
                onOpenLink = { url, title ->
                    navigation.onOpenLink(
                        url = url,
                        title = title,
                        onViewFeed = ::onViewFeed,
                        toast = toasterState::show,
                    )
                },
                onCopyText = navigation::navigateToCopyText,
                onReport = { _, _ -> },
                onViewImage = navigation::navigateToImageView,
                onSearch = { title, pageType, pageParam ->
                    initialPage = 0
                    navigation.navigateToSearch(
                        title,
                        pageType,
                        pageParam
                    )
                },
            )

            is Child.BlackList -> BlackListScreen(
                onBackClick = navigation::pop,
                type = instance.type,
                onViewUser = navigation::navigateToUser,
                onViewTopic = navigation::navigateToTopic,
            )

            is Child.Carousel -> CarouselScreen(
                childComponentContext = instance.childComponentContext,
                onBackClick = navigation::pop,
                url = instance.url,
                title = instance.title,
                onViewUser = navigation::navigateToUser,
                onViewFeed = ::onViewFeed,
                onOpenLink = { url, title ->
                    navigation.onOpenLink(
                        url = url,
                        title = title,
                        onViewFeed = ::onViewFeed,
                        toast = toasterState::show,
                    )
                },
                onCopyText = navigation::navigateToCopyText,
                onReport = { _, _ -> },
                onViewImage = navigation::navigateToImageView,
            )

            is Child.Chat -> ChatScreen(
                childComponentContext = instance.childComponentContext,
                onBackClick = navigation::pop,
                ukey = instance.ukey,
                uid = instance.uid,
                username = instance.username,
                onViewUser = navigation::navigateToUser,
                onReport = { _, _ -> },
                onViewImage = navigation::navigateToImageView,
            )

            is Child.Collection -> CollectionScreen(
                childComponentContext = instance.childComponentContext,
                onBackClick = navigation::pop,
                id = instance.id,
                onViewUser = navigation::navigateToUser,
                onViewFeed = ::onViewFeed,
                onOpenLink = { url, title ->
                    navigation.onOpenLink(
                        url = url,
                        title = title,
                        onViewFeed = ::onViewFeed,
                        toast = toasterState::show,
                    )
                },
                onCopyText = navigation::navigateToCopyText,
                onReport = { _, _ -> },
                onViewImage = navigation::navigateToImageView,
            )

            is Child.CoolPic -> CoolPicScreen(
                childComponentContext = instance.childComponentContext,
                onBackClick = navigation::pop,
                title = instance.title,
                onViewUser = navigation::navigateToUser,
                onViewFeed = ::onViewFeed,
                onOpenLink = { url, title ->
                    navigation.onOpenLink(
                        url = url,
                        title = title,
                        onViewFeed = ::onViewFeed,
                        toast = toasterState::show,
                    )
                },
                onCopyText = navigation::navigateToCopyText,
                onReport = { _, _ -> },
                onViewImage = navigation::navigateToImageView,
            )

            is Child.CopyText -> CopyTextScreen(
                onBackClick = navigation::pop,
                text = instance.text.orEmpty(),
            )

            is Child.Dyh -> DyhScreen(
                childComponentContext = instance.childComponentContext,
                onBackClick = navigation::pop,
                id = instance.id,
                title = instance.title,
                onViewUser = navigation::navigateToUser,
                onViewFeed = ::onViewFeed,
                onOpenLink = { url, title ->
                    navigation.onOpenLink(
                        url = url,
                        title = title,
                        onViewFeed = ::onViewFeed,
                        toast = toasterState::show,
                    )
                },
                onCopyText = navigation::navigateToCopyText,
                onReport = { _, _ -> },
                onViewImage = navigation::navigateToImageView,
            )

            is Child.FFFList -> FFFListScreen(
                childComponentContext = instance.childComponentContext,
                onBackClick = navigation::pop,
                uid = instance.uid,
                type = instance.type,
                id = instance.id,
                title = instance.title,
                onViewUser = navigation::navigateToUser,
                onViewFeed = ::onViewFeed,
                onOpenLink = { url, title ->
                    navigation.onOpenLink(
                        url = url,
                        title = title,
                        onViewFeed = ::onViewFeed,
                        toast = toasterState::show,
                    )
                },
                onCopyText = navigation::navigateToCopyText,
                onReport = { _, _ -> },
                onViewImage = navigation::navigateToImageView,
                onViewFFFList = navigation::navigateToFFFList
            )

            is Child.History -> HistoryScreen(
                onBackClick = navigation::pop,
                type = instance.type,
                onViewUser = navigation::navigateToUser,
                onViewFeed = ::onViewFeed,
                onOpenLink = { url, title ->
                    navigation.onOpenLink(
                        url = url,
                        title = title,
                        onViewFeed = ::onViewFeed,
                        toast = toasterState::show,
                    )
                },
                onCopyText = navigation::navigateToCopyText,
                onReport = { _, _ -> },
            )

            is Child.Image -> ImageScreen(
                onBackClick = navigation::pop,
                picArr = instance.picArr,
                initialPage = instance.initialPage,
            )

            Child.Login -> LoginScreen(
                onBackClick = navigation::pop,
            )

            is Child.Notice -> NoticeScreen(
                childComponentContext = instance.childComponentContext,
                onBackClick = navigation::pop,
                type = instance.type,
                onViewUser = navigation::navigateToUser,
                onViewFeed = ::onViewFeed,
                onOpenLink = { url, title ->
                    navigation.onOpenLink(
                        url = url,
                        title = title,
                        onViewFeed = ::onViewFeed,
                        toast = toasterState::show,
                    )
                },
                onCopyText = navigation::navigateToCopyText,
                onReport = { _, _ -> },
                onViewImage = navigation::navigateToImageView,
                onViewChat = navigation::navigateToChat,
            )

            Child.Params -> ParamsScreen(
                onBackClick = navigation::pop,
            )

            is Child.Search -> SearchScreen(
                childComponentContext = instance.childComponentContext,
                onBackClick = navigation::pop,
                title = instance.title,
                onSearch = { keyword ->
                    navigation.navigateToSearchResult(
                        keyword,
                        instance.title,
                        instance.pageType,
                        instance.pageParam
                    )
                }
            )

            is Child.SearchResult -> SearchResultScreen(
                childComponentContext = instance.childComponentContext,
                onBackClick = navigation::pop,
                keyword = instance.keyword,
                title = instance.title,
                pageType = instance.pageType,
                pageParam = instance.pageParam,
                onViewUser = navigation::navigateToUser,
                onViewFeed = ::onViewFeed,
                onOpenLink = { url, title ->
                    navigation.onOpenLink(
                        url = url,
                        title = title,
                        onViewFeed = ::onViewFeed,
                        toast = toasterState::show,
                    )
                },
                onCopyText = navigation::navigateToCopyText,
                onReport = { _, _ -> },
                onViewImage = navigation::navigateToImageView,
                initialPage = initialPage,
                updateInitPage = { index ->
                    initialPage = index
                }
            )

            Child.License -> LicenseScreen(
                onBackClick = navigation::pop,
            )
        }
    }
}
