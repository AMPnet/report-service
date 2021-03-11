package com.ampnet.reportservice.grpc.userservice

import com.ampnet.userservice.proto.UserResponse
import com.ampnet.userservice.proto.UserWithInfoResponse
import com.ampnet.userservice.proto.UsersExtendedResponse
import java.util.UUID

interface UserService {
    fun getUsers(uuids: Set<UUID>): List<UserResponse>
    fun getUserWithInfo(uuid: UUID): UserWithInfoResponse
    fun getAllActiveUsers(uuid: UUID, coop: String): UsersExtendedResponse
}
