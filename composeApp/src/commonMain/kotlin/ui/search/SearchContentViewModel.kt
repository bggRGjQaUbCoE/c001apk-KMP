package ui.search

import kotlinx.coroutines.CoroutineDispatcher
import logic.model.HomeFeedResponse
import logic.repository.BlackListRepo
import logic.repository.NetworkRepo
import ui.base.BaseViewModel

/**
 * Created by bggRGjQaUbCoE on 2024/6/9
 */
class SearchContentViewModel(
    val type: String,
    val keyword: String,
    val pageType: String?,
    var pageParam: String?,
    networkRepo: NetworkRepo,
    dispatcher: CoroutineDispatcher,
    blackListRepo: BlackListRepo,
) : BaseViewModel(networkRepo, blackListRepo, dispatcher) {

    var feedType: String = "all"
    var sort: String = "default" //hot // reply

    var searchFeedType = SearchFeedType.ALL
    var sortType = SearchOrderType.DATELINE

    init {
        fetchData()
    }

    override suspend fun customFetchData() =
        networkRepo.getSearch(
            type, feedType, sort, keyword,
            pageType, pageParam, page, lastItem
        )

    override fun handleLoadMore(response: List<HomeFeedResponse.Data>): List<HomeFeedResponse.Data> {
        return response.distinctBy { it.entityId }
    }

}