package com.ampnet.reportservice.service.data

import com.ampnet.userservice.proto.CoopResponse
import com.ampnet.userservice.proto.UserExtendedResponse
import com.ampnet.userservice.proto.UserWithInfoResponse
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class UserInfo {

    val userUuid: String
    val firstName: String
    val lastName: String
    val createdAt: LocalDateTime
    val language: String
    val logo: String
    var dateOfBirth: String? = null
    var documentNumber: String? = null
    var dateOfIssue: String? = null
    var dateOfExpiry: String? = null
    var personalNumber: String? = null

    constructor(userWithInfo: UserWithInfoResponse) {
        userUuid = userWithInfo.user.uuid
        firstName = userWithInfo.user.firstName
        lastName = userWithInfo.user.lastName
        createdAt = userWithInfo.createdAt.millisecondsToLocalDateTime()
        language = userWithInfo.user.language
        logo = userWithInfo.coop.logo
    }
    constructor(user: UserExtendedResponse, coop: CoopResponse, language: String) {
        userUuid = user.uuid
        firstName = user.firstName
        lastName = user.lastName
        createdAt = user.createdAt.millisecondsToLocalDateTime()
        this.language = language
        logo = coop.logo
        dateOfBirth = user.dateOfBirth
        documentNumber = user.documentNumber
        dateOfIssue = user.dateOfIssue
        dateOfExpiry = user.dateOfExpiry
        personalNumber = user.personalNumber
    }
}

fun Long.millisecondsToLocalDateTime(): LocalDateTime {
    return Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
}
