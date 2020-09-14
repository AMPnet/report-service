package com.ampnet.reportservice.service.pojo

import com.ampnet.crowdfunding.proto.TransactionsResponse
import com.ampnet.reportservice.enums.TransactionStatusType
import com.ampnet.userservice.proto.UserWithInfoResponse
import java.text.DecimalFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

const val FROM_CENTS_TO_EUROS = 100L

data class Transaction(
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
    var percentageInProject: String = ""
    val txDate = formatToYearMonthDayTime(date)
    val txStatus = getTransactionStatusType(type)
    val showTxTo = showTxTo(type)
    val showPercentage = showPercentage(type)
    val amountInEuro = getEurAmount(amount.toLong() / FROM_CENTS_TO_EUROS)

    constructor(transaction: TransactionsResponse.Transaction) : this(
        transaction.type,
        transaction.fromTxHash,
        transaction.toTxHash,
        transaction.amount,
        transaction.date,
        transaction.state
    )

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

    private fun showTxTo(type: TransactionsResponse.Transaction.Type): Boolean {
        if (type == TransactionsResponse.Transaction.Type.DEPOSIT ||
            type == TransactionsResponse.Transaction.Type.WITHDRAW
        ) return false
        return true
    }

    private fun showPercentage(type: TransactionsResponse.Transaction.Type): Boolean {
        if (type == TransactionsResponse.Transaction.Type.INVEST ||
            type == TransactionsResponse.Transaction.Type.SHARE_PAYOUT ||
            type == TransactionsResponse.Transaction.Type.CANCEL_INVESTMENT
        ) return true
        return false
    }
}

data class Transactions(
    val transactions: List<Transaction>,
    val userInfo: UserInfo
) {
    private val transactionsByType = transactions.groupBy { it.type }

    val txAmountsSum: Long = transactions.sumByLong { it.amount.toLong() / FROM_CENTS_TO_EUROS }
    val period: String = getPeriod(transactions)
    val dateOfFinish = "Total balance as of " + formatToYearMonthDay(transactions.last().date)
    val balance: String = getBalance(transactions)

    val deposits = transactionsByType[TransactionsResponse.Transaction.Type.DEPOSIT]?.sumByLong {
        it.amount.toLong()
    }?.div(FROM_CENTS_TO_EUROS) ?: 0
    val withdrawals = transactionsByType[TransactionsResponse.Transaction.Type.WITHDRAW]?.sumByLong {
        it.amount.toLong()
    }?.div(FROM_CENTS_TO_EUROS) ?: 0
    val revenueShare = transactionsByType[TransactionsResponse.Transaction.Type.SHARE_PAYOUT]?.sumByLong {
        it.amount.toLong()
    }?.div(FROM_CENTS_TO_EUROS) ?: 0
    val investments = transactionsByType[TransactionsResponse.Transaction.Type.INVEST]?.sumByLong {
        it.amount.toLong()
    }?.div(FROM_CENTS_TO_EUROS) ?: 0
    val sharesBought = transactionsByType[TransactionsResponse.Transaction.Type.UNRECOGNIZED]?.sumByLong {
        it.amount.toLong()
    }?.div(FROM_CENTS_TO_EUROS) ?: 0
    val sharesSold = transactionsByType[TransactionsResponse.Transaction.Type.UNRECOGNIZED]?.sumByLong {
        it.amount.toLong()
    }?.div(FROM_CENTS_TO_EUROS) ?: 0

    private fun getPeriod(transactions: List<Transaction>): String {
        return when (transactions.size) {
            0 -> ""
            1 -> formatToYearMonthDay(transactions.first().date)
            else -> {
                formatToYearMonthDay(transactions.first().date) + " to " +
                    formatToYearMonthDay(transactions.last().date)
            }
        }
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
        return getEurAmount(balance / FROM_CENTS_TO_EUROS)
    }
}

data class UserInfo(
    val userUuid: UUID,
    val firstName: String,
    val lastName: String,
    val address: String
) {
    constructor(userUuid: UUID, userWithInfo: UserWithInfoResponse) : this(
        userUuid,
        userWithInfo.user.firstName,
        userWithInfo.user.lastName,
        userWithInfo.address
    )
}

inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum = 0L
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

private fun getEurAmount(amount: Long): String {
    val decimalFormat = DecimalFormat("#,###.00")
    return "€" + decimalFormat.format(amount)
}
