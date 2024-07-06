package di

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import constant.Constants.APP_ID
import constant.Constants.CHANNEL
import constant.Constants.DARK_MODE
import constant.Constants.LOCALE
import constant.Constants.MODE
import constant.Constants.REQUEST_WITH
import createDataStore
import di.Type.FAVORITE
import di.Type.HISTORY
import di.Type.RECENT_EMOJI
import di.Type.SEARCH_HISTORY
import di.Type.TOPIC_BLACKLIST
import di.Type.USER_BLACKLIST
import getHistoryFavoriteDatabase
import getSearchHistoryDataBase
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import logic.dao.HistoryFavoriteDao
import logic.dao.StringEntityDao
import logic.database.FeedEntityType
import logic.database.HistoryFavoriteDatabase
import logic.database.StringEntityDatabase
import logic.database.StringEntityType
import logic.repository.BlackListRepo
import logic.repository.HistoryFavoriteRepo
import logic.repository.NetworkRepo
import logic.repository.RecentEmojiRepo
import logic.repository.SearchHistoryRepo
import logic.repository.UserPreferencesRepo
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import ui.app.AppContentViewModel
import ui.app.AppViewModel
import ui.blacklist.BlackListType
import ui.blacklist.BlackListViewModel
import ui.carousel.CarouselViewModel
import ui.chat.ChatViewModel
import ui.collection.CollectionViewModel
import ui.coolpic.CoolPicContentViewModel
import ui.dyh.DyhContentViewModel
import ui.feed.FeedViewModel
import ui.ffflist.FFFContentViewModel
import ui.history.HistoryType
import ui.history.HistoryViewModel
import ui.home.TabType
import ui.home.feed.HomeFeedViewModel
import ui.home.topic.HomeTopicViewModel
import ui.login.LoginViewModel
import ui.main.MainViewModel
import ui.message.MessageViewModel
import ui.notification.NoticeViewModel
import ui.search.SearchContentViewModel
import ui.search.SearchViewModel
import ui.settings.SettingsViewModel
import ui.topic.TopicContentViewModel
import ui.topic.TopicViewModel
import ui.user.UserViewModel
import util.DeviceUtil
import util.DeviceUtil.SESSID
import util.DeviceUtil.isGetCaptcha
import util.DeviceUtil.isGetLoginParam
import util.DeviceUtil.isGetSmsLoginParam
import util.DeviceUtil.isGetSmsToken
import util.DeviceUtil.isPreGetLoginParam
import util.DeviceUtil.isTryLogin
import util.DeviceUtil.xAppDevice
import util.TokenDeviceUtils.getTokenV2

/**
 * Created by bggRGjQaUbCoE on 2024/6/27
 */
