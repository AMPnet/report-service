package com.ampnet.reportservice.service.data

import com.ampnet.crowdfunding.proto.TransactionsResponse
import com.ampnet.reportservice.enums.TransactionStatusType
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

const val TO_PERCENTAGE = 100.0

class Transaction(transaction: TransactionsResponse.Transaction) {

    val type: TransactionsResponse.Transaction.Type = transaction.type
    val fromTxHash: String = transaction.fromTxHash
    val toTxHash: String = transaction.toTxHash
    val amount: String = transaction.amount
    val date: String = transaction.date
    val state: String = transaction.state
    var from: String? = null
    var to: String? = null
    var expectedProjectFunding: Long? = null
        set(value) {
            value?.let {
                field = value
                percentageInProject = getPercentageInProject(field, amount)
            }
        }
    var percentageInProject: String? = null
    var txDate: String? = null
    var amountInEuro: String
    var txStatus: TransactionStatusType

    init {
        txDate = formatToYearMonthDayTime(date)
        amountInEuro = CurrencyFormatting.getEurAmountFormatted(amount.toLong())
        txStatus = getTransactionStatusType(type)
    }

    private fun formatToYearMonthDayTime(date: String): String {
        val pattern = "MMM dd, yyyy HH:mm"
        return DateTimeFormatter.ofPattern(pattern).format(ZonedDateTime.parse(date))
    }

    private fun getTransactionStatusType(type: TransactionsResponse.Transaction.Type): TransactionStatusType {
        return when (type) {
            TransactionsResponse.Transaction.Type.INVEST -> TransactionStatusType.PAID_OUT
            TransactionsResponse.Transaction.Type.CANCEL_INVESTMENT -> TransactionStatusType.PAID_IN
            TransactionsResponse.Transaction.Type.SHARE_PAYOUT -> TransactionStatusType.PAID_IN
            TransactionsResponse.Transaction.Type.DEPOSIT -> TransactionStatusType.PAID_IN
            TransactionsResponse.Transaction.Type.WITHDRAW -> TransactionStatusType.PAID_OUT
            TransactionsResponse.Transaction.Type.UNRECOGNIZED -> TransactionStatusType.UNDEFINED
        }
    }

    private fun getPercentageInProject(expectedFunding: Long?, amount: String): String? {
        return expectedFunding?.let {
            (TO_PERCENTAGE * amount.toLong() / expectedFunding).toString()
        }
    }
}
