package logic.repository

import kotlinx.coroutines.flow.Flow
import logic.dao.HistoryFavoriteDao
import logic.model.FeedEntity

class HistoryFavoriteRepo (
    private val browseHistoryDao: HistoryFavoriteDao,
    private val feedFavoriteDao: HistoryFavoriteDao,
) {

    fun loadAllHistoryListFlow(): Flow<List<FeedEntity>> {
        return browseHistoryDao.loadAllListFlow()
    }

    suspend fun insertHistory(history: FeedEntity) {
        browseHistoryDao.insert(history)
    }

    suspend fun checkHistory(id: String): Boolean {
        return browseHistoryDao.isExist(id)
    }

    suspend fun saveHistory(
        id: String,
        uid: String,
        uname: String,
        avatar: String,
        device: String,
        message: String,
        pubDate: String
    ) {
        if (!browseHistoryDao.isExist(id))
            browseHistoryDao.insert(
                FeedEntity(
                    id,
                    uid,
                    uname,
                    avatar,
                    device,
                    message,
                    pubDate
                )
            )
    }

    suspend fun deleteHistory(id: String) {
        browseHistoryDao.delete(id)
    }

    suspend fun deleteAllHistory() {
        browseHistoryDao.deleteAll()
    }

    fun loadAllFavoriteListFlow(): Flow<List<FeedEntity>> {
        return feedFavoriteDao.loadAllListFlow()
    }

    suspend fun insertFavorite(favorite: FeedEntity) {
        feedFavoriteDao.insert(favorite)
    }

    suspend fun checkFavorite(id: String): Boolean {
        return feedFavoriteDao.isExist(id)
    }

    suspend fun saveFavorite(
        id: String,
        uid: String,
        uname: String,
        avatar: String,
        device: String,
        message: String,
        pubDate: String
    ) {
        if (!feedFavoriteDao.isExist(id))
            feedFavoriteDao.insert(
                FeedEntity(
                    id,
                    uid,
                    uname,
                    avatar,
                    device,
                    message,
                    pubDate
                )
            )
    }

    suspend fun deleteFavorite(id: String) {
        feedFavoriteDao.delete(id)
    }

    suspend fun deleteAllFavorite() {
        feedFavoriteDao.deleteAll()
    }

    suspend fun deleteHistoryByUid(uid: String){
        browseHistoryDao.deleteByUid(uid)
    }
    
    suspend fun deleteFavByUid(uid: String){
        feedFavoriteDao.deleteByUid(uid)
    }

}