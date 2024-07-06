package logic.repository

import kotlinx.coroutines.flow.Flow
import logic.dao.StringEntityDao
import logic.model.StringEntity

class BlackListRepo(
    private val userBlackListDao: StringEntityDao,
    private val topicBlackListDao: StringEntityDao,
) {

    fun loadAllUserListFlow(): Flow<List<StringEntity>> {
        return userBlackListDao.loadAllListFlow()
    }

    suspend fun insertUid(uid: String) {
        userBlackListDao.insert(StringEntity(uid))
    }

    suspend fun insertUidList(list: List<StringEntity>) {
        userBlackListDao.insertList(list)
    }

    suspend fun checkUid(uid: String): Boolean {
        return userBlackListDao.isExist(uid)
    }

    suspend fun saveUid(uid: String) {
        if (!userBlackListDao.isExist(uid)) {
            userBlackListDao.insert(StringEntity(uid))
        }
    }

    suspend fun deleteUid(uid: String) {
        userBlackListDao.delete(uid)
    }

    suspend fun deleteAllUser() {
        userBlackListDao.deleteAll()
    }

    fun loadAllTopicListFlow(): Flow<List<StringEntity>> {
        return topicBlackListDao.loadAllListFlow()
    }

    suspend fun insertTopic(topic: String) {
        topicBlackListDao.insert(StringEntity(topic))
    }

    suspend fun insertTopicList(list: List<StringEntity>) {
        topicBlackListDao.insertList(list)
    }

    suspend fun checkTopic(topic: String): Boolean {
        return topicBlackListDao.isContain(topic)
    }

    suspend fun saveTopic(topic: String) {
        if (!topicBlackListDao.isExist(topic)) {
            topicBlackListDao.insert(StringEntity(topic))
        }
    }

    suspend fun deleteTopic(topic: String) {
        topicBlackListDao.delete(topic)
    }

    suspend fun deleteAllTopic() {
        topicBlackListDao.deleteAll()
    }

}