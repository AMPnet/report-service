package com.ampnet.reportservice.service.data

import com.ampnet.crowdfunding.proto.TransactionType
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
    val deposits = sumTransactionAmountsByType(TransactionType.DEPOSIT).toEurAmount()
    val withdrawals = sumTransactionAmountsByType(TransactionType.WITHDRAW).toEurAmount()
    val revenueShare = sumTransactionAmountsByType(TransactionType.SHARE_PAYOUT).toEurAmount()
    val investments = (
        sumTransactionAmountsByType(TransactionType.INVEST) -
            sumTransactionAmountsByType(TransactionType.CANCEL_INVESTMENT)
        ).toEurAmount()
    val sharesBought = sumTransactionAmountsByType(TransactionType.UNRECOGNIZED).toEurAmount()
    val sharesSold = sumTransactionAmountsByType(TransactionType.UNRECOGNIZED).toEurAmount()

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

    private fun formatToYearMonthDay(date: ZonedDateTime) =
        date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))

    private fun getBalance(transactions: List<Transaction>): String {
        val balance = transactions.sumByLong { it.amountToCalculate() }
        if (balance < 0) {
            logger.error("Negative balance for user: $userInfo for a period: $period")
            return "N/A"
        }
        return balance.toEurAmount()
    }

    private fun sumTransactionAmountsByType(type: TransactionType): Long {
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
