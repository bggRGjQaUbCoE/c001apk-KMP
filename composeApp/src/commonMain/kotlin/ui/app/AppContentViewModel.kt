package ui.app

import kotlinx.coroutines.CoroutineDispatcher
import logic.model.HomeFeedResponse
import logic.repository.BlackListRepo
import logic.repository.NetworkRepo
import ui.base.BaseViewModel


/**
 * Created by bggRGjQaUbCoE on 2024/6/10
 */
class AppContentViewModel(
    val url: String,
    val appCommentTitle: String,
    networkRepo: NetworkRepo,
    dispatcher: CoroutineDispatcher,
    blackListRepo: BlackListRepo,
) : BaseViewModel(networkRepo, blackListRepo, dispatcher) {

    init {
        fetchData()
    }

    override suspend fun customFetchData() =
        networkRepo.getDataList(
            url,
            appCommentTitle,
            null,
            lastItem,
            page
        )

    override fun handleLoadMore(response: List<HomeFeedResponse.Data>): List<HomeFeedResponse.Data> {
        return response.distinctBy { it.entityId }
    }

}