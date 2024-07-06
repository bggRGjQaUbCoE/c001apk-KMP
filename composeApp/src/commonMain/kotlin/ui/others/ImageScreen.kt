package ui.others

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import constant.Constants.EMPTY_STRING
import copyToClipboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import logic.repository.NetworkRepo
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import openInBrowser
import org.koin.compose.koinInject
import ui.component.BackButton
import ui.component.KamelLoader
import ui.component.cards.CardIndicator
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Created by bggRGjQaUbCoE on 2024/7/2
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageScreen(
    onBackClick: () -> Unit,
    picArr: List<String>,
    initialPage: Int = 0,
) {

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = initialPage) { picArr.size }
    var showDialog by remember { mutableStateOf(false) }
    val networkRepo = koinInject<NetworkRepo>()
    val toaster = rememberToasterState()

    fun downloadImage(url: String, index: Int? = null) {
        val name =
            url.substring(url.indexOfLast { char -> char == '/' } + 1)
        val dir = File("images")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File("images/$name")
        val indexText = index?.let { " #$index" } ?: EMPTY_STRING
        if (file.exists()) {
            toaster.show("图片已保存$indexText")
        } else {
            scope.launch(Dispatchers.IO) {
                networkRepo.downloadImage(url)?.let {
                    val result = saveImage(it, "png", file)
                    toaster.show(
                        if (result) "保存成功$indexText"
                        else "保存失败$indexText"
                    )
                } ?: toaster.show("下载失败$indexText")
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().systemBarsPadding()) {

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { index ->
            KamelLoader(
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(
                        state = rememberZoomableState(),
                        onLongClick = {
                            showDialog = true
                        }
                    ),
                url = picArr[index],
                contentScale = ContentScale.Fit,
            )
        }
        if (picArr.size > 1) {
            CardIndicator(
                modifier = Modifier.align(Alignment.BottomCenter).padding(20.dp),
                pagerState = pagerState,
            )
        }
        BackButton(
            modifier = Modifier
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
        ) {
            scope.cancel()
            onBackClick()
        }
        if (picArr.size > 1 && pagerState.currentPage != 0) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterStart).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
        if (picArr.size > 1 && pagerState.currentPage != picArr.lastIndex) {
            IconButton(
                modifier = Modifier.align(Alignment.CenterEnd).clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }

    if (showDialog) {
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
                    ListItem(
                        title = "保存图片",
                        onClick = {
                            showDialog = false
                            downloadImage(picArr[pagerState.currentPage])
                        }
                    )
                    if (picArr.size > 1) {
                        ListItem(
                            title = "保存全部图片",
                            onClick = {
                                showDialog = false
                                picArr.forEachIndexed { index, url ->
                                    downloadImage(url, index + 1)
                                }
                            }
                        )
                    }
                    ListItem(
                        title = "复制图片地址",
                        onClick = {
                            showDialog = false
                            copyToClipboard(picArr[pagerState.currentPage])
                        }
                    )
                    ListItem(
                        title = "浏览器中打开",
                        onClick = {
                            showDialog = false
                            openInBrowser(picArr[pagerState.currentPage])
                        }
                    )
                }
            }
        }
    }

    Toaster(
        state = toaster,
        alignment = Alignment.BottomCenter
    )

}

@Composable
fun ListItem(
    title: String,
    onClick: () -> Unit,
) {
    Text(
        text = title,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        style = MaterialTheme.typography.titleSmall
    )
}

fun saveImage(image: BufferedImage, format: String, outputFile: File): Boolean {
    return try {
        ImageIO.write(image, format, outputFile)
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
