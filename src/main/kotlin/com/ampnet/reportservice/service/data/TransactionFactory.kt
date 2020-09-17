package com.ampnet.reportservice.service.data

import com.ampnet.crowdfunding.proto.TransactionsResponse
import com.ampnet.reportservice.enums.TransactionStatusType
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

const val TO_PERCENTAGE = 100

class TransactionFactory private constructor() {
    companion object {
        fun createTransaction(transaction: TransactionsResponse.Transaction): Transaction? {
            transaction.type?.let {
                return when (it) {
                    TransactionsResponse.Transaction.Type.DEPOSIT -> TransactionDeposit(transaction)
                    TransactionsResponse.Transaction.Type.WITHDRAW -> TransactionWithdraw(transaction)
                    TransactionsResponse.Transaction.Type.INVEST -> TransactionInvest(transaction)
                    TransactionsResponse.Transaction.Type.SHARE_PAYOUT -> TransactionSharePayout(transaction)
                    TransactionsResponse.Transaction.Type.CANCEL_INVESTMENT -> TransactionCancelInvestment(transaction)
                    TransactionsResponse.Transaction.Type.UNRECOGNIZED -> null
                }
            }
            return null
        }
    }
}

abstract class Transaction(transaction: TransactionsResponse.Transaction) {

    val type: TransactionsResponse.Transaction.Type = transaction.type
    val fromTxHash: String = transaction.fromTxHash
    val toTxHash: String = transaction.toTxHash
    val amount: Long = transaction.amount.toLong()
    val date: String = transaction.date
    val state: String = transaction.state
    var txDate = formatToYearMonthDayTime(date)
    val amountInEuro: String = amount.toEurAmount()
    abstract val txStatus: TransactionStatusType
    abstract val name: String

    private fun formatToYearMonthDayTime(date: String): String {
        val pattern = "MMM dd, yyyy HH:mm"
        return DateTimeFormatter.ofPattern(pattern).format(ZonedDateTime.parse(date))
    }

    fun amountToCalculate(): Long {
        return when (txStatus) {
            TransactionStatusType.PAID_IN -> amount
            TransactionStatusType.PAID_OUT -> -amount
            TransactionStatusType.UNDEFINED -> 0
        }
    }

    var from: String? = null
    var to: String? = null
    var expectedProjectFunding: Long? = null
        set(value) {
            if (value != null && value > 0) {
                field = value
                percentageInProject = ((TO_PERCENTAGE * amount) / value).toString()
            }
        }
    var percentageInProject: String? = null
}

class TransactionInvest(transaction: TransactionsResponse.Transaction) : Transaction(transaction) {
    override val txStatus = TransactionStatusType.PAID_OUT
    override val name = "Investment"
}

class TransactionCancelInvestment(transaction: TransactionsResponse.Transaction) : Transaction(transaction) {
    override val txStatus = TransactionStatusType.PAID_IN
    override val name = "Investment cancel"
}

class TransactionSharePayout(transaction: TransactionsResponse.Transaction) : Transaction(transaction) {
    override val txStatus = TransactionStatusType.PAID_IN
    override val name = "Revenue share payout"
}

class TransactionDeposit(transaction: TransactionsResponse.Transaction) : Transaction(transaction) {
    override val txStatus = TransactionStatusType.PAID_IN
    override val name = "Deposit"
}

class TransactionWithdraw(transaction: TransactionsResponse.Transaction) : Transaction(transaction) {
    override val txStatus = TransactionStatusType.PAID_OUT
    override val name = "Withdraw"
}
