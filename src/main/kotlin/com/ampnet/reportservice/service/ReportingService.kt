package com.ampnet.reportservice.service

import java.util.UUID

interface ReportingService {

    fun generatePdfReportForUserTransactions(userUUID: UUID): ByteArray
}
