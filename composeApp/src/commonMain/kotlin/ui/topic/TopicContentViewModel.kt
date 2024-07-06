package ui.topic

import constant.Constants.EMPTY_STRING
import kotlinx.coroutines.CoroutineDispatcher
import logic.model.HomeFeedResponse
import logic.repository.BlackListRepo
import logic.repository.NetworkRepo
import ui.base.BaseViewModel

/**
 * Created by bggRGjQaUbCoE on 2024/6/9
 */
class TopicContentViewModel(
    var url: String,
    var title: String,
    networkRepo: NetworkRepo,
    dispatcher: CoroutineDispatcher,
    blackListRepo: BlackListRepo,
) : BaseViewModel(networkRepo, blackListRepo, dispatcher) {

    var sortType = ProductSortType.REPLY

    init {
        fetchData()
    }

    override suspend fun customFetchData() =
        networkRepo.getDataList(url, title, EMPTY_STRING, lastItem, page)

    override fun handleLoadMore(response: List<HomeFeedResponse.Data>): List<HomeFeedResponse.Data> {
        return response.distinctBy { it.entityId }
    }

}