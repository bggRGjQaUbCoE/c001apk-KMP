package util

import constant.Constants.APP_ID
import constant.Constants.APP_LABEL
import constant.Constants.EMPTY_STRING
import org.mindrot.jbcrypt.BCrypt
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Base64

/**
 * Created by bggRGjQaUbCoE on 2024/6/27
 */
object TokenDeviceUtils {

    fun encodeDevice(deviceInfo: String): String {
        val bytes = deviceInfo.toByteArray(StandardCharsets.UTF_8)
        val encodeToString = Base64.getEncoder().encodeToString(bytes)
        val replace = StringBuilder(encodeToString).reverse().toString()
        return Regex("\\r\\n|\\r|\\n|=").replace(replace, EMPTY_STRING)
    }

    fun String.getTokenV2(): String {
        val timeStamp = (System.currentTimeMillis() / 1000f).toString()

        val base64TimeStamp = timeStamp.getBase64()
        val md5TimeStamp = timeStamp.getMD5()
        val md5DeviceCode = this.getMD5()

        val token = "${APP_LABEL}?$md5TimeStamp$$md5DeviceCode&${APP_ID}"
        val base64Token = token.getBase64()
        val md5Base64Token = base64Token.getMD5()
        val md5Token = token.getMD5()

        val bcryptSalt = "${"$2a$10$$base64TimeStamp/$md5Token".substring(0, 31)}u"
        val bcryptResult = BCrypt.hashpw(md5Base64Token, bcryptSalt)

        return "v2${bcryptResult.replaceRange(0, 3, "$2y").getBase64()}"
    }

    private fun String.getBase64(): String {
        return Base64.getEncoder().encodeToString(this.toByteArray()).replace("=", "")
    }

    private fun String.getMD5(): String {
        val bytes = this.toByteArray()
        val md = MessageDigest.getInstance("MD5")
        val digest = md.digest(bytes)

        return digest.joinToString("") {
            "%02x".format(it)
        }.replace("-", "")
    }

}