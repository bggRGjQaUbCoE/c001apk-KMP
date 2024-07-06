package logic.repository

import constant.Constants.EMPTY_STRING
import constant.Constants.LOADING_FAILED
import constant.Constants.URL_ACCOUNT_SERVICE
import constant.Constants.URL_API2_SERVICE
import constant.Constants.URL_API_SERVICE
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import logic.model.CheckResponse
import logic.model.CreateFeedResponse
import logic.model.FeedContentResponse
import logic.model.HomeFeedResponse
import logic.model.LikeResponse
import logic.model.OSSUploadPrepareResponse
import logic.state.LoadingState
import util.createRandomNumber
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

/**
 * Created by bggRGjQaUbCoE on 2024/6/27
 */
class NetworkRepo(
    private val httpClient: HttpClient,
    private val dispatcher: CoroutineDispatcher,
) {

    suspend fun getHomeFeed(
        page: Int,
        firstLaunch: Int,
        installTime: String,
        firstItem: String?,
        lastItem: String?
    ): Flow<LoadingState<List<HomeFeedResponse.Data>>> = flowList {
        val url = "/v6/main/indexV8"
        httpClient.get("$URL_API2_SERVICE$url") {
            parameter("page", page)
            parameter("firstLaunch", firstLaunch)
            parameter("installTime", installTime)
            parameter("firstItem", firstItem)
            parameter("lastItem", lastItem)
        }.body<HomeFeedResponse>()
    }

    suspend fun getDataList(
        url: String,
        title: String,
        firstItem: String?,
        lastItem: String?,
        page: Int,
    ): Flow<LoadingState<List<HomeFeedResponse.Data>>> = flowList {
        val baseUrl = "/v6/page/dataList"
        httpClient.get("$URL_API2_SERVICE$baseUrl") {
            parameter("url", url)
            parameter("title", title)
            parameter("firstItem", firstItem)
            parameter("lastItem", lastItem)
            parameter("page", page)
        }.body<HomeFeedResponse>()
    }

    suspend fun getFeedContentReply(
        id: String,
        listType: String,
        page: Int,
        firstItem: String?,
        lastItem: String?,
        discussMode: Int,
        feedType: String,
        blockStatus: Int,
        fromFeedAuthor: Int
    ): Flow<LoadingState<List<HomeFeedResponse.Data>>> = flowList {
        val url = "/v6/feed/replyList"
        httpClient.get("$URL_API2_SERVICE$url") {
            parameter("id", id)
            parameter("listType", listType)
            parameter("page", page)
            parameter("firstItem", firstItem)
            parameter("lastItem", lastItem)
            parameter("discussMode", discussMode)
            parameter("feedType", feedType)
            parameter("blockStatus", blockStatus)
            parameter("fromFeedAuthor", fromFeedAuthor)
        }.body<HomeFeedResponse>()
    }

    suspend fun getFeedContent(
        url: String,
    ): Flow<LoadingState<HomeFeedResponse.Data>> = flowData {
        httpClient.get("$URL_API_SERVICE$url").body<FeedContentResponse>()
    }

    suspend fun postLikeDeleteFollow(
        url: String,
        id: String? = null,
        uid: String? = null,
        data: FormDataContent? = null,
    ): Flow<Result<LikeResponse>> = fire {
        httpClient.post("$URL_API_SERVICE$url") {
            parameter("id", id)
            parameter("uid", uid)
            contentType(ContentType.Application.FormUrlEncoded)
            data?.let {
                setBody(it)
            }
        }.body<LikeResponse>()
    }

    suspend fun getFollow(
        url: String,
        tag: String?,
        id: String?,
    ): Flow<Result<LikeResponse>> = fire {
        httpClient.get("$URL_API_SERVICE$url") {
            parameter("tag", tag)
            parameter("id", id)
        }.body<LikeResponse>()
    }

    suspend fun getAppInfo(
        id: String,
        installed: Int = 1,
    ): Flow<LoadingState<HomeFeedResponse.Data>> = flowData {
        val url = "/v6/apk/detail"
        httpClient.post("$URL_API_SERVICE$url") {
            contentType(ContentType.Application.FormUrlEncoded)
            parameter("id", id)
            parameter("installed", installed)
        }.body<FeedContentResponse>()
    }

    suspend fun getAppDownloadLink(
        id: String,
        aid: String,
        vc: String,
        extra: String = "",
    ): Flow<Result<HttpResponse>> = fire {
        val url = "/v6/apk/download"
        httpClient.post("$URL_API_SERVICE$url") {
            parameter("id", id)
            parameter("aid", aid)
            parameter("vc", vc)
            parameter("extra", extra)
            contentType(ContentType.Application.FormUrlEncoded)
        }.body()
    }

    suspend fun getTopicLayout(
        url: String,
        tag: String?, // topic
        id: String? // product
    ): Flow<LoadingState<HomeFeedResponse.Data>> = flowData {
        httpClient.get("$URL_API2_SERVICE$url") {
            parameter("tag", tag)
            parameter("id", id)
        }.body<FeedContentResponse>()
    }

    suspend fun getSearch(
        type: String,
        feedType: String,
        sort: String,
        keyword: String,
        pageType: String?,
        pageParam: String?,
        page: Int,
        lastItem: String?,
        showAnonymous: Int = -1
    ): Flow<LoadingState<List<HomeFeedResponse.Data>>> = flowList {
        val url = "/v6/search"
        httpClient.get("$URL_API_SERVICE$url") {
            parameter("type", type)
            parameter("feedType", feedType)
            parameter("sort", sort)
            parameter("searchValue", keyword)
            parameter("pageType", pageType)
            parameter("pageParam", pageParam)
            parameter("page", page)
            parameter("lastItem", lastItem)
            parameter("showAnonymous", showAnonymous)
        }.body<HomeFeedResponse>()
    }

    suspend fun getCoolPic(
        tag: String,
        type: String,
        page: Int,
        lastItem: String?
    ): Flow<LoadingState<List<HomeFeedResponse.Data>>> = flowList {
        val url = "/v6/picture/list"
        httpClient.get("$URL_API_SERVICE$url") {
            parameter("tag", tag)
            parameter("type", type)
            parameter("page", page)
            parameter("lastItem", lastItem)
        }.body<HomeFeedResponse>()
    }

    suspend fun getDyhDetail(
        dyhId: String,
        type: String,
        page: Int,
        lastItem: String?
    ): Flow<LoadingState<List<HomeFeedResponse.Data>>> = flowList {
        val url = "/v6/dyhArticle/list"
        httpClient.get("$URL_API_SERVICE$url") {
            parameter("dyhId", dyhId)
            parameter("type", type)
            parameter("page", page)
            parameter("lastItem", lastItem)
        }.body<HomeFeedResponse>()
    }

    suspend fun getUserSpace(
        uid: String,
    ): Flow<LoadingState<HomeFeedResponse.Data>> = flowData {
        val url = "/v6/user/space"
        httpClient.get("$URL_API_SERVICE$url") {
            parameter("uid", uid)
        }.body<FeedContentResponse>()
    }

    suspend fun getUserFeed(
        uid: String,
        page: Int,
        lastItem: String?,
        showAnonymous: Int = 0,
        isIncludeTop: Int = 0,
        showDoing: Int = 1,
    ): Flow<LoadingState<List<HomeFeedResponse.Data>>> = flowList {
        val url = "/v6/user/feedList"
        httpClient.get("$URL_API_SERVICE$url") {
            parameter("uid", uid)
            parameter("page", page)
            parameter("lastItem", lastItem)
            parameter("showAnonymous", showAnonymous)
            parameter("isIncludeTop", isIncludeTop)
            parameter("showDoing", showDoing)
        }.body<HomeFeedResponse>()
    }

    suspend fun checkLoginInfo(
    ): Flow<Result<HttpResponse>> = fire {
        val url = "/v6/account/checkLoginInfo"
        httpClient.get("$URL_API_SERVICE$url").body()
    }

    suspend fun getProductList(
        url: String
    ): Flow<LoadingState<List<HomeFeedResponse.Data>>> = flowList {
        httpClient.get("$URL_API_SERVICE$url").body<HomeFeedResponse>()
    }

    suspend fun getMessage(
        url: String,
        page: Int,
        lastItem: String?
    ): Flow<LoadingState<List<HomeFeedResponse.Data>>> = flowList {
        httpClient.get("$URL_API_SERVICE$url") {
            parameter("page", page)
            parameter("lastItem", lastItem)
        }.body<HomeFeedResponse>()
    }

    suspend fun getProfile(
        uid: String,
    ): Flow<Result<FeedContentResponse>> = fire {
        val url = "/v6/user/profile"
        httpClient.get("$URL_API_SERVICE$url") {
            parameter("uid", uid)
        }.body<FeedContentResponse>()
    }

    suspend fun checkCount(
    ): Flow<Result<CheckResponse>> = fire {
        val url = "/v6/notification/checkCount"
        httpClient.get("$URL_API_SERVICE$url").body<CheckResponse>()
    }

    suspend fun getLoginParam(
        url: String,
    ): Flow<Result<HttpResponse>> = fire {
        httpClient.get("$URL_ACCOUNT_SERVICE$url").body<HttpResponse>()
    }

    suspend fun getCaptcha(
        url: String,
    ): Flow<Result<HttpResponse>> = fire {
        httpClient.get("$URL_ACCOUNT_SERVICE$url").body<HttpResponse>()
    }

    suspend fun getValidateCaptcha(
        url: String,
    ): Flow<Result<HttpResponse>> = fire {
        httpClient.get("$URL_API_SERVICE$url").body<HttpResponse>()
    }

    suspend fun postRequestValidate(
        data: FormDataContent
    ): Flow<Result<LikeResponse>> = fire {
        val url = "/v6/account/requestValidate"
        httpClient.post("$URL_API_SERVICE$url") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(data)
        }.body<LikeResponse>()
    }

    suspend fun tryLogin(
        requestHash: String,
        login: String,
        password: String,
        captcha: String,
    ): Flow<Result<HttpResponse>> = fire {
        val url = "/auth/loginByCoolApk"
        httpClient.post("$URL_ACCOUNT_SERVICE$url") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(
                FormDataContent(Parameters.build {
                    append("submit", "1")
                    append("randomNumber", createRandomNumber())
                    append("requestHash", requestHash)
                    append("login", login)
                    append("password", password)
                    append("captcha", captcha)
                    append("code", EMPTY_STRING)
                })
            )
        }.body<HttpResponse>()
    }

    suspend fun deleteMessage(
        url: String,
        ukey: String?,
        id: String?,
    ): Flow<Result<LikeResponse>> = fire {
        httpClient.get("$URL_API_SERVICE$url") {
            parameter("ukey", ukey)
            parameter("id", id)
        }.body<LikeResponse>()
    }

    suspend fun getFollowList(
        url: String,
        uid: String?,
        id: String?,
        showDefault: Int?,
        page: Int,
        lastItem: String?
    ): Flow<LoadingState<List<HomeFeedResponse.Data>>> = flowList {
        httpClient.get("$URL_API_SERVICE$url") {
            parameter("uid", uid)
            parameter("id", id)
            parameter("showDefault", showDefault)
            parameter("page", page)
            parameter("lastItem", lastItem)
        }.body<HomeFeedResponse>()
    }

    suspend fun downloadImage(url: String): BufferedImage? {
        return withContext(dispatcher) {
            try {
                val bytes: ByteArray = httpClient.get(url).readBytes()
                ImageIO.read(ByteArrayInputStream(bytes))
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun postReply(
        data: FormDataContent,
        id: String,
        type: String
    ): Flow<Result<FeedContentResponse>> = fire {
        val url = "/v6/feed/reply"
        httpClient.post("$URL_API_SERVICE$url") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(data)
            parameter("id", id)
            parameter("type", type)
        }.body<FeedContentResponse>()
    }

    suspend fun postCreateFeed(
        data: FormDataContent,
    ): Flow<Result<CreateFeedResponse>> = fire {
        val url = "/v6/feed/createFeed"
        httpClient.post("$URL_API_SERVICE$url") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(data)
        }.body<CreateFeedResponse>()
    }

    suspend fun messageOperation(
        url: String,
        ukey: String?,
        uid: String?,
        page: Int?,
        firstItem: String?,
        lastItem: String?,
    ): Flow<LoadingState<List<HomeFeedResponse.Data>>> = flowList {
        httpClient.get("$URL_API_SERVICE$url") {
            parameter("ukey", ukey)
            parameter("uid", uid)
            parameter("page", page)
            parameter("firstItem", firstItem)
            parameter("lastItem", lastItem)
        }.body<HomeFeedResponse>()
    }

    suspend fun getImageUrl(
        id: String,
        type: String = "s",
    ): Flow<Result<String?>> = fire {
        val url = "/v6/message/showImage"
        httpClient.get("$URL_API_SERVICE$url") {
            parameter("id", id)
            parameter("type", type)
        }.body<HttpResponse>().headers["Location"]
    }

    suspend fun postOSSUploadPrepare(
        data: FormDataContent
    ): Flow<Result<OSSUploadPrepareResponse>> = fire {
        val url = "/v6/upload/ossUploadPrepare"
        httpClient.post("$URL_API_SERVICE$url") {
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(data)
        }.body<OSSUploadPrepareResponse>()
    }

    suspend fun sendMessage(
        uid: String,
        data: FormDataContent
    ): Flow<Result<HomeFeedResponse>> = fire {
        val url = "/v6/message/send"
        httpClient.post("$URL_API_SERVICE$url") {
            parameter("uid", uid)
            contentType(ContentType.Application.FormUrlEncoded)
            setBody(data)
        }.body<HomeFeedResponse>()
    }

    private fun <T> fire(block: suspend () -> T) = flow {
        val result = try {
            val response = block()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
        emit(result)
    }.flowOn(dispatcher)

    private fun flowList(block: suspend () -> HomeFeedResponse) = flow {
        val result = try {
            val response = block()
            if (!response.message.isNullOrEmpty()) {
                LoadingState.Error(response.message)
            } else if (!response.data.isNullOrEmpty()) {
                LoadingState.Success(response.data)
            } else if (response.data?.isEmpty() == true) {
                LoadingState.Empty
            } else {
                LoadingState.Error(LOADING_FAILED)
            }
        } catch (e: Exception) {
            LoadingState.Error(e.message ?: "unknown error")
        }
        emit(result)
    }.flowOn(dispatcher)

    private fun flowData(block: suspend () -> FeedContentResponse) = flow {
        val result = try {
            val response = block()
            if (!response.message.isNullOrEmpty()) {
                LoadingState.Error(response.message)
            } else if (response.data != null) {
                LoadingState.Success(response.data)
            } else {
                LoadingState.Error(LOADING_FAILED)
            }
        } catch (e: Exception) {
            LoadingState.Error(e.message ?: "unknown error")
        }
        emit(result)
    }.flowOn(dispatcher)

}