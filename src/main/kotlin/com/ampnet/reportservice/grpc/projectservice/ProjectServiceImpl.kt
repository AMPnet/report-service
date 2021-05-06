package com.ampnet.reportservice.grpc.projectservice

import com.ampnet.projectservice.proto.GetByUuids
import com.ampnet.projectservice.proto.ProjectResponse
import com.ampnet.projectservice.proto.ProjectServiceGrpc
import com.ampnet.reportservice.config.ApplicationProperties
import com.ampnet.reportservice.exception.ErrorCode
import com.ampnet.reportservice.exception.GrpcException
import io.grpc.StatusRuntimeException
import mu.KLogging
import net.devh.boot.grpc.client.channelfactory.GrpcChannelFactory
import org.springframework.stereotype.Service
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.jvm.Throws

@Service
class ProjectServiceImpl(
    private val grpcChannelFactory: GrpcChannelFactory,
    private val applicationProperties: ApplicationProperties
) : ProjectService {

    companion object : KLogging()

    private val serviceBlockingStub: ProjectServiceGrpc.ProjectServiceBlockingStub by lazy {
        val channel = grpcChannelFactory.createChannel("project-service")
        ProjectServiceGrpc.newBlockingStub(channel)
    }

    @Throws(GrpcException::class)
    override fun getProjects(uuids: Iterable<UUID>): List<ProjectResponse> {
        if (uuids.none()) return emptyList()
        logger.debug { "Fetching projects: $uuids" }
        try {
            val request = GetByUuids.newBuilder()
                .addAllUuids(uuids.map { it.toString() })
                .build()
            val response = serviceWithTimeout().getProjects(request).projectsList
            logger.debug { "Fetched projects: ${response.size}" }
            return response
        } catch (ex: StatusRuntimeException) {
            logger.warn(ex.localizedMessage)
            throw GrpcException(ErrorCode.INT_GRPC_PROJECT, "Failed to fetch projects")
        }
    }

    private fun serviceWithTimeout() = serviceBlockingStub
        .withDeadlineAfter(applicationProperties.grpc.projectServiceTimeout, TimeUnit.MILLISECONDS)
}
