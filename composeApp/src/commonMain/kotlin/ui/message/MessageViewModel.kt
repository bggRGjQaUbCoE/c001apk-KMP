package ui.message

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import constant.Constants.EMPTY_STRING
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import logic.repository.BlackListRepo
import logic.repository.NetworkRepo
import logic.repository.UserPreferencesRepo
import logic.state.FooterState
import logic.state.LoadingState
import ui.base.BaseViewModel
import util.DeviceUtil.atcommentme
import util.DeviceUtil.atme
import util.DeviceUtil.contacts_follow
import util.DeviceUtil.feedlike
import util.DeviceUtil.isLogin
import util.DeviceUtil.message
import util.DeviceUtil.uid
import util.encode

/**
 * Created by bggRGjQaUbCoE on 2024/6/11
 */
class MessageViewModel(
    val url: String,
    networkRepo: NetworkRepo,
    dispatcher: CoroutineDispatcher,
    blackListRepo: BlackListRepo,
    private val userPreferencesRepo: UserPreferencesRepo,
) : BaseViewModel(networkRepo, blackListRepo, dispatcher) {

    var fffList by mutableStateOf<List<String>>(emptyList())
        private set
    var badgeList by mutableStateOf(listOf(atme, atcommentme, feedlike, contacts_follow, message))
        private set

    init {
        loadingState = LoadingState.Success(emptyList())
    }

    override suspend fun customFetchData() =
        networkRepo.getMessage(url, page, lastItem)

    private fun fetchProfile() {
        viewModelScope.launch(dispatcher) {
            networkRepo.getProfile(uid)
                .collect { result ->
                    val data = result.getOrNull()
                    if (data?.data != null) {
                        fffList = listOf(
                            data.data.feed?.id ?: "0",
                            data.data.follow ?: "0",
                            data.data.fans ?: "0"
                        )
                        userPreferencesRepo.apply {
                            setUserAvatar(data.data.userAvatar.orEmpty())
                            setUsername(data.data.username.encode)
                            setLevel(data.data.level ?: "0")
                            setExperience(data.data.experience ?: "0")
                            setNextLevelExperience(data.data.nextLevelExperience ?: "0")
                        }
                    } else {
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun onLogout() {
        viewModelScope.launch {
            userPreferencesRepo.apply {
                setUid(EMPTY_STRING)
                setUserAvatar(EMPTY_STRING)
                setUsername(EMPTY_STRING)
                setToken(EMPTY_STRING)
                setIsLogin(false)
            }
            fffList = emptyList()
            badgeList = listOf(null)
            loadingState = LoadingState.Success(emptyList())
            footerState = FooterState.Success
        }
    }

    private fun onCheckCount() {
        viewModelScope.launch(dispatcher) {
            networkRepo.checkCount()
                .collect { result ->
                    result.getOrNull()?.data?.let {
                        badgeList = listOf(
                            it.atme, it.atcommentme, it.feedlike, it.contactsFollow, it.message
                        )
                    }
                }
        }
    }

    override fun refresh() {
        if (isLogin && !isRefreshing && !isLoadMore) {
            page = 1
            isEnd = false
            isLoadMore = false
            isRefreshing = true
            firstItem = null
            lastItem = null
            fetchProfile()
            onCheckCount()
            fetchData()
        } else {
            viewModelScope.launch {
                isRefreshing = true
                delay(50)
                isRefreshing = false
            }
        }
    }

    fun clearBadge(index: Int) {
        badgeList = badgeList.mapIndexed { i, count ->
            if (index == i) null
            else count
        }
    }

    lateinit var deleteId: String
    fun onPostDelete() {
        viewModelScope.launch(dispatcher) {
            networkRepo.postLikeDeleteFollow("/v6/notification/delete", id = deleteId)
                .collect { result ->
                    val data = result.getOrNull()
                    if (data != null) {
                        if (!data.message.isNullOrEmpty()) {
                            toastText = data.message
                        } else if (data.data?.count?.contains("成功") == true) {
                            var response = (loadingState as LoadingState.Success).response
                            response = response.filterNot { it.id == deleteId }
                            loadingState = LoadingState.Success(response)
                            toastText = data.data.count
                        }
                    } else {
                        toastText = result.exceptionOrNull()?.message ?: "response is null"
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

}