package util

import constant.Constants.EMPTY_STRING
import constant.Constants.MODE
import logic.repository.UserPreferencesRepo
import util.TokenDeviceUtils.encodeDevice
import kotlin.random.Random

/**
 * Created by bggRGjQaUbCoE on 2024/6/29
 */
object DeviceUtil {

    var isPreGetLoginParam = false
    var isGetLoginParam = false
    var isTryLogin = false
    var isGetCaptcha = false
    var isGetSmsLoginParam = false
    var isGetSmsToken = false

    var atme: Int? = null
    var atcommentme: Int? = null
    var feedlike: Int? = null
    var contacts_follow: Int? = null
    var message: Int? = null
    var notification: Int = 0

    var SESSID = EMPTY_STRING
    var szlmId = EMPTY_STRING
    var versionName = EMPTY_STRING
    var versionCode = EMPTY_STRING
    var userAgent = EMPTY_STRING
    var sdkInt = EMPTY_STRING
    var uid = EMPTY_STRING
    var username = EMPTY_STRING
    var token = EMPTY_STRING
    var apiVersion = EMPTY_STRING
    var xAppDevice = EMPTY_STRING
    var showSquare = true
    var isLogin = true
    var recordHistory = true
    var openInBrowser = false

    suspend fun UserPreferencesRepo.regenerateParams() {
        val manufacturer = randomManufacturer()
        val brand = randomBrand()
        val model = randomDeviceModel()
        val buildNumber = randHexString(32)
        val sdkInt = randomSdkInt()
        val androidVersion = randomAndroidVersionRelease()
        val mac = randomMacAddress()
        val userAgent =
            "Dalvik/2.1.0 (Linux; U; Android $androidVersion; $model $buildNumber) (#Build; $brand; $model; $buildNumber; $androidVersion) +CoolMarket/$versionName-$versionCode-$MODE"
        val xAppDevice =
            encodeDevice("${szlmId.ifEmpty { randHexString(32) }}; ; ; $mac; $manufacturer; $brand; $model; $buildNumber; null")

        setManufacturer(manufacturer)
        setBrand(brand)
        setModel(model)
        setBuildNumber(buildNumber)
        setSdkInt(sdkInt)
        setAndroidVersion(androidVersion)
        setUserAgent(userAgent)
        setXAppDevice(xAppDevice)
    }

    private fun randHexString(length: Int): String {
        val chars = "0123456789abcdef"
        val random = Random.Default
        val hexString = StringBuilder(length)

        repeat(length) {
            hexString.append(chars[random.nextInt(chars.length)])
        }

        return hexString.toString().uppercase()
    }

    fun randomMacAddress(): String {
        val macBytes = ByteArray(6)
        Random.nextBytes(macBytes)

        macBytes[0] = (macBytes[0].toInt() and 0xFE or 0x02).toByte()

        return macBytes.joinToString(separator = ":") {
            String.format("%02X", it)
        }
    }

    private fun randomManufacturer(): String {
        val manufacturers = listOf(
            "Samsung",
            "Google",
            "Huawei",
            "Xiaomi",
            "OnePlus",
            "Sony",
            "LG",
            "Motorola",
            "HTC",
            "Nokia",
            "Lenovo",
            "Asus",
            "ZTE",
            "Alcatel",
            "OPPO",
            "Vivo",
            "Realme"
        )

        return manufacturers[Random.nextInt(manufacturers.size)]
    }

    private fun randomBrand(): String {
        val brands = listOf(
            "Samsung",
            "Google",
            "Huawei",
            "Xiaomi",
            "Redmi",
            "OnePlus",
            "Sony",
            "LG",
            "Motorola",
            "HTC",
            "Nokia",
            "Lenovo",
            "Asus",
            "ZTE",
            "Alcatel",
            "OPPO",
            "Vivo",
            "Realme"
        )

        return brands[Random.nextInt(brands.size)]
    }


    private fun randomDeviceModel(): String {
        // TODO
        return randHexString(6)
    }

    private fun randomSdkInt(): String {
        return Random.nextInt(21, 34).toString()
    }

    private fun randomAndroidVersionRelease(): String {
        val androidVersionRelease = listOf(
            "5.0.1", // Lollipop
            "6.0",   // Marshmallow
            "7.0",   // Nougat
            "7.1.1", // Nougat
            "8.0.0", // Oreo
            "8.1.0", // Oreo
            "9",     // Pie
            "10",    // Android 10
            "11",    // Android 11
            "12",     // Android 12
            "13",
            "14",
        )

        return androidVersionRelease[Random.nextInt(androidVersionRelease.size)]
    }

}