package com.ampnet.reportservice.grpc.userservice

import com.ampnet.reportservice.config.ApplicationProperties
import com.ampnet.reportservice.exception.ErrorCode
import com.ampnet.reportservice.exception.GrpcException
import com.ampnet.userservice.proto.CoopRequest
import com.ampnet.userservice.proto.GetUserRequest
import com.ampnet.userservice.proto.GetUsersRequest
import com.ampnet.userservice.proto.UserResponse
import com.ampnet.userservice.proto.UserServiceGrpc
import com.ampnet.userservice.proto.UserWithInfoResponse
import com.ampnet.userservice.proto.UsersExtendedResponse
import io.grpc.StatusRuntimeException
import mu.KLogging
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.jvm.Throws

@Service
class UserServiceImpl(
    private val grpcChannelFactory: GrpcChannelFactory,
    private val applicationProperties: ApplicationProperties
) : UserService {

    companion object : KLogging()

    private val serviceBlockingStub: UserServiceGrpc.UserServiceBlockingStub by lazy {
        val channel = grpcChannelFactory.createChannel("user-service")
        UserServiceGrpc.newBlockingStub(channel)
    }

    @Throws(GrpcException::class)
    override fun getUsers(uuids: Set<UUID>): List<UserResponse> {
        if (uuids.isEmpty()) return emptyList()
        logger.debug { "Fetching users: $uuids" }
        try {
            val request = GetUsersRequest.newBuilder()
                .addAllUuids(uuids.map { it.toString() })
                .build()
            val response = serviceWithTimeout().getUsers(request).usersList
            logger.debug { "Fetched users: ${response.size}" }
            return response
        } catch (ex: StatusRuntimeException) {
            logger.warn(ex.localizedMessage)
            throw GrpcException(ErrorCode.INT_GRPC_USER, "Failed to fetch users")
        }
    }

    @Throws(GrpcException::class)
    override fun getUserWithInfo(uuid: UUID): UserWithInfoResponse {
        logger.debug { "Fetching user: $uuid" }
        try {
            val request = GetUserRequest.newBuilder()
                .setUuid(uuid.toString())
                .build()
            val response = serviceWithTimeout().getUserWithInfo(request)
            logger.debug { "Fetched user: $response" }
            return response
        } catch (ex: StatusRuntimeException) {
            logger.warn(ex.localizedMessage)
            throw GrpcException(ErrorCode.INT_GRPC_USER, "Failed to fetch user")
        }
    }

    @Throws(GrpcException::class)
    override fun getAllActiveUsers(coop: String): UsersExtendedResponse {
        logger.debug { "Fetching UsersExtendedResponse for coop: $coop" }
        try {
            val request = CoopRequest.newBuilder()
                .setCoop(coop)
                .build()
            val response = serviceWithTimeout().getAllActiveUsers(request)
            logger.debug { "Fetched users: ${response.usersCount}" }
            return response
        } catch (ex: StatusRuntimeException) {
            logger.warn(ex.localizedMessage)
            throw GrpcException(ErrorCode.INT_GRPC_USER, "Failed to fetch UsersExtendedResponse")
        }
    }

    private fun serviceWithTimeout() = serviceBlockingStub
        .withDeadlineAfter(applicationProperties.grpc.userServiceTimeout, TimeUnit.MILLISECONDS)
}
