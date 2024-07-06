package ui.component.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Message
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.ThumbUpAlt
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import be.digitalia.compose.htmlconverter.htmlToString
import constant.Constants.EMPTY_STRING
import copyToClipboard
import kotlinx.coroutines.launch
import logic.model.HomeFeedResponse
import ui.base.LikeType
import ui.component.IconText
import ui.component.KamelLoader
import ui.component.LinkText
import ui.component.NineImageView
import ui.theme.cardBg
import util.DateUtils.fromToday
import util.DeviceUtil
import util.DeviceUtil.isLogin
import util.ReportType
import util.ShareType
import util.getShareText
import util.longClick

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedCard(
    modifier: Modifier = Modifier,
    isFeedContent: Boolean,
    isFeedTop: Boolean = false,
    data: HomeFeedResponse.Data,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: ((String, ReportType) -> Unit)? = null,
    onLike: ((String, Int, LikeType) -> Unit)? = null,
    onDelete: ((String, LikeType, String?) -> Unit)? = null,
    onBlockUser: (String) -> Unit,
    onViewImage: (List<String>, Int) -> Unit,
) {
    val horizontal = if (isFeedContent) 16.dp else 10.dp
    // val vertical = if (isFeedContent) 12.dp else 10.dp
    Column(
        modifier = run {
            val tmp = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(
                    if (isFeedContent) RectangleShape
                    else RoundedCornerShape(12.0.dp)
                )
                .background(
                    if (isFeedContent) MaterialTheme.colorScheme.surface
                    else cardBg()
                )
            if (isFeedContent)
                tmp.longClick {
                    onCopyText(data.message)
                }
            else
                tmp.combinedClickable(
                    onClick = {
                        onViewFeed(data.id.orEmpty(), false)
                    },
                    onLongClick = {
                        onCopyText(data.message)
                    }
                )
        }
    ) {
        if (!isFeedContent) {
            FeedHeader(
                modifier = Modifier.padding(start = horizontal),
                data = data,
                onViewUser = onViewUser,
                isFeedContent = false,
                onReport = onReport,
                isFeedTop = isFeedTop,
                onDelete = onDelete,
                onBlockUser = onBlockUser,
            )
        }
        FeedMessage(
            modifier = Modifier
                .padding(horizontal = horizontal)
                .fillMaxWidth(),
            data = data,
            onOpenLink = onOpenLink,
            isFeedContent = isFeedContent,
            onViewFeed = onViewFeed,
            onCopyText = onCopyText,
            onClick = {
                if (!isFeedContent) {
                    onViewFeed(data.id.orEmpty(), false)
                }
            },
            onViewImage = onViewImage,
        )
        FeedBottomInfo(
            modifier = Modifier
                .padding(horizontal = horizontal)
                .padding(bottom = if (data.targetRow == null && data.relationRows.isNullOrEmpty()) 12.dp else 0.dp),
            isFeedContent = isFeedContent,
            ip = data.ipLocation.orEmpty(),
            dateline = data.dateline ?: 0,
            replyNum = data.replynum.orEmpty(),
            likeNum = data.likenum.orEmpty(),
            onViewFeed = {
                onViewFeed(data.id.orEmpty(), true)
            },
            onLike = {
                if (isLogin) {
                    onLike?.let {
                        it(data.id.orEmpty(), data.userAction?.like ?: 0, LikeType.FEED)
                    }
                }
            },
            like = data.userAction?.like
        )
        FeedRows(
            modifier = Modifier.padding(bottom = 10.dp),
            isFeedContent = isFeedContent,
            relationRows = data.relationRows,
            targetRow = data.targetRow,
            onOpenLink = onOpenLink
        )
    }
}

@Composable
fun FeedRows(
    modifier: Modifier = Modifier,
    isFeedContent: Boolean,
    relationRows: List<HomeFeedResponse.RelationRows>?,
    targetRow: HomeFeedResponse.TargetRow?,
    onOpenLink: (String, String?) -> Unit
) {
    val dataList = relationRows?.toMutableList() ?: ArrayList()
    targetRow?.let {
        dataList.add(
            0,
            HomeFeedResponse.RelationRows(
                it.id.orEmpty(),
                it.logo,
                it.title,
                it.url,
                it.targetType.toString()
            )
        )
    }

    if (dataList.isNotEmpty()) {
        val scrollState = rememberLazyListState()
        val scope = rememberCoroutineScope()
        LazyRow(
            state = scrollState,
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { detail ->
                        scope.launch {
                            scrollState.scrollBy(-detail)
                        }
                    }
                ),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = if (isFeedContent) 16.dp else 10.dp)
        ) {
            dataList.forEach {
                item(key = it.url) {
                    IconMiniScrollCardItem(
                        isFeedContent = isFeedContent,
                        logoUrl = it.logo.orEmpty(),
                        linkUrl = it.url.orEmpty(),
                        titleText = it.title.orEmpty(),
                        onOpenLink = onOpenLink
                    )
                }
            }
        }
    }

}

