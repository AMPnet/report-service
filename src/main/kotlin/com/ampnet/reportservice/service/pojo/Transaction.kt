package com.ampnet.reportservice.service.pojo

import com.ampnet.crowdfunding.proto.TransactionsResponse
import com.ampnet.reportservice.enums.TransactionStatusType
import java.text.DecimalFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

const val FROM_CENTS_TO_EUROS = 100L
const val TO_PERCENTAGE = 100.0

class Transaction(
    val type: TransactionsResponse.Transaction.Type,
    val fromTxHash: String,
    val toTxHash: String,
    val amount: String,
    val date: String,
    val state: String
) {
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
    lateinit var txStatus: TransactionStatusType
    lateinit var amountInEuro: String

    constructor(transaction: TransactionsResponse.Transaction) : this(
        transaction.type,
        transaction.fromTxHash,
        transaction.toTxHash,
        transaction.amount,
        transaction.date,
        transaction.state
    ) {
        txDate = formatToYearMonthDayTime(date)
        amountInEuro = getEurAmountFormatted(amount.toLong())
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

class TxSummary(
    val transactions: List<Transaction>,
    val userInfo: UserInfo
) {
    private val transactionsByType = transactions.groupBy { it.type }
    val period: String? = getPeriod(transactions)
    val dateOfFinish: String? = getDateOfFinish(transactions)
    val balance: String = getBalance(transactions)

    val deposits = getEurAmountFormatted(
        transactionsByType[TransactionsResponse.Transaction.Type.DEPOSIT]?.sumByLong {
            it.amount.toLong()
        }?.div(FROM_CENTS_TO_EUROS) ?: 0
    )
    val withdrawals = getEurAmountFormatted(
        transactionsByType[TransactionsResponse.Transaction.Type.WITHDRAW]?.sumByLong {
            it.amount.toLong()
        }?.div(FROM_CENTS_TO_EUROS) ?: 0
    )
    val revenueShare = getEurAmountFormatted(
        transactionsByType[TransactionsResponse.Transaction.Type.SHARE_PAYOUT]?.sumByLong {
            it.amount.toLong()
        }?.div(FROM_CENTS_TO_EUROS) ?: 0
    )
    val investments = getEurAmountFormatted(
        transactionsByType[TransactionsResponse.Transaction.Type.INVEST]?.sumByLong {
            it.amount.toLong()
        }?.div(FROM_CENTS_TO_EUROS) ?: 0
    )
    val sharesBought = getEurAmountFormatted(
        transactionsByType[TransactionsResponse.Transaction.Type.UNRECOGNIZED]?.sumByLong {
            it.amount.toLong()
        }?.div(FROM_CENTS_TO_EUROS) ?: 0
    )
    val sharesSold = getEurAmountFormatted(
        transactionsByType[TransactionsResponse.Transaction.Type.UNRECOGNIZED]?.sumByLong {
            it.amount.toLong()
        }?.div(FROM_CENTS_TO_EUROS) ?: 0
    )

    private fun getPeriod(transactions: List<Transaction>): String? {
        return when (transactions.size) {
            0 -> null
            1 -> formatToYearMonthDay(transactions.first().date)
            else -> {
                formatToYearMonthDay(transactions.first().date) + " to " +
                    formatToYearMonthDay(transactions.last().date)
            }
        }
    }

    private fun getDateOfFinish(transactions: List<Transaction>): String? {
        if (transactions.isEmpty()) return null
        return "Total balance as of " + formatToYearMonthDay(transactions.last().date)
    }

    private fun formatToYearMonthDay(date: String): String {
        val pattern = "MMM dd, yyyy"
        return DateTimeFormatter.ofPattern(pattern).format(ZonedDateTime.parse(date))
    }

    private fun getBalance(transactions: List<Transaction>): String {
        var balance = 0L
        transactions.forEach { transaction ->
            val amount = transaction.amount.toLong()
            when (transaction.type) {
                TransactionsResponse.Transaction.Type.INVEST -> balance -= amount
                TransactionsResponse.Transaction.Type.CANCEL_INVESTMENT -> balance += amount
                TransactionsResponse.Transaction.Type.SHARE_PAYOUT -> balance += amount
                TransactionsResponse.Transaction.Type.DEPOSIT -> balance += amount
                TransactionsResponse.Transaction.Type.WITHDRAW -> balance -= amount
                TransactionsResponse.Transaction.Type.UNRECOGNIZED -> {
                    // skip
                }
            }
        }
        return getEurAmountFormatted(balance)
    }
}

inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum = 0L
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

private fun getEurAmountFormatted(amount: Long): String {
    val decimalFormat = DecimalFormat("#,##0.00")
    return decimalFormat.format(amount / FROM_CENTS_TO_EUROS)
}
