package com.ampnet.reportservice.service.impl

import com.ampnet.reportservice.service.TemplateDataService
import com.ampnet.reportservice.service.TemplateService
import com.ampnet.reportservice.service.pojo.Transactions
import org.springframework.stereotype.Service

@Service
class TemplateServiceImpl(
    val templateDataService: TemplateDataService
) : TemplateService {

    override fun generateTemplateForUserTransactions(transactions: Transactions) {
        TODO("Not yet implemented")
    }
}