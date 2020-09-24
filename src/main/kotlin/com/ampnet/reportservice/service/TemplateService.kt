package com.ampnet.reportservice.service

import com.ampnet.reportservice.controller.pojo.PeriodServiceRequest
import java.util.UUID

interface TemplateService {
    fun generateTemplateForUserTransactions(userUUID: UUID, periodRequest: PeriodServiceRequest): String
}
