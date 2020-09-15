package com.ampnet.reportservice.service.pojo

import com.ampnet.userservice.proto.UserWithInfoResponse
import java.util.UUID

data class UserInfo(
    val userUuid: UUID,
    val firstName: String,
    val lastName: String,
    val address: String
) {
    constructor(userUuid: UUID, userWithInfo: UserWithInfoResponse) : this(
        userUuid,
        userWithInfo.user.firstName,
        userWithInfo.user.lastName,
        userWithInfo.address
    )
}
