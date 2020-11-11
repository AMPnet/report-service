package com.ampnet.reportservice.controller

import com.ampnet.reportservice.controller.pojo.PeriodServiceRequest
import com.ampnet.reportservice.controller.pojo.TransactionServiceRequest
import com.ampnet.reportservice.service.ReportingService
import mu.KLogging
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class ReportingController(
    private val reportingService: ReportingService
) {

    companion object : KLogging()

    @GetMapping("/report/user/transactions")
    fun getUserTransactionsReport(
        @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?
    ): ResponseEntity<ByteArray> {
        val userPrincipal = ControllerUtils.getUserPrincipalFromSecurityContext()
        logger.debug { "Received request to get report of transactions for user with uuid: ${userPrincipal.uuid}" }
        val periodRequest = PeriodServiceRequest(from, to)
        val pdfContents = reportingService.generatePdfReportForUserTransactions(userPrincipal.uuid, periodRequest)
        return ResponseEntity(pdfContents, getHttpHeadersForPdf(), HttpStatus.OK)
    }

    @GetMapping("/report/user/transaction")
    fun getUserTransactionReport(
        @RequestParam(name = "txHash") txHash: String,
        @RequestParam(name = "fromTxHash") fromTxHash: String,
        @RequestParam(name = "toTxHash") toTxHash: String
    ): ResponseEntity<ByteArray> {
        val userPrincipal = ControllerUtils.getUserPrincipalFromSecurityContext()
        logger.debug {
            "Received request to get the report for a transaction: $txHash " +
                "for user with uuid: ${userPrincipal.uuid}"
        }
        val transactionServiceRequest = TransactionServiceRequest(userPrincipal.uuid, txHash, fromTxHash, toTxHash)
        val pdfContents = reportingService.generatePdfReportForUserTransaction(transactionServiceRequest)
        return ResponseEntity(pdfContents, getHttpHeadersForPdf(), HttpStatus.OK)
    }

    private fun getHttpHeadersForPdf(): HttpHeaders {
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_PDF
        return httpHeaders
    }
}
