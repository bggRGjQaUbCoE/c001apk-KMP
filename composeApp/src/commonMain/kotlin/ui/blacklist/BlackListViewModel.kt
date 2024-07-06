package ui.blacklist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import logic.model.StringEntity
import logic.repository.BlackListRepo

/**
 * Created by bggRGjQaUbCoE on 2024/6/16
 */
class BlackListViewModel(
    val type: BlackListType = BlackListType.USER,
    private val blackListRepo: BlackListRepo,
    private val dispatcher: CoroutineDispatcher,
) : ViewModel() {

    val blackList: Flow<List<StringEntity>> = when (type) {
        BlackListType.USER -> blackListRepo.loadAllUserListFlow()
        BlackListType.TOPIC -> blackListRepo.loadAllTopicListFlow()
    }

    fun clearAll() {
        viewModelScope.launch(dispatcher) {
            when (type) {
                BlackListType.USER -> blackListRepo.deleteAllUser()
                BlackListType.TOPIC -> blackListRepo.deleteAllTopic()
            }
        }
    }

    fun delete(data: String) {
        viewModelScope.launch(dispatcher) {
            when (type) {
                BlackListType.USER -> blackListRepo.deleteUid(data)
                BlackListType.TOPIC -> blackListRepo.deleteTopic(data)
            }
        }
    }

    var toastText by mutableStateOf<String?>(null)
        private set

    fun save(data: String) {
        viewModelScope.launch(dispatcher) {
            when (type) {
                BlackListType.USER -> {
                    if (blackListRepo.checkUid(data))
                        toast()
                    else
                        blackListRepo.insertUid(data)
                }

                BlackListType.TOPIC -> {
                    if (blackListRepo.checkTopic(data))
                        toast()
                    else
                        blackListRepo.insertTopic(data)
                }
            }
        }
    }

    private fun toast() {
        toastText = "已存在"
    }

    fun reset() {
        toastText = null
    }

    fun insertList(list: List<StringEntity>) {
        viewModelScope.launch(dispatcher) {
            when (type) {
                BlackListType.USER -> blackListRepo.insertUidList(list)
                BlackListType.TOPIC -> blackListRepo.insertTopicList(list)
            }
        }
    }

}