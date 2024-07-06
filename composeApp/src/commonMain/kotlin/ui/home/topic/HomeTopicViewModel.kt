package ui.home.topic

import kotlinx.coroutines.CoroutineDispatcher
import logic.repository.BlackListRepo
import logic.repository.NetworkRepo
import ui.base.BaseViewModel


/**
 * Created by bggRGjQaUbCoE on 2024/6/11
 */
class HomeTopicViewModel(
    val url: String,
    networkRepo: NetworkRepo,
    dispatcher: CoroutineDispatcher,
    blackListRepo: BlackListRepo,
) : BaseViewModel(networkRepo, blackListRepo, dispatcher) {

    init {
        fetchData()
    }

    override suspend fun customFetchData() = networkRepo.getProductList(url)

}
