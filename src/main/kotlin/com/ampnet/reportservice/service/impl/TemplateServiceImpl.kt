package com.ampnet.reportservice.service.impl

import com.ampnet.reportservice.controller.pojo.PeriodServiceRequest
import com.ampnet.reportservice.exception.ErrorCode
import com.ampnet.reportservice.exception.InternalException
import com.ampnet.reportservice.service.TemplateDataService
import com.ampnet.reportservice.service.TemplateService
import com.ampnet.reportservice.service.data.TxSummary
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

    override fun generateTemplateForUserTransactions(userUUID: UUID, periodRequest: PeriodServiceRequest): String {
        val transactions = templateDataService.getUserTransactionsData(userUUID, periodRequest)
        return processThymeleafTemplate(transactions)
    }

    private fun processThymeleafTemplate(txSummary: TxSummary): String {
        val context = Context()
        context.setVariable("txSummary", txSummary)
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
