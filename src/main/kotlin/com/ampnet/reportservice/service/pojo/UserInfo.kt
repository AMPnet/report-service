package com.ampnet.reportservice.service.pojo

import com.ampnet.userservice.proto.UserWithInfoResponse
import java.lang.StringBuilder
import java.util.UUID

const val ADDRESS_DETAILS_SIZE = 3

data class UserInfo(
    val userUuid: UUID,
    val firstName: String,
    val lastName: String,
    val address: String
) {
    var street: String? = null
    var city: String? = null
    var county: String? = null

    constructor(userUuid: UUID, userWithInfo: UserWithInfoResponse) : this(
        userUuid,
        userWithInfo.user.firstName,
        userWithInfo.user.lastName,
        userWithInfo.address
    ) {
        setStreetCityCounty(address)
    }

    private fun setStreetCityCounty(address: String) {
        val listValues = address.split(",").map { it -> it.trim().toLowerCase() }
        if (listValues.size == ADDRESS_DETAILS_SIZE) {
            county = capitalizeEachLetter(listValues[0])
            city = capitalizeEachLetter(listValues[1])
            street = capitalizeEachLetter(listValues[2])
        }
    }

    private fun capitalizeEachLetter(string: String): String {
        val formatted = StringBuilder()
        string.split(" ").forEach {
            formatted.append(it.capitalize())
            formatted.append(" ")
        }
        return formatted.trim().toString()
    }
}
