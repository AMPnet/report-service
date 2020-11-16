package com.ampnet.reportservice.service.impl

import com.ampnet.reportservice.controller.pojo.PeriodServiceRequest
import com.ampnet.reportservice.controller.pojo.TransactionServiceRequest
import com.ampnet.reportservice.exception.ErrorCode
import com.ampnet.reportservice.exception.InternalException
import com.ampnet.reportservice.service.ReportingService
import com.ampnet.reportservice.service.TemplateService
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import mu.KLogging
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URL
import java.util.UUID

@Service
class ReportingServiceImpl(
    private val templateService: TemplateService,
    private val rendererBuilder: PdfRendererBuilder
) : ReportingService {

    companion object : KLogging()

    private val rootTemplate = "templates/root.htm"

    override fun generatePdfReportForUserTransactions(userUUID: UUID, periodRequest: PeriodServiceRequest): ByteArray {
        val template = templateService.generateTemplateForUserTransactions(userUUID, periodRequest)
        return generateFromTemplateToByteArray(template)
    }

    override fun generatePdfReportForUserTransaction(transactionServiceRequest: TransactionServiceRequest): ByteArray {
        val template = templateService.generateTemplateForUserTransaction(transactionServiceRequest)
        return generateFromTemplateToByteArray(template)
    }

    private fun generateFromTemplateToByteArray(html: String): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val baseUri = getRootTemplateUri()
        try {
            rendererBuilder.withHtmlContent(html, baseUri.toString())
            rendererBuilder.toStream(outputStream)
            rendererBuilder.run()
            return outputStream.toByteArray()
        } catch (ex: IOException) {
            logger.warn { ex.message }
            throw InternalException(
                ErrorCode.INT_GENERATING_PDF,
                "Could not generate pdf with PdfRendererBuilder.",
                ex
            )
        }
    }

    private fun getRootTemplateUri(): URL {
        return javaClass.classLoader.getResource("templates/root.htm")
            ?: throw InternalException(
                ErrorCode.INT_GENERATING_PDF,
                "Could not find local resources under: $rootTemplate"
            )
    }
}
