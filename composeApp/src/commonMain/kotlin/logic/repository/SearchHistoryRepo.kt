package logic.repository

import kotlinx.coroutines.flow.Flow
import logic.dao.StringEntityDao
import logic.model.StringEntity

class SearchHistoryRepo(
    private val searchHistoryDao: StringEntityDao,
) {

    fun loadAllListFlow(): Flow<List<StringEntity>> {
        return searchHistoryDao.loadAllListFlow()
    }

    suspend fun insertHistory(history: StringEntity) {
        searchHistoryDao.insert(history)
    }

    suspend fun insertList(list: List<StringEntity>) {
        searchHistoryDao.insertList(list)
    }

    suspend fun saveHistory(history: String) {
        searchHistoryDao.insert(StringEntity(history))
    }

    suspend fun deleteHistory(history: String) {
        searchHistoryDao.delete(history)
    }

    suspend fun deleteAllHistory() {
        searchHistoryDao.deleteAll()
    }

    suspend fun isExist(history: String): Boolean {
        return searchHistoryDao.isExist(history)
    }

    suspend fun updateHistory(data: String) {
        searchHistoryDao.updateHistory(data, System.currentTimeMillis())
    }

}