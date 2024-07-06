package ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import constant.Constants.CHANNEL
import constant.Constants.EMPTY_STRING
import constant.Constants.PREFIX_APP
import constant.Constants.PREFIX_CAROUSEL
import constant.Constants.PREFIX_CAROUSEL1
import constant.Constants.PREFIX_COLLECTION
import constant.Constants.PREFIX_COOLMARKET
import constant.Constants.PREFIX_DYH
import constant.Constants.PREFIX_FEED
import constant.Constants.PREFIX_GAME
import constant.Constants.PREFIX_HTTP
import constant.Constants.PREFIX_PRODUCT
import constant.Constants.PREFIX_TOPIC
import constant.Constants.PREFIX_USER
import constant.Constants.PREFIX_USER_LIST
import copyToClipboard
import kotlinx.serialization.Serializable
import openInBrowser
import ui.ffflist.FFFListType
import ui.root.RootComponent.Config
import util.DeviceUtil
import util.decode
import java.net.URI

/**
 * Created by bggRGjQaUbCoE on 2024/7/4
 */
class RootComponent(
    componentContext: ComponentContext,
) : ComponentContext by componentContext {

    val navigation = StackNavigation<Config>()

    val stack: Value<ChildStack<*, Child>> =
        childStack(
            source = navigation,
            serializer = Config.serializer(),
            initialConfiguration = Config.Main,
            handleBackButton = true,
            childFactory = ::child,
        )

    fun child(config: Config, childComponentContext: ComponentContext) = when (config) {
        Config.NONE -> Child.NONE
        is Config.Feed -> Child.Feed(childComponentContext, config.id, config.isViewReply)
        Config.About -> Child.About
        is Config.App -> Child.App(childComponentContext, config.packageName)
        is Config.BlackList -> Child.BlackList(childComponentContext, config.type)
        is Config.Carousel -> Child.Carousel(childComponentContext, config.url, config.title)
        is Config.Chat -> Child.Chat(
            childComponentContext,
            config.ukey,
            config.uid,
            config.username
        )

        is Config.Collection -> Child.Collection(childComponentContext, config.id)
        is Config.CoolPic -> Child.CoolPic(childComponentContext, config.title)
        is Config.CopyText -> Child.CopyText(childComponentContext, config.text)
        is Config.Dyh -> Child.Dyh(childComponentContext, config.id, config.title)
        is Config.FFFList -> Child.FFFList(
            childComponentContext,
            config.uid,
            config.type,
            config.id,
            config.title
        )

        is Config.History -> Child.History(childComponentContext, config.type)
        is Config.Image -> Child.Image(childComponentContext, config.picArr, config.initialPage)
        Config.License -> Child.License
        Config.Login -> Child.Login
        Config.Main -> Child.Main(childComponentContext)
        is Config.Notice -> Child.Notice(childComponentContext, config.type)
        Config.Params -> Child.Params
        is Config.Search -> Child.Search(
            childComponentContext,
            config.title,
            config.pageType,
            config.pageParam
        )

        is Config.SearchResult -> Child.SearchResult(
            childComponentContext,
            config.keyword,
            config.title,
            config.pageType,
            config.pageParam
        )

        is Config.Topic -> Child.Topic(childComponentContext, config.tag, config.id)
        is Config.User -> Child.User(childComponentContext, config.uid)
    }

    sealed class Child {
        data object NONE : Child()

        class Main(val childComponentContext: ComponentContext) : Child()

        class Feed(
            val childComponentContext: ComponentContext,
            val id: String,
            val isViewReply: Boolean
        ) : Child()

        class User(val childComponentContext: ComponentContext, val uid: String) : Child()

        class Topic(
            val childComponentContext: ComponentContext,
            val tag: String?,
            val id: String?
        ) : Child()

        class Search(
            val childComponentContext: ComponentContext,
            val title: String?,
            val pageType: String?,
            val pageParam: String?
        ) : Child()

        class SearchResult(
            val childComponentContext: ComponentContext,
            val keyword: String,
            val title: String?,
            val pageType: String?,
            val pageParam: String?
        ) : Child()

        class Image(
            val childComponentContext: ComponentContext,
            val picArr: List<String>,
            val initialPage: Int
        ) : Child()

        class Collection(val childComponentContext: ComponentContext, val id: String) : Child()

        class Chat(
            val childComponentContext: ComponentContext,
            val ukey: String,
            val uid: String,
            val username: String
        ) : Child()

        class History(val childComponentContext: ComponentContext, val type: String) : Child()

        class BlackList(val childComponentContext: ComponentContext, val type: String) : Child()

        class Notice(val childComponentContext: ComponentContext, val type: String) : Child()

        class CoolPic(val childComponentContext: ComponentContext, val title: String) : Child()

        class Dyh(val childComponentContext: ComponentContext, val id: String, val title: String) :
            Child()

        class FFFList(
            val childComponentContext: ComponentContext,
            val uid: String?,
            val type: String,
            val id: String?,
            val title: String?
        ) :
            Child()

        class Carousel(
            val childComponentContext: ComponentContext,
            val url: String,
            val title: String
        ) : Child()

        class App(val childComponentContext: ComponentContext, val packageName: String) : Child()

        class CopyText(val childComponentContext: ComponentContext, val text: String?) : Child()

        data object Login : Child()

        data object Params : Child()

        data object About : Child()

        data object License : Child()
    }

    @Serializable
    sealed interface Config {
        @Serializable
        data object NONE : Config

        @Serializable
        data object Main : Config

        @Serializable
        class Feed(val id: String, val isViewReply: Boolean) : Config

        @Serializable
        class User(val uid: String) : Config

        @Serializable
        class Topic(val tag: String?, val id: String?) : Config

        @Serializable
        class Search(val title: String?, val pageType: String?, val pageParam: String?) : Config

        @Serializable
        class SearchResult(
            val keyword: String,
            val title: String?,
            val pageType: String?,
            val pageParam: String?
        ) : Config

        @Serializable
        class Image(val picArr: List<String>, val initialPage: Int) : Config

        @Serializable
        class Collection(val id: String) : Config

        @Serializable
        class Chat(val ukey: String, val uid: String, val username: String) : Config

        @Serializable
        class History(val type: String) : Config

        @Serializable
        class BlackList(val type: String) : Config

        @Serializable
        class Notice(val type: String) : Config

        @Serializable
        class CoolPic(val title: String) : Config

        @Serializable
        class Dyh(val id: String, val title: String) : Config

        @Serializable
        class FFFList(val uid: String?, val type: String, val id: String?, val title: String?) :
            Config

        @Serializable
        class Carousel(val url: String, val title: String) : Config

        @Serializable
        class App(val packageName: String) : Config

        @Serializable
        class CopyText(val text: String?) : Config

        @Serializable
        data object Login : Config

        @Serializable
        data object Params : Config

        @Serializable
        data object About : Config

        @Serializable
        data object License : Config
    }
}

