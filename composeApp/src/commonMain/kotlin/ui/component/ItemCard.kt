package ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import logic.model.HomeFeedResponse
import logic.state.LoadingState
import ui.base.LikeType
import ui.component.cards.AppCard
import ui.component.cards.AppCardType
import ui.component.cards.CarouselCard
import ui.component.cards.CollectionCard
import ui.component.cards.FeedCard
import ui.component.cards.FeedReplyCard
import ui.component.cards.IconLinkGridCard
import ui.component.cards.IconMiniGridCard
import ui.component.cards.IconMiniScrollCard
import ui.component.cards.IconScrollCard
import ui.component.cards.ImageSquareScrollCard
import ui.component.cards.ImageTextScrollCard
import ui.component.cards.LoadingCard
import ui.component.cards.MessageCard
import ui.component.cards.MsgCard
import ui.component.cards.NotificationCard
import ui.component.cards.TextCard
import ui.component.cards.TitleCard
import util.ReportType

/**
 * Created by bggRGjQaUbCoE on 2024/6/10
 */
fun LazyListScope.ItemCard(
    loadingState: LoadingState<List<HomeFeedResponse.Data>>,
    loadMore: () -> Unit,
    isEnd: Boolean,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onShowTotalReply: ((String, String, String?) -> Unit)? = null,
    isHomeFeed: Boolean = false,
    onReport: ((String, ReportType) -> Unit)? = null,
    isTotalReply: Boolean = false,
    isReply2Reply: Boolean = false,
    onViewFFFList: ((String?, String, String?, String?) -> Unit)? = null,
    onLike: ((String, Int, LikeType) -> Unit)? = null,
    onDelete: ((String, LikeType, String?) -> Unit)? = null,
    onBlockUser: (String, String?) -> Unit,
    onFollowUser: ((String, Int) -> Unit)? = null,
    onHandleRecent: ((String, String, String, Int) -> Unit)? = null,
    onHandleMessage: ((String, Int) -> Unit)? = null,
    onViewChat: ((String, String, String) -> Unit)? = null,
    onDeleteNotice: ((String) -> Unit)? = null,
    onReply: ((String, String, String, String?) -> Unit)? = null,
    onViewImage: (List<String>, Int) -> Unit,
) {

    when (loadingState) {
        LoadingState.Loading, LoadingState.Empty, is LoadingState.Error -> {
            item(key = "loadingState") {
                Box(modifier = Modifier.fillParentMaxSize()) {
                    LoadingCard(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(horizontal = 10.dp),
                        state = loadingState,
                        onClick = if (loadingState is LoadingState.Loading) null
                        else loadMore
                    )
                }
            }
        }

        is LoadingState.Success -> {
            itemsIndexed(
                items = loadingState.response,
                key = { _, item -> item.entityId + item.dateline + item.fuid + item.likeUid },
            ) { index, item ->
                when (val type = item.entityType) {
                    "card" -> when (item.entityTemplate) {
                        "imageCarouselCard_1" -> CarouselCard(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            entities = item.entities,
                            onOpenLink = onOpenLink
                        )

                        "iconLinkGridCard" -> IconLinkGridCard(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            entities = item.entities,
                            onOpenLink = onOpenLink
                        )


                        "iconMiniScrollCard" -> IconMiniScrollCard(
                            data = item,
                            onOpenLink = onOpenLink
                        )

                        "iconMiniGridCard" -> IconMiniGridCard(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            data = item,
                            onOpenLink = onOpenLink
                        )

                        "imageSquareScrollCard" -> ImageSquareScrollCard(
                            entities = item.entities,
                            onOpenLink = onOpenLink
                        )

                        "titleCard" -> TitleCard(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            url = item.url.orEmpty(),
                            title = item.title.orEmpty(),
                            onOpenLink = onOpenLink
                        )

                        "iconScrollCard" -> IconScrollCard(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            data = item,
                            onOpenLink = onOpenLink
                        )

                        "imageTextScrollCard" -> ImageTextScrollCard(
                            data = item,
                            onOpenLink = onOpenLink
                        )

                        "noMoreDataCard" -> TextCard(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            text = item.title.orEmpty()
                        )

                        "messageCard" -> MsgCard(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            msg = item.description.orEmpty(),
                        )

                    }

                    "feed" -> FeedCard(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        data = item,
                        onViewUser = onViewUser,
                        onViewFeed = onViewFeed,
                        isFeedContent = false,
                        onOpenLink = onOpenLink,
                        onCopyText = onCopyText,
                        onReport = onReport,
                        onLike = onLike,
                        onDelete = onDelete,
                        onBlockUser = { uid ->
                            onBlockUser(uid, null)
                        },
                        onViewImage = onViewImage,
                    )

                    "feed_reply" -> {
                        FeedReplyCard(
                            data = item,
                            onViewUser = onViewUser,
                            onShowTotalReply = onShowTotalReply,
                            onOpenLink = onOpenLink,
                            onCopyText = onCopyText,
                            onReport = onReport,
                            isTotalReply = isTotalReply,
                            isTopReply = isTotalReply && index == 0,
                            onLike = onLike,
                            onDelete = onDelete,
                            onBlockUser = onBlockUser,
                            isReply2Reply = if (index == 0) isReply2Reply else false,
                            onReply = onReply,
                            onViewImage = onViewImage,
                        )
                        if (item.fetchType == "feed_reply")
                            HorizontalDivider()
                    }

                    "apk", "product", "user", "topic", "contacts", "recentHistory" -> AppCard(
                        data = item,
                        onOpenLink = onOpenLink,
                        appCardType = when (type) {
                            "apk" -> AppCardType.APP
                            "product" -> AppCardType.PRODUCT
                            "user" -> AppCardType.USER
                            "topic" -> AppCardType.TOPIC
                            "contacts" -> AppCardType.CONTACTS
                            "recentHistory" -> AppCardType.RECENT
                            else -> throw IllegalArgumentException("invalid type: $type")
                        },
                        isHomeFeed = isHomeFeed,
                        onViewUser = onViewUser,
                        onFollowUser = onFollowUser,
                        onHandleRecent = onHandleRecent,
                    )

                    "notification" -> NotificationCard(
                        data = item,
                        onViewUser = onViewUser,
                        onOpenLink = onOpenLink,
                        onReport = onReport,
                        onDeleteNotice = onDeleteNotice,
                    )

                    "message" -> MessageCard(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        data = item,
                        onOpenLink = onOpenLink,
                        onViewUser = onViewUser,
                        onHandleMessage = onHandleMessage,
                        onViewChat = onViewChat,
                    )

                    "collection" -> CollectionCard(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        data = item,
                        onViewFFFList = onViewFFFList,
                    )

                }

                if (index == loadingState.response.lastIndex && !isEnd) {
                    loadMore()
                }
            }
        }
    }

}