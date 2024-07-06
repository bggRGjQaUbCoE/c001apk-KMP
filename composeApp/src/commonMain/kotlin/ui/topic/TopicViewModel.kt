package ui.topic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.Parameters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import logic.model.HomeFeedResponse
import logic.repository.BlackListRepo
import logic.repository.NetworkRepo
import logic.state.LoadingState
import ui.base.BaseViewModel

/**
 * Created by bggRGjQaUbCoE on 2024/6/9
 */
class TopicViewModel(
    val url: String,
    val tag: String?,
    var id: String?,
    networkRepo: NetworkRepo,
    dispatcher: CoroutineDispatcher,
    blackListRepo: BlackListRepo,
) : BaseViewModel(networkRepo, blackListRepo, dispatcher) {

    override suspend fun customFetchData(): Flow<LoadingState<List<HomeFeedResponse.Data>>> {
        TODO("Not yet implemented")
    }

    var topicState by mutableStateOf<LoadingState<HomeFeedResponse.Data>>(LoadingState.Loading)
        private set

    init {
        fetchTopicLayout()
    }

    lateinit var entityType: String
    lateinit var title: String
    var selectedTab: String? = null
    var tabList: List<HomeFeedResponse.TabList>? = null

    var isFollowed by mutableStateOf(false)
    var isBlocked by mutableStateOf(false)
        private set

    private fun fetchTopicLayout() {
        viewModelScope.launch(dispatcher) {
            networkRepo.getTopicLayout(url, tag, id)
                .collect { state ->
                    if (state is LoadingState.Success) {
                        val response = state.response
                        id = response.id
                        entityType = response.entityType.orEmpty()
                        title = response.title.orEmpty()
                        tabList = response.tabList
                        selectedTab = response.selectedTab
                        isFollowed = response.userAction?.follow == 1
                        checkIsBlocked(title)
                    }
                    topicState = state
                }
        }
    }

    override fun refresh() {
        topicState = LoadingState.Loading
        fetchTopicLayout()
    }

    fun onGetFollow() {
        val followUrl = if (isFollowed) "/v6/feed/unFollowTag"
        else "/v6/feed/followTag"
        viewModelScope.launch(dispatcher) {
            networkRepo.getFollow(followUrl, title, null)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (!response.message.isNullOrEmpty()) {
                            if (response.message.contains("关注成功"))
                                isFollowed = !isFollowed
                            toastText = response.message
                        }
                    } else {
                        toastText = result.exceptionOrNull()?.message ?: "response is null"
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun onPostFollow() {
        viewModelScope.launch(dispatcher) {
            networkRepo.postLikeDeleteFollow(
                "/v6/product/changeFollowStatus",
                data = FormDataContent(
                    Parameters.build {
                        append("id", id.orEmpty())
                        append("status", if (isFollowed) "0" else "1")
                    })
            )
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (!response.message.isNullOrEmpty()) {
                            if (response.message.contains("成功"))
                                isFollowed = !isFollowed
                            toastText = response.message
                        }
                    } else {
                        toastText = result.exceptionOrNull()?.message ?: "response is null"
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun blockTopic() {
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