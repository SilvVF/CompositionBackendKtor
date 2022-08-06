package com.androiddevs.security

import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import java.security.SecureRandom



fun getHashWithSalt(sToHash: String, saltLen: Int = 32): String {
    val salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLen)
    val saltAsHex = Hex.encodeHexString(salt)
    val hash = DigestUtils.sha256Hex("${saltAsHex}${sToHash}")
    //seperated by colon with the salt easier to retrieve it when their seperated later
    return "$saltAsHex:$hash"
}

fun checkHashForPassword(password: String, hashWithSalt: String): Boolean {
    //separate the hash and the salt at the colon that they were split with
    val hashAndSalt  = hashWithSalt.split(":")
    val salt = hashAndSalt.first()
    val hash = hashAndSalt.last()
    // get the hashed value of the password entered
    val passwordHash = DigestUtils.sha256Hex("$salt$password")
    return hash == passwordHash
}