@Composable
fun FeedBottomInfo(
    modifier: Modifier = Modifier,
    isFeedContent: Boolean,
    ip: String,
    dateline: Long,
    replyNum: String,
    likeNum: String,
    onViewFeed: () -> Unit,
    onLike: () -> Unit,
    like: Int?,
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = if (isFeedContent) {
                if (ip.isNotEmpty()) "发布于 $ip"
                else EMPTY_STRING
            } else fromToday(dateline),
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp),
            color = MaterialTheme.colorScheme.outline
        )

        IconText(
            imageVector = Icons.AutoMirrored.Outlined.Message,
            title = replyNum,
            onClick = onViewFeed,
        )

        IconText(
            modifier = Modifier.padding(start = 10.dp),
            imageVector = if (like == 1) Icons.Filled.ThumbUpAlt
            else Icons.Default.ThumbUpOffAlt,
            title = likeNum,
            onClick = onLike,
            isLike = like == 1,
        )
    }

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeedMessage(
    modifier: Modifier = Modifier,
    data: HomeFeedResponse.Data,
    onOpenLink: (String, String?) -> Unit,
    isFeedContent: Boolean,
    onViewFeed: (String, Boolean) -> Unit,
    onCopyText: (String?) -> Unit,
    onClick: () -> Unit,
    onViewImage: (List<String>, Int) -> Unit,
) {

    if (!data.messageTitle.isNullOrEmpty()) {
        LinkText(
            text = data.messageTitle,
            modifier = modifier.padding(top = 10.dp),
            onOpenLink = onOpenLink,
            onClick = onClick,
        )
    }
    if (!data.message.isNullOrEmpty()) {
        LinkText(
            text = data.message,
            modifier = modifier.padding(top = 10.dp),
            onOpenLink = { url, title ->
                if (url.isEmpty()) {
                    onClick()
                } else {
                    onOpenLink(url, title)
                }
            },
            onClick = onClick,
            onViewImage = {
                onViewImage(data.picArr ?: emptyList(), 0)
            }
        )
    }

    if (!data.picArr.isNullOrEmpty()) {
        NineImageView(
            modifier = modifier.padding(top = 10.dp),
            picArr = data.picArr,
            onViewImage = onViewImage,
        )

        /*NineImageView(
            modifier = modifier.padding(top = 10.dp),
            pic = data.pic,
            picArr = data.picArr,
            feedType = data.feedType
        )*/
    }

    if (!data.forwardSourceType.isNullOrEmpty()) {
        if (data.forwardSourceFeed == null) {
            Text(
                text = "动态已被删除",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = modifier
                    .padding(top = 10.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        if (isFeedContent)
                            cardBg()
                        else
                            MaterialTheme.colorScheme.surface
                    )
                    .padding(10.dp)
            )
        } else {
            Column(
                modifier = modifier
                    .padding(top = 10.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(
                        if (isFeedContent)
                            cardBg()
                        else
                            MaterialTheme.colorScheme.surface
                    )
                    .combinedClickable(
                        onClick = {
                            onOpenLink(data.forwardSourceFeed.url.orEmpty(), null)
                        },
                        onLongClick = {
                            onCopyText(data.forwardSourceFeed.message)
                        }
                    )
                    .padding(10.dp)
            ) {
                if (!data.forwardSourceFeed.messageTitle.isNullOrEmpty()) {
                    LinkText(
                        text = """<a class="feed-link-uname" href="/u/${data.forwardSourceFeed.uid}">@${data.forwardSourceFeed.username}</a>: ${data.forwardSourceFeed.messageTitle}""",
                        onOpenLink = { url, title ->
                            if (url.isEmpty()) {
                                onOpenLink(data.forwardSourceFeed.url.orEmpty(), null)
                            } else {
                                onOpenLink(url, title)
                            }
                        },
                        onClick = {
                            onOpenLink(data.forwardSourceFeed.url.orEmpty(), null)
                        },
                        onViewImage = {
                            onViewImage(data.forwardSourceFeed.picArr ?: emptyList(), 0)
                        }
                    )
                    if (!data.forwardSourceFeed.message.isNullOrEmpty())
                        LinkText(
                            text = data.forwardSourceFeed.message,
                            onOpenLink = { url, title ->
                                if (url.isEmpty()) {
                                    onOpenLink(data.forwardSourceFeed.url.orEmpty(), null)
                                } else {
                                    onOpenLink(url, title)
                                }
                            },
                            onClick = {
                                onOpenLink(data.forwardSourceFeed.url.orEmpty(), null)
                            },
                            onViewImage = {
                                onViewImage(data.forwardSourceFeed.picArr ?: emptyList(), 0)
                            }
                        )
                } else {
                    LinkText(
                        text = """<a class="feed-link-uname" href="/u/${data.forwardSourceFeed.uid}">@${data.forwardSourceFeed.username}</a>: ${data.forwardSourceFeed.message}""",
                        onOpenLink = { url, title ->
                            if (url.isEmpty()) {
                                onOpenLink(data.forwardSourceFeed.url.orEmpty(), null)
                            } else {
                                onOpenLink(url, title)
                            }
                        },
                        onClick = {
                            onOpenLink(data.forwardSourceFeed.url.orEmpty(), null)
                        },
                        onViewImage = {
                            onViewImage(data.forwardSourceFeed.picArr ?: emptyList(), 0)
                        }
                    )
                }
                if (!data.forwardSourceFeed.picArr.isNullOrEmpty()) {
                    NineImageView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        picArr = data.forwardSourceFeed.picArr,
                        onViewImage = onViewImage,
                    )
                    /*NineImageView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        pic = data.forwardSourceFeed.pic,
                        picArr = data.forwardSourceFeed.picArr,
                        feedType = data.forwardSourceFeed.feedType
                    )*/
                }
            }
        }
    }

    if (!data.extraUrl.isNullOrEmpty()) {
        ConstraintLayout(
            modifier = modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(
                    if (isFeedContent)
                        cardBg()
                    else
                        MaterialTheme.colorScheme.surface
                )
                .clickable {
                    onOpenLink(data.extraUrl, data.extraTitle)
                }
                .padding(10.dp)
        ) {
            val (pic, title, url) = createRefs()

            if (!data.extraPic.isNullOrEmpty()) {
                KamelLoader(
                    url = data.extraPic,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .constrainAs(pic) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            height = Dimension.fillToConstraints
                        })
            } else {
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .constrainAs(pic) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            height = Dimension.fillToConstraints
                        }
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.onPrimary)
                    )
                }
            }

            if (!data.extraTitle.isNullOrEmpty()) {
                Text(
                    text = data.extraTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .constrainAs(title) {
                            start.linkTo(pic.end)
                            top.linkTo(parent.top)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        }
                )
            }

            Text(
                text = data.extraUrl,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                modifier = Modifier
                    .padding(start = 10.dp)
                    .constrainAs(url) {
                        start.linkTo(pic.end)
                        top.linkTo(if (!data.extraTitle.isNullOrEmpty()) title.bottom else parent.top)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
            )
        }
    }

    if (!data.replyRows.isNullOrEmpty()) {
        val reply = data.replyRows?.getOrNull(0)
        val replyPic = when (reply?.pic) {
            EMPTY_STRING -> EMPTY_STRING
            else -> """ <a class=\"feed-forward-pic\" href=${reply?.pic}>查看图片(${reply?.picArr?.size})</a>"""
        }
        LinkText(
            text = """<a class="feed-link-uname" href="/u/${reply?.uid}">${reply?.username}</a>: ${reply?.message}$replyPic""",
            onOpenLink = { url, title ->
                if (url.isEmpty()) {
                    onViewFeed(data.id.orEmpty(), true)
                } else {
                    onOpenLink(url, title)
                }
            },
            onClick = {
                onViewFeed(data.id.orEmpty(), true)
            },
            modifier = modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surface)
                .combinedClickable(
                    onClick = {
                        onViewFeed(data.id.orEmpty(), true)
                    },
                    onLongClick = {
                        onCopyText(reply?.message)
                    }
                )
                .padding(10.dp),
            onViewImage = {
                onViewImage(reply?.picArr ?: emptyList(), 0)
            },
            onShowTotalReply = {
                onViewFeed(data.id.orEmpty(), true)
            }
        )
    }

}

