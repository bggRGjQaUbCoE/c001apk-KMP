package ui.home.feed

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import logic.model.HomeFeedResponse
import logic.repository.BlackListRepo
import logic.repository.NetworkRepo
import logic.repository.UserPreferencesRepo
import ui.base.BaseViewModel
import ui.home.TabType
import util.DeviceUtil.regenerateParams

/**
 * Created by bggRGjQaUbCoE on 2024/6/30
 */
class HomeFeedViewModel(
    private val type: TabType = TabType.FEED,
    var dataListUrl: String,
    var dataListTitle: String,
    private var installTime: String,
    networkRepo: NetworkRepo,
    dispatcher: CoroutineDispatcher,
    blackListRepo: BlackListRepo,
    private val userPreferencesRepo: UserPreferencesRepo,
) : BaseViewModel(networkRepo, blackListRepo, dispatcher) {

    init {
        if (installTime.isEmpty()) {
            setInstallTime()
        }
        fetchData()
    }

    private fun setInstallTime() {
        with(System.currentTimeMillis().toString()) {
            installTime = this
            viewModelScope.launch(dispatcher) {
                userPreferencesRepo.setInstallTime(this@with)
                userPreferencesRepo.regenerateParams()
            }
        }
    }

    override suspend fun customFetchData() = when (type) {
        TabType.FOLLOW, TabType.HOT, TabType.COOLPIC ->
            networkRepo.getDataList(dataListUrl, dataListTitle, null, lastItem, page)

        TabType.FEED -> networkRepo.getHomeFeed(page, firstLaunch, installTime, null, null)

        else -> throw IllegalArgumentException("invalid type: ${type.name}")
    }

    override fun handleLoadMore(response: List<HomeFeedResponse.Data>): List<HomeFeedResponse.Data> {
        return response.distinctBy { it.entityId }
    }

}