val httpModule = module {
    single {
        HttpClient {
            install(ContentNegotiation) {
                gson()
            }

            install(Logging) {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        println(message)
                    }
                }
                //  filter { filter-> filter.url.host.contains("coolapk.com") }
                //  sanitizeHeader { header-> header == HttpHeaders.Authorization }
            }

            install(DefaultRequest) {
                if (isPreGetLoginParam) {
                    isPreGetLoginParam = false

                    header(
                        "sec-ch-ua",
                        """"Android WebView";v="117", "Not;A=Brand";v="8", "Chromium";v="117"""
                    )
                    header("sec-ch-ua-mobile", "?1")
                    header("sec-ch-ua-platform", "Android")
                    header("Upgrade-Insecure-Requests", "1")
                    header("User-Agent", DeviceUtil.userAgent)
                    header(
                        "Accept",
                        """text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"""
                    )
                    header("X-Requested-With", APP_ID)
                } else if (isGetLoginParam) {
                    isGetLoginParam = false

                    header(
                        "sec-ch-ua",
                        """"Android WebView";v="117", "Not;A=Brand";v="8", "Chromium";v="117"""
                    )
                    header("sec-ch-ua-mobile", "?1")
                    header("sec-ch-ua-platform", "Android")
                    header("Upgrade-Insecure-Requests", "1")
                    header("User-Agent", DeviceUtil.userAgent)
                    header(
                        "Accept",
                        """text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"""
                    )
                    header("User-Agent", DeviceUtil.userAgent)
                    header("X-Requested-With", APP_ID)
                    header("X-App-Id", APP_ID)
                    header("Cookie", SESSID)
                } else if (isTryLogin) {
                    isTryLogin = false

                    header("User-Agent", DeviceUtil.userAgent)
                    header("Cookie", "$SESSID; forward=https://www.coolapk.com")
                    header("X-Requested-With", REQUEST_WITH)
                    header("Content-Type", "application/x-www-form-urlencoded")
                } else if (isGetCaptcha) {
                    isGetCaptcha = false

                    header("User-Agent", DeviceUtil.userAgent)
                    header(
                        "sec-ch-ua",
                        """"Android WebView";v="117", "Not;A=Brand";v="8", "Chromium";v="117"""
                    )
                    header("sec-ch-ua-mobile", "?1")
                    header("User-Agent", DeviceUtil.userAgent)
                    header("sec-ch-ua-platform", "Android")
                    header(
                        "Accept",
                        """image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8"""
                    )
                    header("X-Requested-With", APP_ID)
                    header("Sec-Fetch-Site", "same-origin")
                    header("Sec-Fetch-Mode", "no-cors")
                    header("Sec-Fetch-Dest", "image")
                    header("Referer", "https://account.coolapk.com/auth/loginByCoolapk")
                    header("Cookie", "$SESSID; forward=https://www.coolapk.com")
                } else if (isGetSmsToken) {
                    isGetSmsToken = false
                    header(
                        "sec-ch-ua",
                        """"Android WebView";v="117", "Not;A=Brand";v="8", "Chromium";v="117"""
                    )
                    header("Content-Type", "application/x-www-form-urlencoded")
                    header("X-Requested-With", REQUEST_WITH)
                    header("sec-ch-ua-mobile", "?1")
                    header("User-Agent", DeviceUtil.userAgent)
                    header("sec-ch-ua-platform", "Android")
                    header("Accept", "*/*")
                    header("Origin", "https://account.coolapk.com")
                    header("Sec-Fetch-Site", "same-origin")
                    header("Sec-Fetch-Mode", "cors")
                    header("Sec-Fetch-Dest", "empty")
                    header("Referer", "https://account.coolapk.com/auth/login?type=mobile")
                    header("Accept-Encoding", "gzip, deflate, br")
                    header("Accept-Language", "zh-CM,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                    header("Cookie", "$SESSID; forward=https://www.coolapk.com")
                } else if (isGetSmsLoginParam) {
                    isGetSmsLoginParam = false
                    header(
                        "sec-ch-ua",
                        """"Android WebView";v="117", "Not;A=Brand";v="8", "Chromium";v="117"""
                    )
                    header("sec-ch-ua-mobile", "?1")
                    header("sec-ch-ua-platform", "Android")
                    header("Upgrade=Insecure-Requests", "1")
                    header("User-Agent", DeviceUtil.userAgent)
                    header(
                        "Accept",
                        """text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7"""
                    )
                    header("X-Requested-With", APP_ID)
                    header("Sec-Fetch-Site", "none")
                    header("Sec-Fetch-Mode", "navigate")
                    header("Sec-Fetch-User", "?1")
                    header("Sec-Fetch-Dest", "document")
                    header("Accept-Encoding", "gzip, deflate, br")
                    header("Accept-Language", "zh-CM,zh;q=0.9,en-US;q=0.8,en;q=0.7")
                } else {
                    header("User-Agent", DeviceUtil.userAgent)
                    header("X-Requested-With", REQUEST_WITH)
                    header("X-Sdk-Int", DeviceUtil.sdkInt)
                    header("X-Sdk-Locale", LOCALE)
                    header("X-App-Id", APP_ID)
                    header("X-App-Token", xAppDevice.getTokenV2())
                    header("X-App-Version", DeviceUtil.versionName)
                    header("X-App-Code", DeviceUtil.versionCode)
                    header("X-Api-Version", DeviceUtil.apiVersion)
                    header("X-App-Device", xAppDevice)
                    header("X-Dark-Mode", DARK_MODE)
                    header("X-App-Channel", CHANNEL)
                    header("X-App-Mode", MODE)
                    header("X-App-Supported", DeviceUtil.versionCode)
                    if (DeviceUtil.isLogin)
                        header(
                            "Cookie",
                            "uid=${DeviceUtil.uid}; username=${DeviceUtil.username}; token=${DeviceUtil.token}"
                        )
                    else header("Cookie", SESSID)
                }

            }

            install(HttpTimeout) {
                connectTimeoutMillis = 5000
                requestTimeoutMillis = 5000
                socketTimeoutMillis = 5000
            }

        }
    }
}

object Type {
    const val SEARCH_HISTORY = "SearchHistory"
    const val USER_BLACKLIST = "UserBlacklist"
    const val TOPIC_BLACKLIST = "TopicBlacklist"
    const val RECENT_EMOJI = "RecentEmoji"
    const val HISTORY = "HISTORY"
    const val FAVORITE = "FAVORITE"
}