@Composable
fun FeedHeader(
    modifier: Modifier = Modifier,
    data: HomeFeedResponse.Data,
    onViewUser: (String) -> Unit,
    isFeedContent: Boolean,
    isFeedTop: Boolean,
    onReport: ((String, ReportType) -> Unit)? = null,
    onDelete: ((String, LikeType, String?) -> Unit)? = null,
    onBlockUser: ((String) -> Unit)? = null,
) {

    val vertical = if (isFeedContent) 12.dp else 10.dp
    var dropdownMenuExpanded by remember { mutableStateOf(false) }

    ConstraintLayout(modifier = modifier.fillMaxWidth()) {
        val (avatar, username, from, device, expand, stickTop) = createRefs()

        KamelLoader(
            url = data.userInfo?.userAvatar,
            modifier = Modifier
                .padding(top = vertical)
                .clip(CircleShape)
                .aspectRatio(1f, false)
                .constrainAs(avatar) {
                    top.linkTo(username.top)
                    bottom.linkTo(device.bottom)
                    start.linkTo(parent.start)
                    height = Dimension.fillToConstraints
                }
                .clickable {
                    onViewUser(data.uid.orEmpty())
                },
        )

        Text(
            modifier = Modifier
                .padding(
                    start = 10.dp,
                    top = vertical,
                    end = if (isFeedTop) 0.dp else if (!isFeedContent) 10.dp else 16.dp
                )
                .constrainAs(username) {
                    start.linkTo(avatar.end)
                    top.linkTo(parent.top)
                    end.linkTo(
                        if (isFeedContent) parent.end
                        else if (data.isStickTop == 1) stickTop.start
                        else expand.start
                    )
                    width = Dimension.fillToConstraints
                },
            text = data.userInfo?.username.orEmpty(),
            style = MaterialTheme.typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (!isFeedContent && data.isStickTop == 1) {
            Text(
                text = "置顶",
                modifier = Modifier
                    .padding(top = 10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                    .constrainAs(stickTop) {
                        top.linkTo(username.top)
                        bottom.linkTo(username.bottom)
                        end.linkTo(expand.start)
                    }
                    .padding(horizontal = 6.dp),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleSmall.copy(fontSize = 10.sp)
            )
        }

        if (isFeedContent || !data.infoHtml.isNullOrEmpty()) {
            Text(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .constrainAs(from) {
                        start.linkTo(avatar.end)
                        top.linkTo(username.bottom)
                    },
                text = if (isFeedContent) fromToday(data.dateline ?: 0)
                else htmlToString(data.infoHtml.orEmpty()),
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconText(
            modifier = Modifier
                .padding(
                    start = 10.dp,
                    end = if (isFeedTop) 0.dp else if (!isFeedContent) 10.dp else 16.dp
                )
                .constrainAs(device) {
                    start.linkTo(if (isFeedContent || !data.infoHtml.isNullOrEmpty()) from.end else avatar.end)
                    top.linkTo(username.bottom)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                },
            imageVector = Icons.Default.Smartphone,
            title = htmlToString(data.deviceTitle.orEmpty()),
            textSize = 13f,
            isConstraint = true,
        )

        if (!isFeedContent) {
            Box(
                modifier = Modifier
                    .aspectRatio(1f, false)
                    .constrainAs(expand) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                        bottom.linkTo(device.bottom)
                        height = Dimension.fillToConstraints
                    }) {

                IconButton(
                    onClick = {
                        dropdownMenuExpanded = true
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline
                    )
                }

                DropdownMenu(
                    expanded = dropdownMenuExpanded,
                    onDismissRequest = {
                        dropdownMenuExpanded = false
                    },
                ) {
                    listOf("Copy", "Block").forEachIndexed { index, menu ->
                        DropdownMenuItem(
                            text = { Text(menu) },
                            onClick = {
                                dropdownMenuExpanded = false
                                when (index) {
                                    0 -> copyToClipboard(
                                        getShareText(ShareType.FEED, data.id.orEmpty())
                                    )

                                    1 -> onBlockUser?.let { it(data.uid.orEmpty()) }
                                }
                            }
                        )
                    }
                    /*if (isLogin) {
                        DropdownMenuItem(
                            text = { Text("Report") },
                            onClick = {
                                dropdownMenuExpanded = false
                                onReport?.let { it(data.id.orEmpty(), ReportType.FEED) }
                            }
                        )
                    }*/
                    if (data.uid == DeviceUtil.uid) {
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                dropdownMenuExpanded = false
                                onDelete?.let { it(data.id.orEmpty(), LikeType.FEED, null) }
                            }
                        )
                    }
                }

            }
        }

    }

}