package ui.base

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import constant.Constants.entityTemplateList
import constant.Constants.entityTypeList
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.statement.readBytes
import io.ktor.http.Parameters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logic.model.HomeFeedResponse
import logic.repository.BlackListRepo
import logic.repository.NetworkRepo
import logic.state.FooterState
import logic.state.LoadingState
import util.DeviceUtil.showSquare
import util.ViewModelInstance
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

/**
 * Created by bggRGjQaUbCoE on 2024/6/10
 */

enum class LikeType {
    FEED, REPLY
}

abstract class BaseViewModel(
    val networkRepo: NetworkRepo,
    val blackListRepo: BlackListRepo,
    dispatcher: CoroutineDispatcher,
) : ViewModelInstance(dispatcher) {

    var isRefreshing by mutableStateOf(false)

    var loadingState by mutableStateOf<LoadingState<List<HomeFeedResponse.Data>>>(LoadingState.Loading)

    var footerState by mutableStateOf<FooterState>(FooterState.Success)

    var page = 1
    var firstLaunch = 1
    var isLoadMore = false
    var isEnd = false
    var firstItem: String? = null
    var lastItem: String? = null

    abstract suspend fun customFetchData(): Flow<LoadingState<List<HomeFeedResponse.Data>>>

    fun fetchData() {
        viewModelScope.launch(dispatcher) {
            customFetchData().collect { state ->
                when (state) {
                    LoadingState.Empty -> {
                        if (loadingState is LoadingState.Success && !isRefreshing)
                            footerState = FooterState.End
                        else {
                            loadingState = state
                            footerState = FooterState.Success
                        }
                        isEnd = true
                    }

                    is LoadingState.Error -> {
                        if (loadingState is LoadingState.Success)
                            footerState = FooterState.Error(state.errMsg)
                        else
                            loadingState = state
                        isEnd = true
                    }

                    LoadingState.Loading -> {
                        if (loadingState is LoadingState.Success)
                            footerState = FooterState.Loading
                        else
                            loadingState = state
                    }

                    is LoadingState.Success -> {
                        page++
                        var response = state.response.filter {
                            (it.entityType in entityTypeList
                                    || it.entityTemplate in
                                    if (showSquare) entityTemplateList
                                    else entityTemplateList.toMutableList()
                                        .also { list ->
                                            list.removeAll(
                                                listOf(
                                                    "iconMiniScrollCard",
                                                    "iconMiniGridCard"
                                                )
                                            )
                                        })
                                    && !blackListRepo.checkUid(
                                if (!it.fromuid.isNullOrEmpty()) it.fromuid
                                else it.uid.orEmpty()
                            )
                                    && !blackListRepo.checkTopic(
                                it.tags + it.ttitle +
                                        it.relationRows?.getOrNull(0)?.title
                            )
                        }
                        firstItem = response.firstOrNull()?.id
                        lastItem = response.lastOrNull()?.id

                        handleResponse(response)?.let {
                            response = it
                        }

                        if (isLoadMore) {
                            response =
                                ((loadingState as? LoadingState.Success)?.response ?: emptyList()) +
                                        response
                        }

                        handleLoadMore(response)?.let {
                            response = it
                        }

                        loadingState = LoadingState.Success(response)
                        footerState = FooterState.Success
                        if (response.isEmpty()) {
                            isLoadMore = false
                            isRefreshing = false
                            loadMore()
                        }
                    }
                }
                isLoadMore = false
                isRefreshing = false
            }
        }
    }

    open fun handleResponse(response: List<HomeFeedResponse.Data>): List<HomeFeedResponse.Data>? {
        return null
    }

    open fun handleLoadMore(response: List<HomeFeedResponse.Data>): List<HomeFeedResponse.Data>? {
        return null
    }

    open fun refresh() {
        if (!isRefreshing && !isLoadMore) {
            page = 1
            isEnd = false
            isLoadMore = false
            isRefreshing = true
            firstItem = null
            lastItem = null
            fetchData()
        }
    }

    open fun loadMore() {
        if (!isRefreshing && !isLoadMore) {
            isEnd = false
            isLoadMore = true
            fetchData()
            if (loadingState is LoadingState.Success) {
                footerState = FooterState.Loading
            } else {
                loadingState = LoadingState.Loading
            }
        }
    }

    var toastText by mutableStateOf<String?>(null)

    fun resetToastText() {
        toastText = null
    }

    fun onLike(id: String, like: Int, likeType: LikeType) {
        val isLike = when (likeType) {
            LikeType.FEED -> if (like == 1) "unlike" else "like"
            LikeType.REPLY -> if (like == 1) "unLikeReply" else "likeReply"
        }
        val likeUrl = "/v6/feed/$isLike"
        viewModelScope.launch(dispatcher) {
            networkRepo.postLikeDeleteFollow(likeUrl, id = id)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (!response.message.isNullOrEmpty()) {
                            toastText = response.message
                        } else if (response.data != null) {
                            if (handleLikeResponse(id, like, response.data.count) == null) {
                                val dataList = (loadingState as LoadingState.Success).response.map {
                                    if (it.id == id) {
                                        it.copy(
                                            likenum = response.data.count,
                                            userAction = it.userAction?.copy(like = if (like == 1) 0 else 1)
                                        )
                                    } else it
                                }
                                loadingState = LoadingState.Success(dataList)
                            }
                        }
                    } else {
                        toastText = result.exceptionOrNull()?.message ?: "response is null"
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    open fun handleLikeResponse(id: String, like: Int, count: String?): Boolean? {
        return null
    }

    fun onDelete(id: String, deleteType: LikeType) {
        val url = if (deleteType == LikeType.FEED) "/v6/feed/deleteFeed"
        else "/v6/feed/deleteReply"
        viewModelScope.launch(dispatcher) {
            networkRepo.postLikeDeleteFollow(url, id = id)
                .collect { result ->
                    val data = result.getOrNull()
                    if (data != null) {
                        if (!data.message.isNullOrEmpty()) {
                            toastText = data.message
                        } else if (data.data?.count == "删除成功") {
                            var response = (loadingState as LoadingState.Success).response
                            handleDeleteResponse(id, response)?.let {
                                response = it
                                loadingState = LoadingState.Success(response)
                            }
                            toastText = data.data.count
                        }
                    } else {
                        toastText = result.exceptionOrNull()?.message ?: "response is null"
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    open fun handleDeleteResponse(
        id: String,
        response: List<HomeFeedResponse.Data>
    ): List<HomeFeedResponse.Data>? {
        return response.filterNot { it.id == id }
    }

    open fun onBlockUser(uid: String) {
        viewModelScope.launch(dispatcher) {
            blackListRepo.saveUid(uid)

            if (loadingState is LoadingState.Success) {
                var response =
                    (loadingState as LoadingState.Success).response.filterNot { it.uid == uid }
                handleBlockUser(uid, response)?.let {
                    response = it
                }
                loadingState = LoadingState.Success(response)
            }
        }
    }

    open fun handleBlockUser(
        uid: String,
        response: List<HomeFeedResponse.Data>
    ): List<HomeFeedResponse.Data>? {
        return null
    }

    fun onFollowUser(uid: String, isFollow: Int) {
        val url = if (isFollow == 1) "/v6/user/unfollow" else "/v6/user/follow"
        viewModelScope.launch(dispatcher) {
            networkRepo.postLikeDeleteFollow(url, uid = uid)
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (!response.message.isNullOrEmpty()) {
                            toastText = response.message
                        } else {
                            val follow = if (isFollow == 1) 0 else 1
                            if (handleFollowResponse(follow) == null) {
                                val dataList = (loadingState as LoadingState.Success).response.map {
                                    if (it.uid == uid)
                                        it.copy(isFollow = follow)
                                    else it
                                }
                                loadingState = LoadingState.Success(dataList)
                            }
                            toastText = if (follow == 1) "关注成功"
                            else "取消关注成功"
                        }
                    } else {
                        toastText = result.exceptionOrNull()?.message ?: "response is null"
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    open fun handleFollowResponse(follow: Int): Boolean? {
        return null
    }

    var onCreateFeed by mutableStateOf(false)
    fun onPostCreateFeed(
        targetType: String? = null,
        targetId: String? = null,
        message: String,
    ) {
        viewModelScope.launch(dispatcher) {
            networkRepo.postCreateFeed(
                FormDataContent(Parameters.build {
                    append("type", "feed")
                    targetType?.let {
                        append("targetType", it)
                    }
                    targetId?.let {
                        append("targetId", it)
                    }
                    append("message", message)
                })
            )
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data?.id != null) {
                            onCreateFeed = false
                            toastText = "发布成功"
                        } else {
                            toastText = response.message
                            if (response.messageStatus == "err_request_captcha") {
                                onGetValidateCaptcha()
                            }
                        }
                    } else {
                        toastText = result.exceptionOrNull()?.message ?: "response is null"
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    var captchaImg by mutableStateOf<ImageBitmap?>(null)
        private set

    fun resetCaptcha() {
        captchaImg = null
    }

    fun onGetValidateCaptcha() {
        viewModelScope.launch(dispatcher) {
            networkRepo.getValidateCaptcha("/v6/account/captchaImage?${System.currentTimeMillis() / 1000}&w=270=&h=113")
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        val byteArray = response.readBytes()
                        val inputStream = ByteArrayInputStream(byteArray)
                        captchaImg = withContext(dispatcher) {
                            ImageIO.read(inputStream).toComposeImageBitmap()
                        }
                    } else {
                        toastText = result.exceptionOrNull()?.message ?: "response is null"
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun onPostRequestValidate(captcha: String) {
        viewModelScope.launch(dispatcher) {
            networkRepo.postRequestValidate(
                FormDataContent(Parameters.build {
                    append("type", "err_request_captcha")
                    append("code", captcha)
                })
            )
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        if (response.data != null) {
                            toastText = response.data.count
                            if (response.data.count == "验证通过") {
                                /*if (type == "createFeed")
                                    onPostCreateFeed()
                                else
                                    onPostReply()*/
                                toastText = response.data.count
                            }
                        } else if (response.message != null) {
                            toastText = response.message
                            if (response.message == "请输入正确的图形验证码") {
                                onGetValidateCaptcha()
                            }
                        }
                    } else {
                        toastText = result.exceptionOrNull()?.message ?: "response is null"
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

}