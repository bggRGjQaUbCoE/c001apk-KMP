package ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import constant.Constants.EMPTY_STRING
import io.ktor.client.call.body
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import logic.model.CheckResponse
import logic.repository.NetworkRepo
import logic.repository.UserPreferencesRepo
import util.DeviceUtil
import util.DeviceUtil.SESSID
import util.DeviceUtil.isLogin
import util.ViewModelInstance
import util.encode

/**
 * Created by bggRGjQaUbCoE on 2024/6/3
 */
class MainViewModel(
    private val networkRepo: NetworkRepo,
    private val userPreferencesRepo: UserPreferencesRepo,
    dispatcher: CoroutineDispatcher,
) : ViewModelInstance(dispatcher) {

    var badge by mutableIntStateOf(0)
        private set

    var toastText by mutableStateOf<String?>(null)

    fun resetToastText() {
        toastText = null
    }

    init {
        getCheckLoginInfo()
    }

    private fun getCheckLoginInfo() {
        viewModelScope.launch(dispatcher) {
            delay(2000)
            networkRepo.checkLoginInfo()
                .collect { result ->
                    val response = result.getOrNull()
                    if (response != null) {
                        response.body<CheckResponse>().data?.let { login ->
                            badge = login.notifyCount?.badge ?: 0
                            DeviceUtil.atme = login.notifyCount?.atme
                            DeviceUtil.atcommentme = login.notifyCount?.atcommentme
                            DeviceUtil.feedlike = login.notifyCount?.feedlike
                            DeviceUtil.contacts_follow = login.notifyCount?.contactsFollow
                            DeviceUtil.message = login.notifyCount?.message

                            userPreferencesRepo.apply {
                                setUid(login.uid.orEmpty())
                                setUserAvatar(login.userAvatar.orEmpty())
                                setUsername(login.username.encode)
                                setToken(login.token.orEmpty())
                                setIsLogin(true)
                            }
                        }

                        if (!response.body<CheckResponse>().message.isNullOrEmpty()) {
                            if (isLogin) {
                                toastText = response.body<CheckResponse>().message
                            }
                            userPreferencesRepo.apply {
                                setUid(EMPTY_STRING)
                                setUserAvatar(EMPTY_STRING)
                                setUsername(EMPTY_STRING)
                                setToken(EMPTY_STRING)
                                setIsLogin(false)
                            }
                        }

                        try {
                            response.headers.getAll("Set-Cookie")?.get(0)?.let {
                                SESSID = it.substring(0, it.indexOf(";"))
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            e.message?.let {
                                toastText = it
                            }
                        }

                    }
                }
        }
    }

    fun resetBadge() {
        badge = 0
    }

}