package com.ampnet.reportservice

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReportServiceApplication

@Suppress("SpreadOperator")
fun main(args: Array<String>) {
    runApplication<ReportServiceApplication>(*args)
}
