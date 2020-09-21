package com.ampnet.reportservice.service

import com.ampnet.reportservice.controller.pojo.PeriodServiceRequest
import java.util.UUID

interface ReportingService {
    fun generatePdfReportForUserTransactions(periodRequest: PeriodServiceRequest, userUUID: UUID): ByteArray
}
