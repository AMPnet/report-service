package com.ampnet.reportservice.service

import com.ampnet.reportservice.controller.pojo.PeriodServiceRequest
import com.ampnet.reportservice.controller.pojo.TransactionServiceRequest
import java.util.UUID

interface ReportingService {
    fun generatePdfReportForUserTransactions(userUUID: UUID, periodRequest: PeriodServiceRequest): ByteArray
    fun generatePdfReportForUserTransaction(transactionServiceRequest: TransactionServiceRequest): ByteArray
}
