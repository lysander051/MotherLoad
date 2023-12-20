package com.example.motherLoad.Utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.motherland.MotherLoad
import java.security.KeyStore
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

class LoginManager {
    companion object{
        private val TAG = "LoginManager"
        fun hash(string : String): String {
            val bytes = string.toByteArray()
            val md = MessageDigest.getInstance("SHA256")
            val digest = md.digest(bytes)
            return digest.fold("", { str, it -> str + "%02x".format(it) })
        }

        private fun generateKey() : SecretKey {
            val keyGenerator : KeyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            keyGenerator.init(
                KeyGenParameterSpec.Builder("Key",
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build())
            return keyGenerator.generateKey()
        }
        @RequiresApi(Build.VERSION_CODES.O)
        public fun savePassword(password: String) : String {
            val secretKey : SecretKey = generateKey()
            val ciph : Cipher = Cipher.getInstance(
                KeyProperties.KEY_ALGORITHM_AES
                        + "/"
                        + KeyProperties.BLOCK_MODE_CBC
                        + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7)
            ciph.init(Cipher.ENCRYPT_MODE, secretKey)

            val byteIV : ByteArray = ciph.iv
            val iv : String = Base64.getEncoder().encodeToString(byteIV)
            Log.d(TAG, "L'iv du chiffrement = $iv")
            val bytePassword : ByteArray = password.toByteArray()
            val byteChiffrePassword : ByteArray = ciph.doFinal(bytePassword)
            val chiffrePassword : String = Base64.getEncoder().encodeToString(byteChiffrePassword)

            val sharedPreferences = MotherLoad.instance.getSharedPreferences("Connexion", Context.MODE_PRIVATE)
            with(sharedPreferences.edit()){
                putString("iv",iv)
                apply()
            }
            return chiffrePassword
        }

        @RequiresApi(Build.VERSION_CODES.O)
        public fun getDecryptedPassword() : String {
            val sharedPreferences = MotherLoad.instance.getSharedPreferences("Connexion", Context.MODE_PRIVATE)
            val chiffrePassword : String? = sharedPreferences.getString("psw","")
            val iv : String? = sharedPreferences.getString("iv", "")
            if (chiffrePassword != "" && iv != ""){
                Log.d(TAG, "password chiffre = $chiffrePassword")
                Log.d(TAG, "L'iv du déchiffrement = $iv")
                val byteChiffrePassword : ByteArray = Base64.getDecoder().decode(chiffrePassword)
                val byteIV : ByteArray = Base64.getDecoder().decode(iv)
                val keyStore : KeyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                val secretKey : SecretKey = keyStore.getKey("Key",null) as SecretKey
                val ciph : Cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES
                        + "/"
                        + KeyProperties.BLOCK_MODE_CBC
                        + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7)
                ciph.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(byteIV))
                val bytePassword : ByteArray = ciph.doFinal(byteChiffrePassword)
                val password : String = bytePassword.decodeToString()
                Log.d(TAG, "Password = $password")
                return password
            }
            return ""
        }

    }

}