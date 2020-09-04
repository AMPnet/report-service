package com.ampnet.reportservice.service.pojo

import com.ampnet.crowdfunding.proto.TransactionsResponse

const val FROM_CENTS_TO_EUROS = 100L

data class Transaction(
    val type: String,
    val fromTxHash: String,
    val toTxHash: String,
    val amount: String,
    val date: String,
    val state: String
) {
    constructor(transaction: TransactionsResponse.Transaction) : this(
        transaction.type.name,
        transaction.fromTxHash,
        transaction.toTxHash,
        transaction.amount,
        transaction.date,
        transaction.state
    )
}

data class Transactions(
    val transactions: List<Transaction>

) {
    val txAmountsSum: Long = transactions.sumByLong { it.amount.toLong() / FROM_CENTS_TO_EUROS }
}

inline fun <T> Iterable<T>.sumByLong(selector: (T) -> Long): Long {
    var sum = 0L
    for (element in this) {
        sum += selector(element)
    }
    return sum
}
