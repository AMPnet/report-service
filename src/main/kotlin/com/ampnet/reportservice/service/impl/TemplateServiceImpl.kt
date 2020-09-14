package com.ampnet.reportservice.service.impl

import com.ampnet.reportservice.exception.ErrorCode
import com.ampnet.reportservice.exception.InternalException
import com.ampnet.reportservice.service.TemplateDataService
import com.ampnet.reportservice.service.TemplateService
import com.ampnet.reportservice.service.pojo.Transactions
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

    override fun generateTemplateForUserTransactions(userUUID: UUID): String {
        val transactions = templateDataService.getUserTransactionsData(userUUID)
        return processThymeleafTemplate(transactions)
    }

    private fun processThymeleafTemplate(transactions: Transactions): String {
        val context = Context()
        context.setVariable("transactions", transactions)
        try {
            return templateEngine.process(userTransactionsTemplate, context)
        } catch (ex: TemplateEngineException) {
            logger.warn { ex.message }
            throw InternalException(
                ErrorCode.INT_GENERATING_PDF,
                "Could not process template with thymeleaf.",
                ex
            )
        }
    }
}
