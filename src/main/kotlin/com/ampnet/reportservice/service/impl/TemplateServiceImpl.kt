package com.ampnet.reportservice.service.impl

import com.ampnet.core.jwt.UserPrincipal
import com.ampnet.reportservice.controller.pojo.PeriodServiceRequest
import com.ampnet.reportservice.controller.pojo.TransactionServiceRequest
import com.ampnet.reportservice.exception.ErrorCode
import com.ampnet.reportservice.exception.InternalException
import com.ampnet.reportservice.service.TemplateDataService
import com.ampnet.reportservice.service.TemplateService
import mu.KLogging
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import org.thymeleaf.exceptions.TemplateEngineException
import java.util.UUID

@Service
class TemplateServiceImpl(
    val templateDataService: TemplateDataService,
    val templateEngine: TemplateEngine
) : TemplateService {

    companion object : KLogging()

    internal val userTransactionsTemplate = "user-transactions-template"
    internal val userTransactionTemplate = "user-transaction-template"
    internal val usersAccountsSummaryTemplate = "users-accounts-summary-template"

    override fun generateTemplateForUserTransactions(userUUID: UUID, periodRequest: PeriodServiceRequest): String {
        val transactions = templateDataService.getUserTransactionsData(userUUID, periodRequest)
        return processThymeleafTemplate(transactions, userTransactionsTemplate)
    }

    override fun generateTemplateForUserTransaction(transactionServiceRequest: TransactionServiceRequest): String {
        val transaction = templateDataService.getUserTransactionData(transactionServiceRequest)
        return processThymeleafTemplate(transaction, userTransactionTemplate)
    }

    override fun generateTemplateForAllActiveUsers(user: UserPrincipal, periodRequest: PeriodServiceRequest): String {
        val activeUsersSummaryData = templateDataService.getAllActiveUsersSummaryData(user, periodRequest)
        return processThymeleafTemplate(activeUsersSummaryData, usersAccountsSummaryTemplate)
    }

    private fun processThymeleafTemplate(data: Any, templateName: String): String {
        val context = Context()
        context.setVariable("data", data)
        try {
            return templateEngine.process(templateName, context)
        } catch (ex: TemplateEngineException) {
            logger.warn { ex.message }
            throw InternalException(
                ErrorCode.INT_GENERATING_PDF,
                "Could not process $templateName with thymeleaf.",
                ex
            )
        }
    }
}
