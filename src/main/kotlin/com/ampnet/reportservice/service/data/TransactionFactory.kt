package com.ampnet.reportservice.service.data

import com.ampnet.crowdfunding.proto.TransactionResponse
import com.ampnet.crowdfunding.proto.TransactionState
import com.ampnet.crowdfunding.proto.TransactionType
import com.ampnet.reportservice.enums.TransactionStatusType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

const val TO_PERCENTAGE = 100.0
const val LENGTH_OF_PERCENTAGE = 8

class TransactionFactory private constructor() {
    companion object {
        fun createTransaction(transaction: TransactionResponse): Transaction? {
            if (transaction.state == TransactionState.MINED) {
                transaction.type?.let {
                    return when (it) {
                        TransactionType.DEPOSIT -> TransactionDeposit(transaction)
                        TransactionType.WITHDRAW -> TransactionWithdraw(transaction)
                        TransactionType.INVEST -> TransactionInvest(transaction)
                        TransactionType.SHARE_PAYOUT -> TransactionSharePayout(transaction)
                        TransactionType.CANCEL_INVESTMENT ->
                            TransactionCancelInvestment(transaction)
                        else -> null
                    }
                }
            }
            return null
        }
    }
}

abstract class Transaction(transaction: TransactionResponse) {

    val type: TransactionType = transaction.type
    val fromTxHash: String = transaction.fromTxHash
    val toTxHash: String = transaction.toTxHash
    val amount: Long = transaction.amount.toLong()
    val date: LocalDateTime =
        Instant.ofEpochMilli(transaction.date.toLong()).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val state: TransactionState = transaction.state
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

class TransactionInvest(transaction: TransactionResponse) : Transaction(transaction) {
    override val txStatus = TransactionStatusType.PAID_OUT
    override val name = "Investment"
}

class TransactionCancelInvestment(transaction: TransactionResponse) : Transaction(transaction) {
    override val txStatus = TransactionStatusType.PAID_IN
    override val name = "Investment cancel"
}

class TransactionSharePayout(transaction: TransactionResponse) : Transaction(transaction) {
    override val txStatus = TransactionStatusType.PAID_IN
    override val name = "Revenue share payout"
}

class TransactionDeposit(transaction: TransactionResponse) : Transaction(transaction) {
    override val txStatus = TransactionStatusType.PAID_IN
    override val name = "Deposit"
}

class TransactionWithdraw(transaction: TransactionResponse) : Transaction(transaction) {
    override val txStatus = TransactionStatusType.PAID_OUT
    override val name = "Withdraw"
}
