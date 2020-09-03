package com.ampnet.reportservice.controller

import com.ampnet.reportservice.service.ReportingService
import mu.KLogging
import org.springframework.web.bind.annotation.RestController

@RestController
class ReportingController(
    private val reportingService: ReportingService
) {

    companion object : KLogging()
}