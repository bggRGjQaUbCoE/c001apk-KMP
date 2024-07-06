package ui.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import constant.Constants.EMPTY_STRING
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import logic.model.HomeFeedResponse
import logic.repository.BlackListRepo
import logic.repository.NetworkRepo
import logic.state.LoadingState
import openInBrowser
import ui.base.BaseViewModel

/**
 * Created by bggRGjQaUbCoE on 2024/6/10
 */
class AppViewModel(
    var packageName: String,
    networkRepo: NetworkRepo,
    dispatcher: CoroutineDispatcher,
    blackListRepo: BlackListRepo,
) : BaseViewModel(networkRepo, blackListRepo, dispatcher) {

    override suspend fun customFetchData(): Flow<LoadingState<List<HomeFeedResponse.Data>>> {
        TODO("Not yet implemented")
    }

    lateinit var id: String
    var title: String = EMPTY_STRING
    lateinit var versionName: String
    lateinit var versionCode: String
    lateinit var commentStatusText: String
    var commentStatus: Int = -1

    var isFollowed by mutableStateOf(false)
    var isBlocked by mutableStateOf(false)
        private set

    var appState by mutableStateOf<LoadingState<HomeFeedResponse.Data>>(LoadingState.Loading)
        private set

    init {
        if (packageName.isNotEmpty()) {
            fetchAppInfo()
        }
    }

    private fun fetchAppInfo() {
        viewModelScope.launch(dispatcher) {
            networkRepo.getAppInfo(packageName)
                .collect { state ->
                    if (state is LoadingState.Success) {
                        val response = state.response
                        id = response.id.orEmpty()
                        title = response.title.orEmpty()
                        versionName = response.apkversionname.orEmpty()
                        versionCode = response.apkversioncode.orEmpty()
                        commentStatus = response.commentStatus ?: -1
                        commentStatusText = response.commentStatusText.orEmpty()
                        isFollowed = response.userAction?.follow == 1
                        checkIsBlocked(response.title.orEmpty())
                    }
                    appState = state
                }
        }
    }

   override fun refresh() {
        appState = LoadingState.Loading
        fetchAppInfo()
    }

    fun onGetDownloadLink() {
        viewModelScope.launch(dispatcher) {
            networkRepo.getAppDownloadLink(packageName, id, versionCode)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        response.headers["Location"]?.let {
                            openInBrowser(it)
                        }
                    } else {
                        toastText =
                            result.exceptionOrNull()?.message ?: "failed to get download url"
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun onGetFollowApk() {
        val followUrl = if (isFollowed) "/v6/apk/unFollow"
        else "/v6/apk/follow"
        viewModelScope.launch(dispatcher) {
            networkRepo.getFollow(followUrl, null, id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (!response.message.isNullOrEmpty()) {
                            toastText = response.message
                        } else if (response.data?.follow != null) {
                            toastText = if (isFollowed) "取消关注成功"
                            else "关注成功"
                            isFollowed = !isFollowed
                        }
                    } else {
                        toastText = result.exceptionOrNull()?.message ?: "response is null"
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun blockApp() {
        viewModelScope.launch(dispatcher) {
            if (isBlocked)
                blackListRepo.deleteTopic(title)
            else
                blackListRepo.saveTopic(title)
            isBlocked = !isBlocked
        }
    }

    private fun checkIsBlocked(title: String) {
        viewModelScope.launch(dispatcher) {
            isBlocked = blackListRepo.checkTopic(title)
        }
    }

}