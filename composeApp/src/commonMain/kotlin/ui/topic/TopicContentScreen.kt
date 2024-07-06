package ui.topic

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import ui.component.CommonScreen
import util.ReportType

/**
 * Created by bggRGjQaUbCoE on 2024/6/9
 */
@Composable
fun TopicContentScreen(
    childComponentContext: ComponentContext,
    refreshState: Boolean,
    resetRefreshState: () -> Unit,
    entityType: String,
    id: String?,
    url: String,
    title: String,
    sortType: ProductSortType,
    paddingValues: PaddingValues,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
    isScrollingUp: ((Boolean) -> Unit)? = null,
    onViewImage: (List<String>, Int) -> Unit,
) {

    val viewModel = childComponentContext.instanceKeeper.getOrCreate(key = id + url + title) {
        koinInject<TopicContentViewModel> {
            parametersOf(url, title)
        }
    }

    if (entityType == "product" && title == "讨论") {
        LaunchedEffect(sortType) {
            if (sortType != viewModel.sortType) {
                viewModel.sortType = sortType
                viewModel.title = when (sortType) {
                    ProductSortType.REPLY -> "最近回复"
                    ProductSortType.HOT -> "热度排序"
                    ProductSortType.DATELINE -> "最新发布"
                }
                viewModel.url = "/page?url=/product/feedList?type=feed&id=$id&" + when (sortType) {
                    ProductSortType.REPLY -> "ignoreEntityById=1"
                    ProductSortType.HOT -> "listType=rank_score"
                    ProductSortType.DATELINE -> "ignoreEntityById=1&listType=dateline_desc"
                }
                viewModel.refresh()
            }
        }
    }

    CommonScreen(
        viewModel = viewModel,
        refreshState = refreshState,
        resetRefreshState = resetRefreshState,
        paddingValues = paddingValues,
        onViewUser = onViewUser,
        onViewFeed = onViewFeed,
        onOpenLink = onOpenLink,
        onCopyText = onCopyText,
        onReport = onReport,
        isScrollingUp = isScrollingUp,
        onViewImage = onViewImage,
    )

    val toaster = rememberToasterState()
    Toaster(
        state = toaster,
        alignment = Alignment.BottomCenter
    )

    viewModel.toastText?.let {
        viewModel.resetToastText()
        toaster.show(it)
    }

}