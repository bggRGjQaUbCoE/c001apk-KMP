package ui.coolpic

import kotlinx.coroutines.CoroutineDispatcher
import logic.model.HomeFeedResponse
import logic.repository.BlackListRepo
import logic.repository.NetworkRepo
import ui.base.BaseViewModel

/**
 * Created by bggRGjQaUbCoE on 2024/6/12
 */
class CoolPicContentViewModel(
    val title: String,
    val type: String,
    networkRepo: NetworkRepo,
    dispatcher: CoroutineDispatcher,
    blackListRepo: BlackListRepo,
) : BaseViewModel(networkRepo, blackListRepo, dispatcher) {

    init {
        fetchData()
    }

    override suspend fun customFetchData() = networkRepo.getCoolPic(title, type, page, lastItem)

    override fun handleLoadMore(response: List<HomeFeedResponse.Data>): List<HomeFeedResponse.Data> {
        return response.distinctBy { it.entityId }
    }

}