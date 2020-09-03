package com.ampnet.reportservice.service

import com.ampnet.reportservice.service.pojo.Transactions

interface TemplateService {
    fun generateTemplateForUserTransactions(transactions: Transactions)
}