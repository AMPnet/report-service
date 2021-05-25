package com.ampnet.reportservice.controller

import com.ampnet.reportservice.controller.pojo.PeriodServiceRequest
import com.ampnet.reportservice.controller.pojo.XlsxType
import com.ampnet.reportservice.service.ReportingService
import com.ampnet.reportservice.service.XlsxService
import mu.KLogging
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class AdminController(
    private val reportingService: ReportingService,
    private val xlsxService: XlsxService
) {

    companion object : KLogging()

    @GetMapping("/admin/report/user")
    @PreAuthorize("hasAuthority(T(com.ampnet.reportservice.enums.PrivilegeType).PRA_PROFILE)")
    fun getActiveUsersReport(
        @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?
    ): ResponseEntity<ByteArray> {
        val user = ControllerUtils.getUserPrincipalFromSecurityContext()
        logger.info {
            "Received request to get users accounts summary for all the active users"
        }
        val periodRequest = PeriodServiceRequest(from, to)
        val pdfContents = reportingService.generatePdfReportForAllActiveUsers(user, periodRequest)
        return ResponseEntity(pdfContents, ControllerUtils.getHttpHeadersForPdf(), HttpStatus.OK)
    }

    @GetMapping("/admin/report/xlsx")
    @PreAuthorize("hasAuthority(T(com.ampnet.reportservice.enums.PrivilegeType).PRA_PROFILE)")
    fun getXlsxReport(@RequestParam type: XlsxType): ResponseEntity<ByteArray> {
        val user = ControllerUtils.getUserPrincipalFromSecurityContext()
        logger.info { "Received request to get users xlsx report, type: $type" }
        val pdfContents = xlsxService.generateXlsx(user.coop, type)
        val httpHeaders = HttpHeaders().apply {
            contentType = MediaType.APPLICATION_OCTET_STREAM
        }
        logger.info { "Successfully generate xlsx report, type: $type" }
        return ResponseEntity(pdfContents, httpHeaders, HttpStatus.OK)
    }
}
