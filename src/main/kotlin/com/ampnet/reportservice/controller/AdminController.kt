package com.ampnet.reportservice.controller

import com.ampnet.reportservice.controller.pojo.PeriodServiceRequest
import com.ampnet.reportservice.service.ReportingService
import mu.KLogging
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
class AdminController(
    private val reportingService: ReportingService
) {

    companion object : KLogging()

    @GetMapping("/admin/report/user")
    @PreAuthorize("hasAuthority(T(com.ampnet.reportservice.enums.PrivilegeType).PRA_PROFILE)")
    fun getActiveUsersReport(
        @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) from: LocalDate?,
        @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) to: LocalDate?
    ): ResponseEntity<ByteArray> {
        val user = ControllerUtils.getUserPrincipalFromSecurityContext()
        logger.debug {
            "Received request to get users accounts summary for all the active users"
        }
        val periodRequest = PeriodServiceRequest(from, to)
        val pdfContents = reportingService.generatePdfReportForAllActiveUsers(user, periodRequest)
        return ResponseEntity(pdfContents, ControllerUtils.getHttpHeadersForPdf(), HttpStatus.OK)
    }
}
