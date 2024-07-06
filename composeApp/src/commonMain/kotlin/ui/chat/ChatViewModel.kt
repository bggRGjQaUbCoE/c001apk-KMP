package ui.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.gson.Gson
import io.ktor.client.request.forms.FormDataContent
import io.ktor.http.Parameters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import logic.model.HomeFeedResponse
import logic.model.OSSUploadPrepareModel
import logic.model.OSSUploadPrepareResponse
import logic.model.StringEntity
import logic.repository.BlackListRepo
import logic.repository.NetworkRepo
import logic.repository.RecentEmojiRepo
import logic.state.LoadingState
import ui.base.BaseViewModel
import java.net.URI

/**
 * Created by bggRGjQaUbCoE on 2024/6/19
 */
class ChatViewModel(
    val ukey: String,
    networkRepo: NetworkRepo,
    blackListRepo: BlackListRepo,
    dispatcher: CoroutineDispatcher,
    private val recentEmojiRepo: RecentEmojiRepo
) : BaseViewModel(networkRepo, blackListRepo, dispatcher) {

    init {
        fetchData()
    }

    override suspend fun customFetchData() =
        networkRepo.messageOperation("/v6/message/chat", ukey, null, page, firstItem, lastItem)

    override fun onBlockUser(uid: String) {
        viewModelScope.launch(dispatcher) {
            blackListRepo.saveUid(uid)
        }
    }

    override fun handleResponse(response: List<HomeFeedResponse.Data>): List<HomeFeedResponse.Data> {
        return response.reversed()
    }

    fun onGetImageUrl(id: String) {
        viewModelScope.launch(dispatcher) {
            networkRepo.getImageUrl(id)
                .collect { result ->
                    val imageUrl = result.getOrNull()
                    if (!imageUrl.isNullOrEmpty()) {
                        var response = (loadingState as LoadingState.Success).response
                        response = response.map { item ->
                            if (item.id == id)
                                item.copy(messagePic = imageUrl)
                            else item
                        }
                        loadingState = LoadingState.Success(response)
                    } else {
                        toastText = result.exceptionOrNull()?.message ?: "response is null"
                        result.exceptionOrNull()?.printStackTrace()
                    }
                }
        }
    }

    fun onDeleteMsg() {
        viewModelScope.launch(dispatcher) {
            networkRepo.deleteMessage("/v6/message/delete", ukey, deleteId)
                .collect { result ->
                    val data = result.getOrNull()
                    if (data != null) {
                        if (!data.message.isNullOrEmpty()) {
                            toastText = data.message
                        } else if (data.data?.count != null) {
                            var response = (loadingState as LoadingState.Success).response
                            response = response.filterNot { it.id == deleteId }
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

    lateinit var deleteId: String
    lateinit var message: String
    lateinit var pic: String
    var scroll by mutableStateOf(false)
        private set

    fun reset() {
        scroll = false
    }

    fun onSendMessage(uid: String, text: String, url: String) {
        showUploadDialog = true
        val postData =FormDataContent(
            Parameters.build {
                append("message", text)
                append("message_pic", url)
            }
        )
        viewModelScope.launch(dispatcher) {
            networkRepo.sendMessage(uid, postData)
                .collect { result ->
                    val data = result.getOrNull()
                    if (data != null) {
                        if (!data.message.isNullOrEmpty()) {
                            toastText = data.message
                        } else if (data.data != null) {
                            val response =
                                (loadingState as LoadingState.Success).response.toMutableList()
                                    .also {
                                        it.addAll(0, data.data)
                                    }
                            loadingState = LoadingState.Success(response)
                            scroll = true
                        }
                    } else {
                        toastText = result.exceptionOrNull()?.message ?: "failed to send message"
                    }
                    showUploadDialog = false
                }
        }
    }

    var uploadImage by mutableStateOf<OSSUploadPrepareResponse.Data?>(null)
        private set

    fun resetUploadImage() {
        uploadImage = null
    }

    lateinit var uriList: List<URI>
    lateinit var typeList: List<String>
    lateinit var md5List: List<ByteArray?>
    var showUploadDialog by mutableStateOf(false)

    fun onPostOSSUploadPrepare(uid: String, imageList: List<OSSUploadPrepareModel>) {
        showUploadDialog = true
        val ossUploadPrepareData = FormDataContent(
            Parameters.build {
                append("uploadBucket", "message")
                append("uploadDir", "message")
                append("is_anonymous", "0")
                append("uploadFileList", Gson().toJson(imageList))
                append("toUid", uid)
            }
        )
        viewModelScope.launch(dispatcher) {
            networkRepo.postOSSUploadPrepare(ossUploadPrepareData)
                .collect { result ->
                    val data = result.getOrNull()
                    if (data != null) {
                        if (!data.message.isNullOrEmpty()) {
                            toastText = data.message
                            showUploadDialog = false
                        } else if (data.data != null) {
                            uploadImage = data.data
                        }
                    } else {
                        showUploadDialog = false
                        toastText = result.exceptionOrNull()?.message ?: "upload prepare failed"
                    }
                }
        }
    }

    val recentEmojiData = recentEmojiRepo.loadAllListFlow()

    fun updateRecentEmoji(data: String, size: Int, last: String?) {
        viewModelScope.launch(dispatcher) {
            if (recentEmojiRepo.checkEmoji(data)) {
                recentEmojiRepo.updateEmoji(data)
            } else {
                if (size == 27)
                    last?.let {
                        recentEmojiRepo.updateEmoji(it, data)
                    }
                else
                    recentEmojiRepo.insertEmoji(StringEntity(data))
            }
        }
    }

}