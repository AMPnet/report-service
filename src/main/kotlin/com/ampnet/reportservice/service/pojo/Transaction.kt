package com.ampnet.reportservice.service.pojo

import com.ampnet.crowdfunding.proto.TransactionsResponse

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
)