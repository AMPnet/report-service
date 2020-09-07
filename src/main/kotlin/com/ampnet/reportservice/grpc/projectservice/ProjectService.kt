package com.ampnet.reportservice.grpc.projectservice

import com.ampnet.projectservice.proto.ProjectResponse
import java.util.UUID

interface ProjectService {
    fun getProjects(uuids: Iterable<UUID>): List<ProjectResponse>
}
