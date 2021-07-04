package com.isel_5gqos.common.utils.mobile_utils

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.telephony.TelephonyManager
import com.isel_5gqos.common.SECRET_PASSWORD
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class MobileInfoUtils {

    companion object {

        fun getImei(context: Context, telephonyManager: TelephonyManager) = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Settings.Secure.getString(
                context.getContentResolver(),
                Settings.Secure.ANDROID_ID
            )
        } else {
            telephonyManager.imei
        }

        fun getDeviceSerialNumber (): String {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                return ""
            }

            return Build.getSerial()
        }

        fun encryptLoginCredentials(username:String, password:String):Pair<ByteArray,ByteArray>{
            val random = SecureRandom()
            val salt = ByteArray(256)
            random.nextBytes(salt)

            val pbKeySpec = PBEKeySpec(SECRET_PASSWORD.toCharArray(), salt, 1324, 256)
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
            val keySpec = SecretKeySpec(keyBytes, "AES")
            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            return Pair(cipher.doFinal("$username:$password".toByteArray()),salt)
        }

        fun decryptLoginCredentials(decriptionPair:Pair<ByteArray,ByteArray>):Pair<String,String>{
            val (first,second) = decriptionPair
            val pbKeySpec = PBEKeySpec(SECRET_PASSWORD.toCharArray(), second, 1324, 256)
            val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
            val keyBytes = secretKeyFactory.generateSecret(pbKeySpec).encoded
            val keySpec = SecretKeySpec(keyBytes, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS7Padding")
            cipher.init(Cipher.DECRYPT_MODE, keySpec)
            val credentialsByteArray = cipher.doFinal(first)
            val credentialsStr = String(credentialsByteArray)
            val splittedCredentials = credentialsStr.split(":")
            return Pair(splittedCredentials[0],splittedCredentials[1])
        }
    }
}