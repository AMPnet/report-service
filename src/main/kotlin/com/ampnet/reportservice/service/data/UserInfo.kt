package com.ampnet.reportservice.service.data

import com.ampnet.reportservice.enums.SupportedLanguages
import com.ampnet.userservice.proto.UserWithInfoResponse
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class UserInfo(userWithInfo: UserWithInfoResponse) {

    val userUuid: String = userWithInfo.user.uuid
    val firstName: String = userWithInfo.user.firstName
    val lastName: String = userWithInfo.user.lastName
    val createdAt: LocalDateTime = getLocalDateTime(userWithInfo.createdAt)
    val language: String = setLanguage(userWithInfo.user.language)
    val logo: String = userWithInfo.coop.logo

    private fun getLocalDateTime(milliSeconds: Long): LocalDateTime {
        return Instant.ofEpochMilli(milliSeconds).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    private fun setLanguage(userLanguage: String): String {
        if (SupportedLanguages.values().any { it.name.equals(userLanguage, true) }) return userLanguage
        return "en"
    }
}
