package ui.app

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import ui.component.CommonScreen
import util.ReportType

/**
 * Created by bggRGjQaUbCoE on 2024/6/10
 */
@Composable
fun AppContentScreen(
    childComponentContext: ComponentContext,
    refreshState: Boolean,
    resetRefreshState: () -> Unit,
    id: String,
    appCommentSort: String,
    appCommentTitle: String,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
    isScrollingUp: ((Boolean) -> Unit)? = null,
    onViewImage: (List<String>, Int) -> Unit,
) {

    val viewModel = childComponentContext.instanceKeeper.getOrCreate(key = id + appCommentTitle) {
        koinInject<AppContentViewModel> {
            parametersOf(
                "/page?url=/feed/apkCommentList?id=$id$appCommentSort",
                appCommentTitle,
            )
        }
    }

    val windowInsets = WindowInsets.navigationBars.only(WindowInsetsSides.Start)

    CommonScreen(
        modifier = Modifier.windowInsetsPadding(windowInsets),
        viewModel = viewModel,
        refreshState = refreshState,
        resetRefreshState = resetRefreshState,
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