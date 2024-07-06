package ui.notification

import constant.Constants.EMPTY_STRING
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import logic.repository.BlackListRepo
import logic.repository.NetworkRepo
import logic.state.LoadingState
import ui.base.BaseViewModel
import ui.ffflist.FFFContentViewModel

/**
 * Created by bggRGjQaUbCoE on 2024/6/13
 */
class NoticeViewModel(
    val url: String,
    networkRepo: NetworkRepo,
    dispatcher: CoroutineDispatcher,
    blackListRepo: BlackListRepo,
) : BaseViewModel(networkRepo, blackListRepo, dispatcher) {

    init {
        fetchData()
    }

    override suspend fun customFetchData() = networkRepo.getMessage(url, page, lastItem)

    var isTop = 0
    lateinit var ukey: String

    fun onHandleMessage(actionType: FFFContentViewModel.ActionType) {
        val url = when (actionType) {
            FFFContentViewModel.ActionType.TOP -> "/v6/message/${if (isTop == 1) "removeTop" else "addTop"}"
            FFFContentViewModel.ActionType.DELETE -> "/v6/message/deleteChat"
            FFFContentViewModel.ActionType.DELETE_ALL -> EMPTY_STRING
        }
        viewModelScope.launch(dispatcher) {
            networkRepo.deleteMessage(url, ukey, null)
                .collect { result ->
                    val data = result.getOrNull()
                    if (data != null) {
                        if (!data.message.isNullOrEmpty()) {
                            toastText = data.message
                        } else if (data.data?.count?.contains("成功") == true) {
                            var response = (loadingState as LoadingState.Success).response
                            response = when (actionType) {
                                FFFContentViewModel.ActionType.TOP -> {
                                    if (isTop == 0) {
                                        val actionItem = response.find { it.ukey == ukey }
                                        response.toMutableList().also {
                                            it.remove(actionItem)
                                            actionItem?.let { item ->
                                                item.isTop = 1
                                                it.add(0, item)
                                            }
                                        }
                                    } else {
                                        response.map {
                                            if (it.ukey == ukey) it.copy(isTop = 0)
                                            else it
                                        }
                                    }
                                }

                                FFFContentViewModel.ActionType.DELETE -> response.filterNot { it.ukey == ukey }
                                FFFContentViewModel.ActionType.DELETE_ALL -> response
                            }
                            loadingState = LoadingState.Success(response)
                            toastText = data.data.count
                        }
                    } else {
                        toastText = result.exceptionOrNull()?.message ?: "response is null"
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun resetUnRead(ukey: String) {
        viewModelScope.launch(dispatcher) {
            var response = (loadingState as LoadingState.Success).response
            response = response.map { item ->
                if (item.ukey == ukey)
                    item.copy(unreadNum = 0)
                else
                    item
            }
            loadingState = LoadingState.Success(response)
        }
    }

}