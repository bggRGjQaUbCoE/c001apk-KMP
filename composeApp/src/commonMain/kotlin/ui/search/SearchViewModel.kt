package ui.search

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import logic.model.StringEntity
import logic.repository.SearchHistoryRepo
import util.ViewModelInstance

/**
 * Created by bggRGjQaUbCoE on 2024/6/16
 */
class SearchViewModel(
    private val searchHistoryRepo: SearchHistoryRepo,
    dispatcher: CoroutineDispatcher,
) : ViewModelInstance(dispatcher) {

    val searchHistory: Flow<List<StringEntity>> = searchHistoryRepo.loadAllListFlow()

    fun saveHistory(keyword: String) {
        viewModelScope.launch(dispatcher) {
            if (searchHistoryRepo.isExist(keyword)) {
                searchHistoryRepo.updateHistory(keyword)
            } else {
                searchHistoryRepo.saveHistory(keyword)
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch(dispatcher) {
            searchHistoryRepo.deleteAllHistory()
        }
    }

    fun delete(keyword: String) {
        viewModelScope.launch(dispatcher) {
            searchHistoryRepo.deleteHistory(keyword)
        }
    }

}