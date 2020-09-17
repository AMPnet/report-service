package com.ampnet.reportservice.service.data

import java.text.DecimalFormat

const val FROM_CENTS_TO_EUROS = 100L

fun Long.toEurAmount(): String = DecimalFormat("#,##0.00").format(this / FROM_CENTS_TO_EUROS)
