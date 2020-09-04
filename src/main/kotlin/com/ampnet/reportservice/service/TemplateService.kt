package com.ampnet.reportservice.service

import java.util.UUID

interface TemplateService {
    fun generateTemplateForUserTransactions(userUUID: UUID): String
}