fun StackNavigation<Config>.navigateToFeed(id: String, isViewReply: Boolean) {
    push(Config.Feed(id, isViewReply))
}

fun StackNavigation<Config>.navigateToUser(uid: String) {
    push(Config.User(uid))
}

fun StackNavigation<Config>.navigateToSearch(
    title: String?,
    pageType: String?,
    pageParam: String?
) {
    push(Config.Search(title, pageType, pageParam))
}

fun StackNavigation<Config>.navigateToSearchResult(
    keyword: String,
    title: String?,
    pageType: String?,
    pageParam: String?
) {
    push(Config.SearchResult(keyword, title, pageType, pageParam))
}

fun StackNavigation<Config>.navigateToImageView(picArr: List<String>, initialPage: Int) {
    push(Config.Image(picArr, initialPage))
}

fun StackNavigation<Config>.navigateToCollection(id: String) {
    push(Config.Collection(id))
}

fun StackNavigation<Config>.navigateToChat(ukey: String, uid: String, username: String) {
    push(Config.Chat(ukey, uid, username))
}

fun StackNavigation<Config>.navigateToHistory(type: String) {
    push(Config.History(type))
}

fun StackNavigation<Config>.navigateToBlackList(type: String) {
    push(Config.BlackList(type))
}

fun StackNavigation<Config>.navigateToNotice(type: String) {
    push(Config.Notice(type))
}

fun StackNavigation<Config>.navigateToCoolPic(title: String) {
    push(Config.CoolPic(title))
}

fun StackNavigation<Config>.navigateToDyh(id: String, title: String) {
    push(Config.Dyh(id, title))
}

