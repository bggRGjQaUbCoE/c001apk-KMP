package ui.component.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import logic.model.FeedArticleContentBean
import ui.component.KamelLoader
import ui.component.LinkText
import util.longClick

/**
 * Created by bggRGjQaUbCoE on 2024/6/8
 */
@Composable
fun FeedArticleCard(
    item: FeedArticleContentBean,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String) -> Unit,
    onViewImage: (String) -> Unit,
) {

    when (item.type) {
        "text" -> {
            LinkText(
                text = item.message.orEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .longClick {
                        onCopyText(item.message.orEmpty())
                    }
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
                onOpenLink = onOpenLink
            )
        }

        "image" -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp)
            ) {
                KamelLoader(
                    url = item.url,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable {
                            onViewImage(item.url.orEmpty())
                        }
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            MaterialTheme.shapes.medium
                        ),
                    contentScale = ContentScale.FillWidth,
                )
                /*NineImageView(
                    pic = null,
                    picArr = listOf(item.url.orEmpty()),
                    feedType = null,
                    isSingle = true
                )*/
                if (!item.description.isNullOrEmpty()) {
                    Text(
                        text = item.description,
                        style = MaterialTheme.typography.titleSmall.copy(fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .align(Alignment.CenterHorizontally),
                    )
                }
            }
        }

        "shareUrl" -> {
            Text(
                text = item.title.orEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp))
                    .clickable {
                        onOpenLink(item.url.orEmpty(), item.title)
                    }
                    .padding(10.dp),
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp)
            )
        }
    }
}