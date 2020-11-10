package com.ampnet.reportservice.service.data

import com.ampnet.crowdfunding.proto.TransactionType
import com.ampnet.reportservice.controller.pojo.PeriodServiceRequest
import mu.KLogging
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val DATE_FORMAT = "MMM dd, yyyy"

class TransactionsSummary(
    val transactions: List<Transaction>,
    val userInfo: UserInfo,
    val periodRequest: PeriodServiceRequest
) {
    companion object : KLogging()

    private val transactionsByType = transactions.groupBy { it.type }
    val period: String = getPeriod(periodRequest)
    val dateOfFinish: String? = getDateOfFinish(transactions, periodRequest)
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

    private fun getPeriod(periodRequest: PeriodServiceRequest): String {
        val fromPeriod = formatToYearMonthDay(periodRequest.from ?: userInfo.createdAt)
        val toPeriod = formatToYearMonthDay(periodRequest.to ?: LocalDateTime.now())
        return "$fromPeriod to $toPeriod"
    }

    private fun getDateOfFinish(transactions: List<Transaction>, periodRequest: PeriodServiceRequest): String? {
        return if (transactions.isEmpty()) {
            formatToYearMonthDay(periodRequest.to ?: LocalDateTime.now())
        } else {
            formatToYearMonthDay(periodRequest.to) ?: formatToYearMonthDay(transactions.last().date)
        }
    }

    private fun formatToYearMonthDay(date: LocalDateTime?): String? =
        date?.format(DateTimeFormatter.ofPattern(DATE_FORMAT))

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
