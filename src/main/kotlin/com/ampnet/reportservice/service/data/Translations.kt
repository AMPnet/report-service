package com.ampnet.reportservice.service.data

data class Translations(
    val transactions: String = "Transactions",
    val accountId: String = "Account ID",
    val deposit: String = "Deposit",
    val amount: String = "Amount",
    val date: String = "Date",
    val type: String = "Type",
    val paidIn: String = "Paid In",
    val paidOut: String = "Paid Out",
    val pending: String = "Pending",

    val period: String = "Period",
    val transactionsStatement: String = "Transactions Statement",
    val accountSummary: String = "Account Summary",
    val deposits: String = "Deposits",
    val withdrawals: String = "Withdrawals",
    val totalRevenue: String = "Total revenue share received",
    val totalInvestments: String = "Total investments placed",
    val marketplaceBought: String = "Marketplace shares bought",
    val marketplaceSold: String = "Marketplace shares sold",
    val totalBalanceAsOf: String = "Total balance as of ",

    val investment: String = "Investment",
    val investmentCancel: String = "Investment cancel",
    val revenueSharePayout: String = "Revenue share payout",
    val withdraw: String = "Withdraw"
)
