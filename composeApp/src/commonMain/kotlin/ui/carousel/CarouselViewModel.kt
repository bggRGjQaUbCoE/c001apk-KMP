package ui.carousel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import constant.Constants.entityTemplateList
import constant.Constants.entityTypeList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import logic.model.HomeFeedResponse
import logic.repository.BlackListRepo
import logic.repository.NetworkRepo
import logic.state.LoadingState
import ui.base.BaseViewModel

/**
 * Created by bggRGjQaUbCoE on 2024/6/11
 */
class CarouselViewModel(
    val isInit: Boolean,
    val url: String,
    val title: String,
    networkRepo: NetworkRepo,
    dispatcher: CoroutineDispatcher,
    blackListRepo: BlackListRepo,
) : BaseViewModel(networkRepo, blackListRepo, dispatcher) {

    init {
        if (isInit)
            preFetchData()
        else
            fetchData()
    }

    var pageTitle by mutableStateOf("")
        private set

    private fun preFetchData() {
        viewModelScope.launch(dispatcher) {
            networkRepo.getDataList(url, title, null, lastItem, page)
                .collect { state ->
                    loadingState = if (state is LoadingState.Success) {
                        page++
                        pageTitle = state.response.lastOrNull()?.extraDataArr?.pageTitle ?: title
                        val response = state.response.filter {
                            it.entityType in entityTypeList || it.entityTemplate in entityTemplateList
                        } // TODO
                        firstItem = response.firstOrNull()?.id
                        lastItem = response.lastOrNull()?.id
                        LoadingState.Success(response)
                    } else state
                }
        }
    }

    override suspend fun customFetchData() =
        networkRepo.getDataList(url, title, null, lastItem, page)

    override fun handleLoadMore(response: List<HomeFeedResponse.Data>): List<HomeFeedResponse.Data> {
        return response.distinctBy { it.entityId }
    }

}