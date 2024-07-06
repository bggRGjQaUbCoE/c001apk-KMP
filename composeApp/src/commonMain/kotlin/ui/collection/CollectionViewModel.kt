package ui.collection

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import constant.Constants.EMPTY_STRING
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import logic.model.HomeFeedResponse
import logic.repository.BlackListRepo
import logic.repository.NetworkRepo
import logic.state.LoadingState
import ui.base.BaseViewModel

/**
 * Created by bggRGjQaUbCoE on 2024/6/21
 */
class CollectionViewModel(
    val id: String,
    networkRepo: NetworkRepo,
    dispatcher: CoroutineDispatcher,
    blackListRepo: BlackListRepo,
) : BaseViewModel(networkRepo, blackListRepo, dispatcher) {

    override suspend fun customFetchData() =
        networkRepo.getFollowList("/v6/collection/itemList", null, id, null, page, lastItem)

    init {
        fetchInfo()
    }

    var collectionState by mutableStateOf<LoadingState<HomeFeedResponse.Data>>(LoadingState.Loading)
        private set

    var title by mutableStateOf(EMPTY_STRING)

    var isPull = false
    override fun refresh() {
        if (!isRefreshing && !isLoadMore) {
            if (collectionState is LoadingState.Success) {
                page = 1
                isEnd = false
                isLoadMore = false
                isRefreshing = true
                firstItem = null
                lastItem = null
                fetchData()
            } else {
                if (isPull) {
                    isPull = false
                    viewModelScope.launch {
                        isRefreshing = true
                        delay(50)
                        isRefreshing = false
                    }
                }
                collectionState = LoadingState.Loading
                fetchInfo()
            }
        }
    }

    private fun fetchInfo() {
        viewModelScope.launch(dispatcher) {
            networkRepo.getFeedContent("/v6/collection/detail?id=$id")
                .collect { state ->
                    collectionState = state
                    if (state is LoadingState.Success) {
                        title = state.response.title.orEmpty()
                        fetchData()
                    }
                    isRefreshing = false
                }
        }
    }

}