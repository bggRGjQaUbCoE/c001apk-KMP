package ui.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import logic.model.HomeFeedResponse
import logic.repository.BlackListRepo
import logic.repository.NetworkRepo
import logic.state.LoadingState
import ui.base.BaseViewModel

/**
 * Created by bggRGjQaUbCoE on 2024/6/4
 */
class UserViewModel(
    var uid: String,
    networkRepo: NetworkRepo,
    dispatcher: CoroutineDispatcher,
    blackListRepo: BlackListRepo,
) : BaseViewModel(networkRepo, blackListRepo, dispatcher) {

    var userState by mutableStateOf<LoadingState<HomeFeedResponse.Data>>(LoadingState.Loading)
        private set

    init {
        fetchUserProfile()
    }

    lateinit var username: String
    var isBlocked by mutableStateOf(false)

    private fun fetchUserProfile() {
        viewModelScope.launch(dispatcher) {
            networkRepo.getUserSpace(uid)
                .collect { state ->
                    userState = state
                    if (state is LoadingState.Success) {
                        val response = state.response
                        uid = response.uid.orEmpty()
                        username = response.username.orEmpty()
                        isBlocked = blackListRepo.checkUid(uid)
                        if (isBlocked)
                            loadingState = LoadingState.Error("$username is blocked")
                        else
                            fetchData()
                    }
                    isRefreshing = false
                }
        }
    }

    override suspend fun customFetchData() = networkRepo.getUserFeed(uid, page, lastItem)

    private fun handleBlocked() {
        viewModelScope.launch {
            isRefreshing = true
            delay(50)
            isRefreshing = false
        }
        loadingState = LoadingState.Error("$username is blocked")
    }

    var isPull = false
    override fun refresh() {
        if (!isRefreshing && !isLoadMore) {
            if (userState is LoadingState.Success) {
                if (isBlocked) {
                    handleBlocked()
                } else {
                    page = 1
                    isEnd = false
                    isLoadMore = false
                    isRefreshing = true
                    firstItem = null
                    lastItem = null
                    fetchData()
                }
            } else {
                if (isPull) {
                    isPull = false
                    viewModelScope.launch {
                        isRefreshing = true
                        delay(50)
                        isRefreshing = false
                    }
                }
                userState = LoadingState.Loading
                fetchUserProfile()
            }
        }
    }

    override fun loadMore() {
        if (isBlocked) {
            handleBlocked()
        } else {
            super.loadMore()
        }
    }

    override fun handleFollowResponse(follow: Int): Boolean {
        val response = (userState as LoadingState.Success).response.copy(isFollow = follow)
        userState = LoadingState.Success(response)
        return true
    }

    override fun handleResponse(response: List<HomeFeedResponse.Data>): List<HomeFeedResponse.Data>? {
        isEnd = response.lastOrNull()?.entityTemplate == "noMoreDataCard"
        return null
    }

    override fun onBlockUser(uid: String) {
        viewModelScope.launch(dispatcher) {
            if (isBlocked)
                blackListRepo.deleteUid(uid)
            else
                blackListRepo.saveUid(uid)
            isBlocked = !isBlocked
        }
    }

}