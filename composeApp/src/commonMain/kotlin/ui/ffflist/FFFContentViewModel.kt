package ui.ffflist

import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.Parameters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import logic.repository.BlackListRepo
import logic.repository.NetworkRepo
import logic.state.LoadingState
import ui.base.BaseViewModel

/**
 * Created by bggRGjQaUbCoE on 2024/6/12
 */
class FFFContentViewModel(
    val url: String,
    val uid: String?,
    val id: String?,
    val showDefault: Int?,
    networkRepo: NetworkRepo,
    dispatcher: CoroutineDispatcher,
    blackListRepo: BlackListRepo,
) : BaseViewModel(networkRepo, blackListRepo, dispatcher) {

    init {
        fetchData()
    }

    override suspend fun customFetchData() =
        networkRepo.getFollowList(url, uid, id, showDefault, page, lastItem)

    enum class ActionType {
        TOP, DELETE, DELETE_ALL
    }

    var isTop = 0
    lateinit var actionId: String
    lateinit var targetId: String
    lateinit var targetType: String

    fun onHandleRecentHistory(actionType: ActionType) {
        val url = "/v6/user/" + when (actionType) {
            ActionType.TOP -> if (isTop == 0) "addToTop" else "removeFromTop"
            ActionType.DELETE -> "delete"
            ActionType.DELETE_ALL -> "clear"
        } + "RecentHistory"

        val postData = when (actionType) {
            ActionType.TOP -> if (isTop == 1) FormDataContent(Parameters.build {
                append("id", actionId)
            })
            else
                FormDataContent(Parameters.build {
                    append("targetId", targetId)
                    append("targetType", targetType)
                })

            ActionType.DELETE -> FormDataContent(Parameters.build {
                append("id", actionId)
            })

            ActionType.DELETE_ALL -> FormDataContent(Parameters.build {})
        }
        viewModelScope.launch(dispatcher) {
            networkRepo.postLikeDeleteFollow(url, data = postData)
                .collect { result ->
                    val data = result.getOrNull()
                    if (data != null) {
                        if (!data.message.isNullOrEmpty()) {
                            toastText = data.message
                        } else if (data.data?.count?.contains("成功") == true) {
                            var response = (loadingState as LoadingState.Success).response
                            response = when (actionType) {
                                ActionType.TOP -> {
                                    if (isTop == 0) {
                                        val actionItem = response.find { it.targetId == targetId }
                                        response.toMutableList().also {
                                            it.remove(actionItem)
                                            actionItem?.let { item ->
                                                item.isTop = 1
                                                it.add(0, item)
                                            }
                                        }
                                    } else {
                                        response.map {
                                            if (it.targetId == targetId) it.copy(isTop = 0)
                                            else it
                                        }
                                    }
                                }

                                ActionType.DELETE -> response.filterNot { item -> item.targetId == targetId }
                                ActionType.DELETE_ALL -> emptyList()
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

}