fun StackNavigation<Config>.navigateToFFFList(
    uid: String?,
    type: String,
    id: String?,
    title: String?
) {
    push(Config.FFFList(uid, type, id, title))
}

fun StackNavigation<Config>.navigateToApp(packageName: String) {
    push(Config.App(packageName))
}

fun StackNavigation<Config>.navigateToTopic(tag: String?, id: String?) {
    push(Config.Topic(tag, id))
}

fun StackNavigation<Config>.navigateToCopyText(text: String?) {
    push(Config.CopyText(text))
}

fun StackNavigation<Config>.navigateToCarousel(url: String, title: String) {
    push(Config.Carousel(url, title))
}

fun StackNavigation<Config>.navigateToLogin() {
    push(Config.Login)
}

fun StackNavigation<Config>.navigateToAbout() {
    push(Config.About)
}

fun StackNavigation<Config>.navigateToParams() {
    push(Config.Params)
}

fun StackNavigation<Config>.navigateToLicense() {
    push(Config.License)
}

fun StackNavigation<Config>.onOpenLink(
    url: String,
    title: String? = null,
    needConvert: Boolean = false,
    toast: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
) {
    if (url.isEmpty())
        return
    val path = with(url.decode) {
        if (needConvert) {
            if (this.startsWith(PREFIX_COOLMARKET))
                this.replaceFirst(PREFIX_COOLMARKET, "/")
            else {
                val uri = URI(url)
                if (uri.host?.contains(CHANNEL) == true)
                    "${uri.path}?${uri.query}"
                else url
            }
        } else this
    }
    when {
        path.startsWith(PREFIX_USER) -> {
            navigateToUser(path.replaceFirst(PREFIX_USER, EMPTY_STRING))
        }

        path.startsWith(PREFIX_FEED) -> {
            val id = path.replaceFirst(PREFIX_FEED, EMPTY_STRING).replace("?", "&")
            onViewFeed(id, id.contains("rid"))
        }

        path.startsWith(PREFIX_TOPIC) -> {
            val tag = path.replaceFirst(PREFIX_TOPIC, EMPTY_STRING)
                .replace("\\?type=[A-Za-z0-9]+".toRegex(), EMPTY_STRING)
            if (path.contains("type=8"))
                navigateToCoolPic(tag)
            else
                navigateToTopic(id = null, tag = tag)
        }

        path.startsWith(PREFIX_PRODUCT) -> {
            navigateToTopic(
                id = path.replaceFirst(PREFIX_PRODUCT, EMPTY_STRING),
                tag = null,
            )
        }

        path.startsWith(PREFIX_APP) -> {
            navigateToApp(packageName = path.replaceFirst(PREFIX_APP, EMPTY_STRING))
        }

        path.startsWith(PREFIX_GAME) -> {
            navigateToApp(packageName = path.replaceFirst(PREFIX_GAME, EMPTY_STRING))
        }

        path.startsWith(PREFIX_CAROUSEL) -> {
            navigateToCarousel(
                path.replaceFirst(PREFIX_CAROUSEL, EMPTY_STRING),
                title.orEmpty()
            )
        }

        path.startsWith(PREFIX_CAROUSEL1) -> {
            navigateToCarousel(path.replaceFirst("#", EMPTY_STRING), title.orEmpty())
        }

        path.startsWith(PREFIX_USER_LIST) -> {
            val type = when {
                path.contains("myFollowList") -> FFFListType.USER_FOLLOW.name
                else -> EMPTY_STRING
            }
            navigateToFFFList(DeviceUtil.uid, type, null, null)
        }

        path.startsWith(PREFIX_DYH) -> {
            navigateToDyh(path.replaceFirst(PREFIX_DYH, EMPTY_STRING), title.orEmpty())
        }

        path.startsWith(PREFIX_COLLECTION) -> {
            navigateToCollection(path.replaceFirst(PREFIX_COLLECTION, EMPTY_STRING))
        }

        else -> {
            if (!needConvert)
                onOpenLink(url, title, true, toast, onViewFeed)
            else {
                if (url.startsWith(PREFIX_HTTP)) {
                    openInBrowser(url)
                } else {
                    toast("unsupported url: $url")
                    copyToClipboard(url)
                }
            }
        }
    }
}