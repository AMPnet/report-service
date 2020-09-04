package com.ampnet.reportservice.service.impl

import com.ampnet.reportservice.exception.ErrorCode
import com.ampnet.reportservice.exception.InternalException
import com.ampnet.reportservice.service.ReportingService
import com.ampnet.reportservice.service.TemplateService
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder
import mu.KLogging
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID

@Service
class ReportingServiceImpl(
    private val templateService: TemplateService,
    private val rendererBuilder: PdfRendererBuilder
) : ReportingService {

    companion object : KLogging()

    private val downloadDir = System.getProperty("user.home") + File.separator + "Desktop" + File.separator
    private val rootTemplate = "templates/root.htm"
    private val pdfTest = "pdf-test.pdf"

    override fun generatePdfReportForUserTransactions(userUUID: UUID): ByteArray {
        val template = templateService.generateTemplateForUserTransactions(userUUID)
        // generateFromTemplateToLocalFile(template) --> to be used while testing to generate pdf file locally
        return generateFromTemplateToByteArray(template)
    }

    private fun generateFromTemplateToByteArray(html: String): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val baseUri = javaClass.classLoader.getResource(rootTemplate)
            ?: throw InternalException(
                ErrorCode.INT_GENERATING_PDF,
                "Could not find local resources under: $rootTemplate"
            )
        try {
            rendererBuilder.withHtmlContent(html, baseUri.toString())
            rendererBuilder.toStream(outputStream)
            rendererBuilder.run()
        } catch (ex: IOException) {
            logger.warn { ex.message }
            throw InternalException(
                ErrorCode.INT_GENERATING_PDF,
                "Could not generate pdf with PdfRendererBuilder.\nException message: ${ex.message}"
            )
        }
        return outputStream.toByteArray()
    }

    private fun generateFromTemplateToLocalFile(html: String) {
        val baseUri = javaClass.classLoader.getResource("templates/root.htm")
            ?: throw InternalException(
                ErrorCode.INT_GENERATING_PDF,
                "Could not find local resources under: $rootTemplate"
            )
        try {
            rendererBuilder.withHtmlContent(html, baseUri.toString())
            rendererBuilder.toStream(FileOutputStream(downloadDir + pdfTest))
            rendererBuilder.run()
        } catch (ex: IOException) {
            logger.warn { ex.message }
            throw InternalException(
                ErrorCode.INT_GENERATING_PDF,
                "Could not generate pdf with PdfRendererBuilder.\nException message: ${ex.message}"
            )
        }
    }
}
