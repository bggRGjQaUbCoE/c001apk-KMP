package ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import constant.Constants.EMPTY_STRING
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readBytes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import logic.model.LoginResponse
import logic.repository.NetworkRepo
import logic.repository.UserPreferencesRepo
import org.jsoup.Jsoup
import util.DeviceUtil.SESSID
import util.DeviceUtil.isGetCaptcha
import util.DeviceUtil.isGetLoginParam
import util.DeviceUtil.isPreGetLoginParam
import util.DeviceUtil.isTryLogin
import util.createRequestHash
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

/**
 * Created by bggRGjQaUbCoE on 2024/6/10
 */
class LoginViewModel(
    private val networkRepo: NetworkRepo,
    private val dispatcher: CoroutineDispatcher,
    private val userPreferencesRepo: UserPreferencesRepo,
) : ViewModel() {

    var requestHash by mutableStateOf(EMPTY_STRING)
    var captchaImg by mutableStateOf<ImageBitmap?>(null)
        private set
    var toastText by mutableStateOf<String?>(null)
        private set

    private val urlPreGetParam = "/auth/login?type=mobile"
    private val urlGetParam = "/auth/loginByCoolApk"

    init {
        isPreGetLoginParam = true
        viewModelScope.launch(dispatcher) {
            onGetLoginParam(urlPreGetParam)
        }
    }

    private suspend fun onGetLoginParam(url: String) {
        networkRepo.getLoginParam(url)
            .collect { result ->
                val response = result.getOrNull()
                if (response != null) {
                    if (url == urlGetParam) {
                        response.bodyAsText().let {
                            requestHash = Jsoup.parse(it).createRequestHash()
                        }
                    }
                    try {
                        response.headers.getAll("Set-Cookie")?.get(0)?.let {
                            SESSID = it.substring(0, it.indexOf(";"))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        toastText = "无法获取Cookie: ${e.message}"
                        return@collect
                    }
                    if (url == urlPreGetParam) {
                        isGetLoginParam = true
                        onGetLoginParam(urlGetParam)
                    }
                } else {
                    toastText = result.exceptionOrNull()?.message ?: "response is null"
                    result.exceptionOrNull()?.printStackTrace()
                }
            }
    }

    fun onGetCaptcha() {
        isGetCaptcha = true
        val timeStamp = System.currentTimeMillis().toString()
        viewModelScope.launch(dispatcher) {
            networkRepo.getCaptcha("/auth/showCaptchaImage?$timeStamp")
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

    fun onLogin(account: String, password: String, captcha: String) {
        isTryLogin = true
        viewModelScope.launch(dispatcher) {
            networkRepo.tryLogin(requestHash, account, password, captcha)
                .collect { result ->
                    val response = result.getOrNull()
                    response?.body<HttpResponse>()?.let {
                        val login: LoginResponse = Gson().fromJson(
                            it.bodyAsText(),
                            LoginResponse::class.java
                        )
                        if (login.status == 1) {
                            val cookies = response.headers.getAll("Set-Cookie")
                            val uid =
                                cookies?.find { cookie -> cookie.startsWith("uid=") }?.split(";")
                                    ?.getOrNull(0)
                                    ?.replace("uid=", EMPTY_STRING)?.trim()
                            val username =
                                cookies?.find { cookie -> cookie.startsWith("username=") }
                                    ?.split(";")
                                    ?.getOrNull(0)?.replace("username=", EMPTY_STRING)?.trim()
                            val token =
                                cookies?.findLast { cookie -> cookie.startsWith("token=") }
                                    ?.split(";")
                                    ?.getOrNull(0)
                                    ?.replace("token=", EMPTY_STRING)?.trim()
                            if (!uid.isNullOrEmpty() && !username.isNullOrEmpty() && !token.isNullOrEmpty()) {
                                userPreferencesRepo.apply {
                                    setIsLogin(true)
                                    setUid(uid)
                                    setUsername(username)
                                    setToken(token)
                                }
                            }
                        } else {
                            login.message?.let {
                                toastText = login.message
                                when (login.message) {
                                    "图形验证码不能为空", "图形验证码错误" -> onGetCaptcha()

                                    "密码错误" -> if (captchaImg != null) onGetCaptcha()
                                }
                            }
                        }
                    }
                }
        }
    }

    fun reset() {
        toastText = null
    }

}