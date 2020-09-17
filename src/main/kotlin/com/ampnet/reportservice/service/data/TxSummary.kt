package com.ampnet.reportservice.service.data

import com.ampnet.crowdfunding.proto.TransactionsResponse
import mu.KLogging
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class TxSummary(
    val transactions: List<Transaction>,
    val userInfo: UserInfo
) {
    companion object : KLogging()

    private val transactionsByType = transactions.groupBy { it.type }
    val period: String? = getPeriod(transactions)
    val dateOfFinish: String? = getDateOfFinish(transactions)
    val balance: String = getBalance(transactions)
    val deposits = sumTransactionAmountsByType(TransactionsResponse.Transaction.Type.DEPOSIT).toEurAmount()
    val withdrawals = sumTransactionAmountsByType(TransactionsResponse.Transaction.Type.WITHDRAW).toEurAmount()
    val revenueShare = sumTransactionAmountsByType(TransactionsResponse.Transaction.Type.SHARE_PAYOUT).toEurAmount()
    val investments = (
        sumTransactionAmountsByType(TransactionsResponse.Transaction.Type.INVEST) -
            sumTransactionAmountsByType(TransactionsResponse.Transaction.Type.CANCEL_INVESTMENT)
        ).toEurAmount()
    val sharesBought = sumTransactionAmountsByType(TransactionsResponse.Transaction.Type.UNRECOGNIZED).toEurAmount()
    val sharesSold = sumTransactionAmountsByType(TransactionsResponse.Transaction.Type.UNRECOGNIZED).toEurAmount()

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
        val balance = transactions.sumByLong { it.amountToCalculate() }
        if (balance < 0) {
            logger.error("Negative balance for user: $userInfo for a period: $period")
            return "N/A"
        }
        return balance.toEurAmount()
    }

    private fun sumTransactionAmountsByType(type: TransactionsResponse.Transaction.Type): Long {
        return transactionsByType[type]?.sumByLong { it.amount } ?: 0L
    }
}

inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum = 0L
    for (element in this) {
        sum += selector(element)
    }
    return sum
}