val appModule = module {

    single<CoroutineDispatcher>(named("MainDispatcher")) { Dispatchers.Main }
    single<CoroutineDispatcher>(named("DefaultDispatcher")) { Dispatchers.Default }
    single<CoroutineDispatcher>(named("IoDispatcher")) { Dispatchers.IO }

    single<StringEntityDatabase>(named(SEARCH_HISTORY)) {
        getSearchHistoryDataBase(StringEntityType.HISTORY)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(get<CoroutineDispatcher>(named("IoDispatcher")))
            .build()
    }
    single<StringEntityDao>(named(SEARCH_HISTORY)) {
        get<StringEntityDatabase>(named(SEARCH_HISTORY)).stringEntityDao()
    }

    single<StringEntityDatabase>(named(USER_BLACKLIST)) {
        getSearchHistoryDataBase(StringEntityType.USER)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(get<CoroutineDispatcher>(named("IoDispatcher")))
            .build()
    }
    single<StringEntityDao>(named(USER_BLACKLIST)) {
        get<StringEntityDatabase>(named(USER_BLACKLIST)).stringEntityDao()
    }

    single<StringEntityDatabase>(named(TOPIC_BLACKLIST)) {
        getSearchHistoryDataBase(StringEntityType.TOPIC)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(get<CoroutineDispatcher>(named("IoDispatcher")))
            .build()
    }
    single<StringEntityDao>(named(TOPIC_BLACKLIST)) {
        get<StringEntityDatabase>(named(TOPIC_BLACKLIST)).stringEntityDao()
    }

    single<StringEntityDatabase>(named(RECENT_EMOJI)) {
        getSearchHistoryDataBase(StringEntityType.EMOJI)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(get<CoroutineDispatcher>(named("IoDispatcher")))
            .build()
    }
    single<StringEntityDao>(named(RECENT_EMOJI)) {
        get<StringEntityDatabase>(named(RECENT_EMOJI)).stringEntityDao()
    }

    single<HistoryFavoriteDatabase>(named(HISTORY)) {
        getHistoryFavoriteDatabase(FeedEntityType.HISTORY)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(get<CoroutineDispatcher>(named("IoDispatcher")))
            .build()
    }
    single<HistoryFavoriteDao>(named(HISTORY)) {
        get<HistoryFavoriteDatabase>(named(HISTORY)).historyFavoriteDao()
    }

    single<HistoryFavoriteDatabase>(named(FAVORITE)) {
        getHistoryFavoriteDatabase(FeedEntityType.FAVORITE)
            .fallbackToDestructiveMigrationOnDowngrade(true)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(get<CoroutineDispatcher>(named("IoDispatcher")))
            .build()
    }
    single<HistoryFavoriteDao>(named(FAVORITE)) {
        get<HistoryFavoriteDatabase>(named(FAVORITE)).historyFavoriteDao()
    }

    factory {
        SearchViewModel(
            searchHistoryRepo = get(),
            dispatcher = get(named("IoDispatcher")),
        )
    }

    factory {
        RecentEmojiRepo(
            recentEmojiDao = get(named(RECENT_EMOJI)),
        )
    }

    factory {
        SearchHistoryRepo(
            searchHistoryDao = get(named(SEARCH_HISTORY)),
        )
    }

    factory {
        BlackListRepo(
            userBlackListDao = get(named(USER_BLACKLIST)),
            topicBlackListDao = get(named(TOPIC_BLACKLIST)),
        )
    }

    factory {
        HistoryFavoriteRepo(
            browseHistoryDao = get(named(HISTORY)),
            feedFavoriteDao = get(named(FAVORITE)),
        )
    }

    single {
        NetworkRepo(
            httpClient = get(),
            dispatcher = get(named("IoDispatcher")),
        )
    }

    singleOf(::createDataStore)
    singleOf(::UserPreferencesRepo)

    factory {
        SettingsViewModel(
            userPreferencesRepo = get(),
            dispatcher = get(named("IoDispatcher")),
        )
    }

    factory { (type: TabType, dataListUrl: String, dataListTitle: String, installTime: String) ->
        HomeFeedViewModel(
            type = type,
            dataListUrl = dataListUrl,
            dataListTitle = dataListTitle,
            installTime = installTime,
            networkRepo = get(),
            dispatcher = get(named("IoDispatcher")),
            userPreferencesRepo = get(),
            blackListRepo = get(),
        )
    }

    factory { (id: String, isViewReply: Boolean) ->
        FeedViewModel(
            id = id,
            isViewReply = isViewReply,
            networkRepo = get(),
            dispatcher = get(named("IoDispatcher")),
            blackListRepo = get(),
            historyFavoriteRepo = get(),
        )
    }

    factory { (packageName: String) ->
        AppViewModel(
            packageName = packageName,
            networkRepo = get(),
            dispatcher = get(named("IoDispatcher")),
            blackListRepo = get(),
        )
    }

    factory { (url: String, appCommentTitle: String) ->
        AppContentViewModel(
            url = url,
            appCommentTitle = appCommentTitle,
            networkRepo = get(),
            dispatcher = get(named("IoDispatcher")),
            blackListRepo = get(),
        )
    }

    factory { (url: String, tag: String, id: String) ->
        TopicViewModel(
            url = url,
            tag = tag,
            id = id,
            networkRepo = get(),
            dispatcher = get(named("IoDispatcher")),
            blackListRepo = get(),
        )
    }

    factory { (url: String, title: String) ->
        TopicContentViewModel(
            url = url,
            title = title,
            networkRepo = get(),
            dispatcher = get(named("IoDispatcher")),
            blackListRepo = get(),
        )
    }

    factory { (type: String, keyword: String, pageType: String, pageParam: String) ->
        SearchContentViewModel(
            type = type,
            keyword = keyword,
            pageType = pageType,
            pageParam = pageParam,
            networkRepo = get(),
            dispatcher = get(named("IoDispatcher")),
            blackListRepo = get(),
        )
    }

    factory { (isInit: Boolean, url: String, title: String) ->
        CarouselViewModel(
            isInit = isInit,
            url = url,
            title = title,
            networkRepo = get(),
            dispatcher = get(named("IoDispatcher")),
            blackListRepo = get(),
        )
    }

    factory { (title: String, type: String) ->
        CoolPicContentViewModel(
            title = title,
            type = type,
            networkRepo = get(),
            dispatcher = get(named("IoDispatcher")),
            blackListRepo = get(),
        )
    }

    factory { (id: String, type: String) ->
        DyhContentViewModel(
            id = id,
            type = type,
            networkRepo = get(),
            dispatcher = get(named("IoDispatcher")),
            blackListRepo = get(),
        )
    }

    factory { (uid: String) ->
        UserViewModel(
            uid = uid,
            networkRepo = get(),
            dispatcher = get(named("IoDispatcher")),
            blackListRepo = get(),
        )
    }

    factory {
        MainViewModel(
            networkRepo = get(),
            userPreferencesRepo = get(),
            dispatcher = get(named("IoDispatcher")),
        )
    }

    factory { (url: String) ->
        HomeTopicViewModel(
            url = url,
            networkRepo = get(),
            dispatcher = get(named("IoDispatcher")),
            blackListRepo = get(),
        )
    }

    factory { (url: String) ->
        MessageViewModel(
            url = url,
            networkRepo = get(),
            dispatcher = get(named("IoDispatcher")),
            userPreferencesRepo = get(),
            blackListRepo = get(),
        )
    }

    factory {
        LoginViewModel(
            networkRepo = get(),
            dispatcher = get(named("IoDispatcher")),
            userPreferencesRepo = get(),
        )
    }

    factory { (url: String) ->
        NoticeViewModel(
            url = url,
            networkRepo = get(),
            dispatcher = get(named("IoDispatcher")),
            blackListRepo = get(),
        )
    }

    factory { (url: String, uid: String?, id: String?, showDefault: Int?) ->
        FFFContentViewModel(
            url = url,
            uid = uid,
            id = id,
            showDefault = showDefault,
            networkRepo = get(),
            dispatcher = get(named("IoDispatcher")),
            blackListRepo = get(),
        )
    }

    factory { (id: String) ->
        CollectionViewModel(
            id = id,
            networkRepo = get(),
            dispatcher = get(named("IoDispatcher")),
            blackListRepo = get(),
        )
    }

    factory { (type: HistoryType) ->
        HistoryViewModel(
            type = type,
            blackListRepo = get(),
            historyFavoriteRepo = get(),
            dispatcher = get(named("IoDispatcher")),
        )
    }

    factory { (type: BlackListType) ->
        BlackListViewModel(
            type = type,
            blackListRepo = get(),
            dispatcher = get(named("IoDispatcher")),
        )
    }

    factory { (ukey: String) ->
        ChatViewModel(
            ukey = ukey,
            networkRepo = get(),
            blackListRepo = get(),
            dispatcher = get(named("IoDispatcher")),
            recentEmojiRepo = get(),
        )
    }

}