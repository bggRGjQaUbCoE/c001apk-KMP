package ui.home.feed

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.dokar.sonner.Toaster
import com.dokar.sonner.rememberToasterState
import constant.Constants.EMPTY_STRING
import logic.datastore.FollowType
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import ui.component.CaptchaDialog
import ui.component.CommonScreen
import ui.component.CreateFeedDialog
import ui.home.TabType
import ui.root.LocalUserPreferences
import util.ReportType

/**
 * Created by bggRGjQaUbCoE on 2024/6/30
 */
@Composable
fun HomeFeedScreen(
    childComponentContext: ComponentContext,
    refreshState: Boolean,
    resetRefreshState: () -> Unit,
    type: TabType,
    onViewUser: (String) -> Unit,
    onViewFeed: (String, Boolean) -> Unit,
    onOpenLink: (String, String?) -> Unit,
    onCopyText: (String?) -> Unit,
    onReport: (String, ReportType) -> Unit,
    isScrollingUp: ((Boolean) -> Unit)? = null,
    onViewImage: (List<String>, Int) -> Unit,
    onCreateFeed: Boolean,
    onResetCreateFeed: () -> Unit,
) {

    val prefs = LocalUserPreferences.current

    val dataListUrl: String? = if (type == TabType.FOLLOW)
        when (prefs.followType) {
            FollowType.ALL -> "/page?url=V9_HOME_TAB_FOLLOW"
            FollowType.USER -> "/page?url=V9_HOME_TAB_FOLLOW&type=circle"
            FollowType.TOPIC -> "/page?url=V9_HOME_TAB_FOLLOW&type=topic"
            FollowType.PRODUCT -> "/page?url=V9_HOME_TAB_FOLLOW&type=product"
            FollowType.APP -> "/page?url=V9_HOME_TAB_FOLLOW&type=apk"
        }
    else null

    val dataListTitle: String? = if (type == TabType.FOLLOW)
        when (prefs.followType) {
            FollowType.ALL -> "全部关注"
            FollowType.USER -> "好友关注"
            FollowType.TOPIC -> "话题关注"
            FollowType.PRODUCT -> "数码关注"
            FollowType.APP -> "应用关注"
        }
    else null

    val viewModel = childComponentContext.instanceKeeper.getOrCreate(key = type.name) {
        koinInject<HomeFeedViewModel> {
            parametersOf(
                type,
                when (type) {
                    TabType.FOLLOW -> dataListUrl.orEmpty()
                    TabType.HOT -> "/page?url=V9_HOME_TAB_RANKING"
                    TabType.COOLPIC -> "/page?url=V11_FIND_COOLPIC"
                    else -> EMPTY_STRING
                },
                when (type) {
                    TabType.FOLLOW -> dataListTitle.orEmpty()
                    TabType.HOT -> "热榜"
                    TabType.COOLPIC -> "酷图"
                    else -> EMPTY_STRING
                },
                prefs.installTime
            )
        }
    }

    LaunchedEffect(prefs.followType) {
        if (type == TabType.FOLLOW) {
            viewModel.dataListUrl = dataListUrl.orEmpty()
            viewModel.dataListTitle = dataListTitle.orEmpty()
        }
    }

    LaunchedEffect(onCreateFeed) {
        if (onCreateFeed) {
            viewModel.onCreateFeed = true
            onResetCreateFeed()
        }
    }

    CommonScreen(
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

    when {
        viewModel.onCreateFeed -> {
            CreateFeedDialog(
                onDismiss = {
                    viewModel.onCreateFeed = false
                },
                onPostCreateFeed = { message ->
                    viewModel.onPostCreateFeed(message = message)
                }
            )
        }
    }

    viewModel.captchaImg?.let {
        CaptchaDialog(
            image = it,
            onDismiss = {
                viewModel.resetCaptcha()
            },
            onValidateCaptcha = { captcha ->
                viewModel.onPostRequestValidate(captcha)
            }
        )
    }

}