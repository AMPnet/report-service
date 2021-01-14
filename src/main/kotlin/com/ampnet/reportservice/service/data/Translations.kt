package com.ampnet.reportservice.service.data

data class Translations(
    val transactions: String,
    val accountId: String,
    val deposit: String,
    val amount: String,
    val date: String,
    val type: String,
    val paidIn: String,
    val paidOut: String,
    val pending: String,
    val period: String,
    val transactionsStatement: String,
    val accountSummary: String,
    val deposits: String,
    val withdrawals: String,
    val totalRevenue: String,
    val totalInvestments: String,
    val marketplaceBought: String,
    val marketplaceSold: String,
    val totalBalanceAsOf: String,
    val investment: String,
    val investmentCancel: String,
    val revenueSharePayout: String,
    val withdraw: String
) {
    lateinit var language: String

    companion object {
        fun from(translations: Map<String, String>) = object {
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

            val translation = Translations(
                transactions, accountId, deposit, amount, date, type, paidIn, paidOut,
                pending, period, transactionsStatement, accountSummary, deposits, withdrawals,
                totalRevenue, totalInvestments, marketplaceBought, marketplaceSold, totalBalanceAsOf,
                investment, investmentCancel, revenueSharePayout, withdraw
            )
        }.translation
    }
}
