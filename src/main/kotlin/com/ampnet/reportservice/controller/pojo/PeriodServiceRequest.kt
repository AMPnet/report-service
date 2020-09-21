package com.ampnet.reportservice.controller.pojo

import java.time.LocalDate

data class PeriodServiceRequest(
    val from: LocalDate?,
    val to: LocalDate?
)
