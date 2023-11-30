package com.example.motherLoad.Utils

import java.security.MessageDigest

class SHA256Hasher {
    fun hash(string : String): String {
        val bytes = string.toByteArray()
        val md = MessageDigest.getInstance("SHA256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }
}