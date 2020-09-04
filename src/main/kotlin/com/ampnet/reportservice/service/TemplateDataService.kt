package com.ampnet.reportservice.service

import com.ampnet.reportservice.service.pojo.Transactions
import java.util.UUID

interface TemplateDataService {
    fun getUserTransactionsData(userUUID: UUID): Transactions
}
