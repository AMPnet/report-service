package com.ampnet.reportservice.service

import com.ampnet.core.jwt.UserPrincipal
import com.ampnet.reportservice.controller.pojo.PeriodServiceRequest
import com.ampnet.reportservice.controller.pojo.TransactionServiceRequest
import com.ampnet.reportservice.service.data.SingleTransactionSummary
import com.ampnet.reportservice.service.data.TransactionsSummary
import com.ampnet.reportservice.service.data.UsersAccountsSummary
import java.util.UUID

interface TemplateDataService {
    fun getUserTransactionsData(userUUID: UUID, periodRequest: PeriodServiceRequest): TransactionsSummary
    fun getUserTransactionData(txServiceRequest: TransactionServiceRequest): SingleTransactionSummary
    fun getAllActiveUsersSummaryData(user: UserPrincipal, periodRequest: PeriodServiceRequest): UsersAccountsSummary
}
