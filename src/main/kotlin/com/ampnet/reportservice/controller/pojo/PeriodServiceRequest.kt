package com.ampnet.reportservice.controller.pojo

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class PeriodServiceRequest(from: LocalDate?, to: LocalDate?) {
    var from: LocalDateTime? = null
    var to: LocalDateTime? = null

    init {
        this.from = from?.let { LocalDateTime.of(from, LocalTime.MIN) }
        this.to = to?.let { LocalDateTime.of(to, LocalTime.MAX) }
    }
}
