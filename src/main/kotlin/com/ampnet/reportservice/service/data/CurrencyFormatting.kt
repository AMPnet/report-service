package com.ampnet.reportservice.service.data

import java.text.DecimalFormat

const val FROM_CENTS_TO_EUROS = 100L

class CurrencyFormatting private constructor() {

    companion object {
        fun getEurAmountFormatted(amount: Long): String {
            val decimalFormat = DecimalFormat("#,##0.00")
            return decimalFormat.format(amount / FROM_CENTS_TO_EUROS)
        }
    }
}
