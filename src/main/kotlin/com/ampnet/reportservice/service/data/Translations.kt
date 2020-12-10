package com.ampnet.reportservice.service.data

data class Translations(
    val transactions: String = "X-Transactions",
    val accountId: String = "X-Account ID",
    val deposit: String = "X-Deposit",
    val amount: String = "X-Amount",
    val date: String = "X-Date",
    val type: String = "X-Type",
    val paidIn: String = "X-Paid In",
    val paidOut: String = "X-Paid Out",
    val pending: String = "X-Pending",
    val period: String = "X-Period",
    val transactionsStatement: String = "X-Transactions Statement",
    val accountSummary: String = "X-Account Summary",
    val deposits: String = "X-Deposits",
    val withdrawals: String = "X-Withdrawals",
    val totalRevenue: String = "X-Total revenue share received",
    val totalInvestments: String = "X-Total investments placed",
    val marketplaceBought: String = "X-Marketplace shares bought",
    val marketplaceSold: String = "X-Marketplace shares sold",
    val totalBalanceAsOf: String = "X-Total balance as of "
)
