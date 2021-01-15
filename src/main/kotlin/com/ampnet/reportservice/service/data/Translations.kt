package com.ampnet.reportservice.service.data

data class Translations(val translations: Map<String, String>) {
    val transactions: String by translations
    val accountId: String by translations
    val deposit: String by translations
    val amount: String by translations
    val date: String by translations
    val type: String by translations
    val paidIn: String by translations
    val paidOut: String by translations
    val pending: String by translations
    val period: String by translations
    val transactionsStatement: String by translations
    val accountSummary: String by translations
    val deposits: String by translations
    val withdrawals: String by translations
    val totalRevenue: String by translations
    val totalInvestments: String by translations
    val marketplaceBought: String by translations
    val marketplaceSold: String by translations
    val totalBalanceAsOf: String by translations
    val investment: String by translations
    val investmentCancel: String by translations
    val revenueSharePayout: String by translations
    val withdraw: String by translations
}
