package logic.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import constant.Constants.API_VERSION
import constant.Constants.EMPTY_STRING
import constant.Constants.VERSION_CODE
import constant.Constants.VERSION_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import logic.datastore.FollowType
import logic.datastore.ThemeMode
import logic.datastore.ThemeType
import logic.datastore.UserPreference

/**
 * Created by bggRGjQaUbCoE on 2024/6/27
 */
class UserPreferencesRepo(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val defaultPrefs = UserPreference(
            themeMode = ThemeMode.FOLLOW_SYSTEM,
            materialYou = true,
            pureBlack = false,
            fontScale = 1.00f,
            contentScale = 1.00f,
            szlmId = EMPTY_STRING,
            imageQuality = 0,
            imageFilter = true,
            openInBrowser = false,
            showSquare = true,
            recordHistory = true,
            showEmoji = true,
            checkUpdate = true,
            checkCount = true,
            versionName = VERSION_NAME,
            apiVersion = API_VERSION,
            versionCode = VERSION_CODE,
            manufacturer = EMPTY_STRING,
            brand = EMPTY_STRING,
            model = EMPTY_STRING,
            buildNumber = EMPTY_STRING,
            sdkInt = EMPTY_STRING,
            androidVersion = EMPTY_STRING,
            userAgent = EMPTY_STRING,
            xAppDevice = EMPTY_STRING,
            xAppToken = EMPTY_STRING,
            isLogin = false,
            userAvatar = EMPTY_STRING,
            username = EMPTY_STRING,
            level = EMPTY_STRING,
            experience = EMPTY_STRING,
            nextLevelExperience = EMPTY_STRING,
            uid = EMPTY_STRING,
            token = EMPTY_STRING,
            followType = FollowType.ALL,
            recentIds = EMPTY_STRING,
            checkCountPeriod = 5,
            installTime = EMPTY_STRING,
            themeType = ThemeType.Default,
            seedColor = EMPTY_STRING,
        )
    }

    private val themeModeKey = intPreferencesKey("themeMode")
    private val materialYouKey = booleanPreferencesKey("materialYou")
    private val pureBlackKey = booleanPreferencesKey("pureBlack")
    private val fontScaleKey = floatPreferencesKey("fontScale")
    private val contentScaleKey = floatPreferencesKey("contentScale")
    private val szlmIdKey = stringPreferencesKey("szlmId")
    private val imageQualityKey = intPreferencesKey("imageQuality")
    private val imageFilterKey = booleanPreferencesKey("imageFilter")
    private val openInBrowserKey = booleanPreferencesKey("openInBrowser")
    private val showSquareKey = booleanPreferencesKey("showSquare")
    private val recordHistoryKey = booleanPreferencesKey("recordHistory")
    private val showEmojiKey = booleanPreferencesKey("showEmoji")
    private val checkUpdateKey = booleanPreferencesKey("checkUpdate")
    private val checkCountKey = booleanPreferencesKey("checkCount")
    private val versionNameKey = stringPreferencesKey("versionName")
    private val apiVersionKey = stringPreferencesKey("apiVersion")
    private val versionCodeKey = stringPreferencesKey("versionCode")
    private val manufacturerKey = stringPreferencesKey("manufacturer")
    private val brandKey = stringPreferencesKey("brand")
    private val modelKey = stringPreferencesKey("model")
    private val buildNumberKey = stringPreferencesKey("buildNumber")
    private val sdkIntKey = stringPreferencesKey("sdkInt")
    private val androidVersionKey = stringPreferencesKey("androidVersion")
    private val userAgentKey = stringPreferencesKey("userAgent")
    private val xAppDeviceKey = stringPreferencesKey("xAppDevice")
    private val xAppTokenKey = stringPreferencesKey("xAppToken")
    private val isLoginKey = booleanPreferencesKey("isLogin")
    private val userAvatarKey = stringPreferencesKey("userAvatar")
    private val usernameKey = stringPreferencesKey("username")
    private val levelKey = stringPreferencesKey("level")
    private val experienceKey = stringPreferencesKey("experience")
    private val nextLevelExperienceKey = stringPreferencesKey("nextLevelExperience")
    private val uidKey = stringPreferencesKey("uid")
    private val tokenKey = stringPreferencesKey("token")
    private val followTypeKey = intPreferencesKey("followType")
    private val recentIdsKey = stringPreferencesKey("recentIds")
    private val checkCountPeriodKey = intPreferencesKey("checkCountPeriod")
    private val installTimeKey = stringPreferencesKey("installTime")
    private val themeTypeKey = intPreferencesKey("themeType")
    private val seedColorKey = stringPreferencesKey("seedColor")

    val prefs: Flow<UserPreference> = dataStore.data.map {
        UserPreference(
            ThemeMode.entries[it[themeModeKey] ?: 0],
            it[materialYouKey] ?: defaultPrefs.materialYou,
            it[pureBlackKey] ?: defaultPrefs.pureBlack,
            it[fontScaleKey] ?: defaultPrefs.fontScale,
            it[contentScaleKey] ?: defaultPrefs.contentScale,
            it[szlmIdKey] ?: defaultPrefs.szlmId,
            it[imageQualityKey] ?: defaultPrefs.imageQuality,
            it[imageFilterKey] ?: defaultPrefs.imageFilter,
            it[openInBrowserKey] ?: defaultPrefs.openInBrowser,
            it[showSquareKey] ?: defaultPrefs.showSquare,
            it[recordHistoryKey] ?: defaultPrefs.recordHistory,
            it[showEmojiKey] ?: defaultPrefs.showEmoji,
            it[checkUpdateKey] ?: defaultPrefs.checkUpdate,
            it[checkCountKey] ?: defaultPrefs.checkCount,
            it[versionNameKey] ?: defaultPrefs.versionName,
            it[apiVersionKey] ?: defaultPrefs.apiVersion,
            it[versionCodeKey] ?: defaultPrefs.versionCode,
            it[manufacturerKey] ?: defaultPrefs.manufacturer,
            it[brandKey] ?: defaultPrefs.brand,
            it[modelKey] ?: defaultPrefs.model,
            it[buildNumberKey] ?: defaultPrefs.buildNumber,
            it[sdkIntKey] ?: defaultPrefs.sdkInt,
            it[androidVersionKey] ?: defaultPrefs.androidVersion,
            it[userAgentKey] ?: defaultPrefs.userAgent,
            it[xAppDeviceKey] ?: defaultPrefs.xAppDevice,
            it[xAppTokenKey] ?: defaultPrefs.xAppToken,
            it[isLoginKey] ?: defaultPrefs.isLogin,
            it[userAvatarKey] ?: defaultPrefs.userAvatar,
            it[usernameKey] ?: defaultPrefs.username,
            it[levelKey] ?: defaultPrefs.level,
            it[experienceKey] ?: defaultPrefs.experience,
            it[nextLevelExperienceKey] ?: defaultPrefs.nextLevelExperience,
            it[uidKey] ?: defaultPrefs.uid,
            it[tokenKey] ?: defaultPrefs.token,
            FollowType.entries[it[followTypeKey] ?: 0],
            it[recentIdsKey] ?: defaultPrefs.recentIds,
            it[checkCountPeriodKey] ?: defaultPrefs.checkCountPeriod,
            it[installTimeKey] ?: defaultPrefs.installTime,
            ThemeType.entries[it[themeTypeKey] ?: 0],
            it[seedColorKey] ?: defaultPrefs.seedColor,
        )
    }

    suspend fun setThemeMode(value: Int) {
        dataStore.edit { it[themeModeKey] = value }
    }

    suspend fun setMaterialYou(value: Boolean) {
        dataStore.edit { it[materialYouKey] = value }
    }

    suspend fun setPureBlack(value: Boolean) {
        dataStore.edit { it[pureBlackKey] = value }
    }

    suspend fun setFontScale(value: Float) {
        dataStore.edit { it[fontScaleKey] = value }
    }

    suspend fun setContentScale(value: Float) {
        dataStore.edit { it[contentScaleKey] = value }
    }

    suspend fun setSZLMId(value: String) {
        dataStore.edit { it[szlmIdKey] = value }
    }

    suspend fun setImageQuality(value: Int) {
        dataStore.edit { it[imageQualityKey] = value }
    }

    suspend fun setImageFilter(value: Boolean) {
        dataStore.edit { it[imageFilterKey] = value }
    }

    suspend fun setOpenInBrowser(value: Boolean) {
        dataStore.edit { it[openInBrowserKey] = value }
    }

    suspend fun setShowSquare(value: Boolean) {
        dataStore.edit { it[showSquareKey] = value }
    }

    suspend fun setRecordHistory(value: Boolean) {
        dataStore.edit { it[recordHistoryKey] = value }
    }

    suspend fun setShowEmoji(value: Boolean) {
        dataStore.edit { it[showEmojiKey] = value }
    }

    suspend fun setCheckUpdate(value: Boolean) {
        dataStore.edit { it[checkUpdateKey] = value }
    }

    suspend fun setCheckCount(value: Boolean) {
        dataStore.edit { it[checkCountKey] = value }
    }

    suspend fun setVersionName(value: String) {
        dataStore.edit { it[versionNameKey] = value }
    }

    suspend fun setApiVersion(value: String) {
        dataStore.edit { it[apiVersionKey] = value }
    }

    suspend fun setVersionCode(value: String) {
        dataStore.edit { it[versionCodeKey] = value }
    }

    suspend fun setManufacturer(value: String) {
        dataStore.edit { it[manufacturerKey] = value }
    }

    suspend fun setBrand(value: String) {
        dataStore.edit { it[brandKey] = value }
    }

    suspend fun setModel(value: String) {
        dataStore.edit { it[modelKey] = value }
    }

    suspend fun setBuildNumber(value: String) {
        dataStore.edit { it[buildNumberKey] = value }
    }

    suspend fun setSdkInt(value: String) {
        dataStore.edit { it[sdkIntKey] = value }
    }

    suspend fun setAndroidVersion(value: String) {
        dataStore.edit { it[androidVersionKey] = value }
    }

    suspend fun setUserAgent(value: String) {
        dataStore.edit { it[userAgentKey] = value }
    }

    suspend fun setXAppDevice(value: String) {
        dataStore.edit { it[xAppDeviceKey] = value }
    }

    suspend fun setXAppToken(value: String) {
        dataStore.edit { it[xAppTokenKey] = value }
    }

    suspend fun setFollowType(value: Int) {
        dataStore.edit { it[followTypeKey] = value }
    }

    suspend fun setThemeType(value: Int) {
        dataStore.edit { it[themeTypeKey] = value }
    }

    suspend fun setSeedColor(value: String) {
        dataStore.edit { it[seedColorKey] = value }
    }

    suspend fun setUid(value: String) {
        dataStore.edit { it[uidKey] = value }
    }

    suspend fun setUserAvatar(value: String) {
        dataStore.edit { it[userAvatarKey] = value }
    }

    suspend fun setUsername(value: String) {
        dataStore.edit { it[usernameKey] = value }
    }

    suspend fun setToken(value: String) {
        dataStore.edit { it[tokenKey] = value }
    }

    suspend fun setIsLogin(value: Boolean) {
        dataStore.edit { it[isLoginKey] = value }
    }

    suspend fun setLevel(value: String) {
        dataStore.edit { it[levelKey] = value }
    }

    suspend fun setExperience(value: String) {
        dataStore.edit { it[experienceKey] = value }
    }

    suspend fun setNextLevelExperience(value: String) {
        dataStore.edit { it[nextLevelExperienceKey] = value }
    }

    suspend fun setInstallTime(value: String) {
        dataStore.edit { it[installTimeKey] = value }
    }

}