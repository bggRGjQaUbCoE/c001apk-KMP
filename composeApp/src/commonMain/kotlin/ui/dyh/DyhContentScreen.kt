package ui.dyh

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
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
 * Created by bggRGjQaUbCoE on 2024/6/12
 */
@Composable
fun DyhContentScreen(
    childComponentContext: ComponentContext,
    id: String,
    type: String,
    refreshState: Boolean,
    resetRefreshState: () -> Unit,
    paddingValues: PaddingValues,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
    onViewImage: (List<String>, Int) -> Unit,
) {

    val viewModel = childComponentContext.instanceKeeper.getOrCreate(key = id + type) {
        koinInject<DyhContentViewModel> {
            parametersOf(id, type)
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