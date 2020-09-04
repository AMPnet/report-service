package com.ampnet.reportservice.controller

import com.ampnet.reportservice.service.ReportingService
import mu.KLogging
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ReportingController(
    private val reportingService: ReportingService
) {

    companion object : KLogging()

    @GetMapping("/report/user/transactions")
    fun getUserTransactionsReport(): ResponseEntity<ByteArray> {
        val userPrincipal = ControllerUtils.getUserPrincipalFromSecurityContext()
        logger.debug { "Received request to get report of transactions for user with uuid: ${userPrincipal.uuid}" }
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_PDF
        val pdfContents = reportingService.generatePdfReportForUserTransactions(userPrincipal.uuid)
        return ResponseEntity(pdfContents, httpHeaders, HttpStatus.OK)
    }
}
