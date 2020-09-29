package com.ampnet.reportservice.service.data

import com.ampnet.userservice.proto.UserWithInfoResponse
import java.lang.StringBuilder
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.UUID

class UserInfo(uuid: UUID, userWithInfo: UserWithInfoResponse) {

    val userUuid: UUID = uuid
    val firstName: String = userWithInfo.user.firstName
    val lastName: String = userWithInfo.user.lastName
    val address: List<String>
    val createdAt: LocalDateTime

    init {
        address = setStreetCityCounty(userWithInfo.address)
        createdAt = getLocalDateTime(userWithInfo.createdAt)
    }

    private fun setStreetCityCounty(address: String): List<String> {
        val listValues = address.split(",").map { it -> StringBuilder(it.toLowerCase()) }
        return listValues.map { capitalizeEachLetter(it).trim().toString() }.reversed()
    }

    private fun capitalizeEachLetter(address: StringBuilder): StringBuilder {
        val formatted = StringBuilder()
        address.split(" ").forEach {
            formatted.append(it.capitalize())
            formatted.append(" ")
        }
        return formatted
    }

    private fun getLocalDateTime(miliSeconds: String): LocalDateTime {
        return Instant.ofEpochMilli(miliSeconds.toLong()).atZone(ZoneId.systemDefault()).toLocalDateTime()
    }
}
