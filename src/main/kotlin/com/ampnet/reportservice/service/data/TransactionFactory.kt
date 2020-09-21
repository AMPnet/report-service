package com.ampnet.reportservice.service.data

import com.ampnet.crowdfunding.proto.TransactionsResponse
import com.ampnet.reportservice.enums.TransactionStatusType
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

const val TO_PERCENTAGE = 100.0
const val LENGTH_OF_PERCENTAGE = 8

class TransactionFactory private constructor() {
    companion object {
        fun createTransaction(transaction: TransactionsResponse.Transaction): Transaction? {
            if (transaction.state == TransactionsResponse.Transaction.State.MINED) {
                transaction.type?.let {
                    return when (it) {
                        TransactionsResponse.Transaction.Type.DEPOSIT -> TransactionDeposit(transaction)
                        TransactionsResponse.Transaction.Type.WITHDRAW -> TransactionWithdraw(transaction)
                        TransactionsResponse.Transaction.Type.INVEST -> TransactionInvest(transaction)
                        TransactionsResponse.Transaction.Type.SHARE_PAYOUT -> TransactionSharePayout(transaction)
                        TransactionsResponse.Transaction.Type.CANCEL_INVESTMENT ->
                            TransactionCancelInvestment(transaction)
                        TransactionsResponse.Transaction.Type.APPROVE_INVESTMENT -> {
                            // not needed in reports
                            null
                        }
                        TransactionsResponse.Transaction.Type.UNRECOGNIZED -> null
                    }
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
    val date: ZonedDateTime =
        ZonedDateTime.ofInstant(Instant.ofEpochMilli(transaction.date.toLong()), ZoneId.systemDefault())
    val state: TransactionsResponse.Transaction.State = transaction.state
    val txDate: String = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"))
    val amountInEuro: String = amount.toEurAmount()
    abstract val txStatus: TransactionStatusType
    abstract val name: String

    var description: String? = null
    var percentageInProject: String? = null

    fun setPercentageInProject(expectedProjectFunding: Long) {
        if (expectedProjectFunding > 0) {
            percentageInProject = ((TO_PERCENTAGE * amount / expectedProjectFunding))
                .toString().take(LENGTH_OF_PERCENTAGE)
        }
    }

    fun amountToCalculate(): Long {
        return when (txStatus) {
            TransactionStatusType.PAID_IN -> amount
            TransactionStatusType.PAID_OUT -> -amount
            TransactionStatusType.UNDEFINED -> 0
        }
    }
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
