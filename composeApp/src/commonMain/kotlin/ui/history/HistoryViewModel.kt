package ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import logic.repository.BlackListRepo
import logic.repository.HistoryFavoriteRepo

/**
 * Created by bggRGjQaUbCoE on 2024/6/17
 */
class HistoryViewModel (
    val type: HistoryType = HistoryType.HISTORY,
    private val blackListRepo: BlackListRepo,
    private val historyFavoriteRepo: HistoryFavoriteRepo,
    private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    val dataList = when (type) {
        HistoryType.FAV -> historyFavoriteRepo.loadAllFavoriteListFlow()
        HistoryType.HISTORY -> historyFavoriteRepo.loadAllHistoryListFlow()
    }

    fun blockUser(uid: String) {
        viewModelScope.launch(dispatcher) {
            if (!blackListRepo.checkUid(uid)) {
                blackListRepo.saveUid(uid)
            }
            when (type) {
                HistoryType.FAV -> historyFavoriteRepo.deleteFavByUid(uid)
                HistoryType.HISTORY -> historyFavoriteRepo.deleteHistoryByUid(uid)
            }
        }
    }

    fun delete(id: String) {
        viewModelScope.launch(dispatcher) {
            when (type) {
                HistoryType.FAV -> historyFavoriteRepo.deleteFavorite(id)
                HistoryType.HISTORY -> historyFavoriteRepo.deleteHistory(id)
            }
        }
    }

    fun clearAll() {
        viewModelScope.launch(dispatcher) {
            when (type) {
                HistoryType.FAV -> historyFavoriteRepo.deleteAllFavorite()
                HistoryType.HISTORY -> historyFavoriteRepo.deleteAllHistory()
            }
        }
    }

}