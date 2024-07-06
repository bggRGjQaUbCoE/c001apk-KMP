package ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import logic.model.FeedArticleContentBean
import logic.model.HomeFeedResponse
import ui.component.cards.FeedArticleCard
import ui.component.cards.FeedBottomInfo
import ui.component.cards.FeedHeader
import ui.component.cards.FeedRows

/**
 * Created by bggRGjQaUbCoE on 2024/6/17
 */
fun LazyListScope.ArticleItem(
    response: HomeFeedResponse.Data,
    articleList: List<FeedArticleContentBean>,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onLike: () -> Unit,
    onViewUser: (String) -> Unit,
    onViewImage: (List<String>, Int) -> Unit,
) {

    item(key = "header") {
        FeedHeader(
            modifier = Modifier.padding(horizontal = 16.dp),
            data = response,
            onViewUser = onViewUser,
            isFeedContent = true,
            isFeedTop = false,
        )
    }

    if (!response.messageCover.isNullOrEmpty()) {
        item(key = "cover") {
            KamelLoader(
                url = response.messageCover,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .clickable {
                        onViewImage(listOf(response.messageCover), 0)
                    }
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.medium
                    ),
                contentScale = ContentScale.FillWidth,
            )
            /*NineImageView(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
                pic = null,
                picArr = listOf(response.messageCover),
                feedType = null,
                isSingle = true
            )*/
        }
    }
    if (!response.title.isNullOrEmpty()) {
        item(key = "title") {
            Text(
                text = response.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }

    items(
        items = articleList,
        key = { item -> item.key },
    ) { item ->
        FeedArticleCard(
            item = item,
            onOpenLink = onOpenLink,
            onCopyText = onCopyText,
            onViewImage = { url ->
                onViewImage(response.picArr ?: emptyList(), response.picArr?.indexOf(url) ?: 0)
            },
        )
    }

    item(key = "bottom") {
        FeedBottomInfo(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = if (response.targetRow == null && response.relationRows.isNullOrEmpty()) 12.dp else 0.dp),
            isFeedContent = true,
            ip = response.ipLocation.orEmpty(),
            dateline = response.dateline ?: 0,
            replyNum = response.replynum.orEmpty(),
            likeNum = response.likenum.orEmpty(),
            onViewFeed = {},
            onLike = onLike,
            like = response.userAction?.like,
        )
    }

    item(key = "rows") {
        FeedRows(
            modifier = Modifier.padding(bottom = 12.dp),
            isFeedContent = true,
            relationRows = response.relationRows,
            targetRow = response.targetRow,
            onOpenLink = onOpenLink
        )
    }

}