package com.ampnet.reportservice.service

import com.ampnet.reportservice.controller.pojo.PeriodServiceRequest
import com.ampnet.reportservice.controller.pojo.TransactionServiceRequest
import java.util.UUID

interface TemplateService {
    fun generateTemplateForUserTransactions(userUUID: UUID, periodRequest: PeriodServiceRequest): String
    fun generateTemplateForUserTransaction(transactionServiceRequest: TransactionServiceRequest): String
}
