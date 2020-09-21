package com.ampnet.reportservice.controller

import com.ampnet.reportservice.controller.pojo.PeriodServiceRequest
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
        val httpHeaders = HttpHeaders()
        httpHeaders.contentType = MediaType.APPLICATION_PDF
        val pdfContents = reportingService.generatePdfReportForUserTransactions(periodRequest, userPrincipal.uuid)
        return ResponseEntity(pdfContents, httpHeaders, HttpStatus.OK)
    }
}
