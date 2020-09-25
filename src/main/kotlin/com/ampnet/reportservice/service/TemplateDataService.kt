package com.ampnet.reportservice.service

import com.ampnet.reportservice.controller.pojo.PeriodServiceRequest
import com.ampnet.reportservice.service.data.TxSummary
import java.util.UUID

interface TemplateDataService {
    fun getUserTransactionsData(userUUID: UUID, periodRequest: PeriodServiceRequest): TxSummary
}
