package ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import constant.Constants.SUFFIX_GIF
import util.getImageLp
import util.toThumb

/**
 * Created by bggRGjQaUbCoE on 2024/7/1
 */
@Composable
fun NineImageView(
    modifier: Modifier = Modifier,
    picArr: List<String>,
    onViewImage: (List<String>, Int) -> Unit,
) {
    BoxWithConstraints(modifier = modifier) {
        var imageWidth = (maxWidth - 46.dp) / 3f

        if (picArr.size == 1) {
            val imageLp = getImageLp(picArr[0])
            val width = imageLp.first
            val height = imageLp.second
            imageWidth = with(width / height) {
                if (this > 1.5f) {
                    maxWidth
                } else if (this >= 1f || (height > width && height / width < 1.5f)) {
                    2 * imageWidth
                } else {
                    imageWidth
                }
            }
            val imageHeight = with(height / width) {
                val maxRatio = 22f / 9f
                if (this > maxRatio)
                    imageWidth * maxRatio
                else
                    imageWidth * this
            }
            Box {
                KamelLoader(
                    url = picArr[0].toThumb,
                    modifier = Modifier
                        .width(imageWidth)
                        .height(imageHeight)
                        .clip(MaterialTheme.shapes.medium)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            MaterialTheme.shapes.medium
                        )
                        .clickable {
                            onViewImage(picArr, 0)
                        },
                    contentScale = ContentScale.FillWidth
                )
                ImageBadge(picArr[0])
            }
        } else {
            val column = when (picArr.size) {
                in 1..3 -> 1
                in 4..6 -> 2
                in 7..9 -> 3
                else -> 0
            }
            val space = 5.dp

            LazyVerticalGrid(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .height(imageWidth * column + space * (column - 1)),
                columns = GridCells.Fixed(3)
            ) {
                itemsIndexed(picArr) { index, item ->

                    val startPadding = if (index % 3 == 0) 0.dp else space
                    val topPadding = if (index > 2) space else 0.dp

                    Box(
                        modifier = Modifier
                            .padding(start = startPadding, top = topPadding)
                            .size(imageWidth)
                    ) {
                        KamelLoader(
                            url = item.toThumb,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(MaterialTheme.shapes.medium)
                                .clickable {
                                    onViewImage(picArr, index)
                                }
                                .border(
                                    1.dp,
                                    MaterialTheme.colorScheme.outline,
                                    MaterialTheme.shapes.medium
                                )
                        )
                        ImageBadge(item)
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.ImageBadge(
    url: String
) {
    val imageLp = getImageLp(url)
    val badge =
        if (url.endsWith(SUFFIX_GIF)) "GIF"
        else if (imageLp.second / imageLp.first > 22f / 9f) "长图"
        else null
    badge?.let {
        Text(
            it,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(10.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        )
    }
}