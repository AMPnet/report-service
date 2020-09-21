package com.ampnet.reportservice.service.data

import com.ampnet.crowdfunding.proto.TransactionsResponse
import com.ampnet.reportservice.controller.pojo.PeriodServiceRequest
import mu.KLogging
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

const val DATE_FORMAT = "MMM dd, yyyy"

class TxSummary(
    val transactions: List<Transaction>,
    val userInfo: UserInfo,
    val periodRequest: PeriodServiceRequest
) {
    companion object : KLogging()

    private val transactionsByType = transactions.groupBy { it.type }
    val period: String? = getPeriod(transactions, periodRequest)
    val dateOfFinish: String? = getDateOfFinish(transactions, periodRequest)
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

    private fun getPeriod(transactions: List<Transaction>, periodRequest: PeriodServiceRequest): String? {
        val fromDate = periodRequest.from
        val toDate = periodRequest.to
        return when (transactions.size) {
            0 -> formatToYearMonthDay(fromDate ?: LocalDate.now())
            1 -> {
                (formatToYearMonthDay(fromDate) ?: formatToYearMonthDay(transactions.first().date)) + " to " +
                    (formatToYearMonthDay(toDate) ?: formatToYearMonthDay(LocalDate.now()))
            }
            else -> {
                (formatToYearMonthDay(fromDate) ?: formatToYearMonthDay(transactions.first().date)) + " to " +
                    (formatToYearMonthDay(toDate) ?: formatToYearMonthDay(transactions.last().date))
            }
        }
    }

    private fun getDateOfFinish(transactions: List<Transaction>, periodRequest: PeriodServiceRequest): String? {
        return if (transactions.isEmpty()) {
            formatToYearMonthDay(periodRequest.to ?: LocalDate.now())
        } else {
            formatToYearMonthDay(periodRequest.to) ?: formatToYearMonthDay(transactions.last().date)
        }
    }

    private fun formatToYearMonthDay(date: ZonedDateTime): String =
        date.format(DateTimeFormatter.ofPattern(DATE_FORMAT))

    private fun formatToYearMonthDay(date: LocalDate?): String? {
        return date?.format(DateTimeFormatter.ofPattern(DATE_FORMAT))
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
