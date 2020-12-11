package com.ampnet.reportservice.service.data

open class Translations {

    companion object {
        fun forLanguage(language: String): Translations =
            when (language) {
                "el" -> GreekTranslations()
                else -> Translations()
            }
    }

    open val transactions: String = "Transactions"
    open val accountId: String = "Account ID"
    open val deposit: String = "Deposit"
    open val amount: String = "Amount"
    open val date: String = "Date"
    open val type: String = "Type"
    open val paidIn: String = "Paid In"
    open val paidOut: String = "Paid Out"
    open val pending: String = "Pending"

    open val period: String = "Period"
    open val transactionsStatement: String = "Transactions Statement"
    open val accountSummary: String = "Account Summary"
    open val deposits: String = "Deposits"
    open val withdrawals: String = "Withdrawals"
    open val totalRevenue: String = "Total revenue share received"
    open val totalInvestments: String = "Total investments placed"
    open val marketplaceBought: String = "Marketplace shares bought"
    open val marketplaceSold: String = "Marketplace shares sold"
    open val totalBalanceAsOf: String = "Total balance as of "

    open val investment: String = "Investment"
    open val investmentCancel: String = "Investment cancel"
    open val revenueSharePayout: String = "Revenue share payout"
    open val withdraw: String = "Withdraw"
}

class GreekTranslations : Translations() {
    override val transactions: String
        get() = "need_translation"
}
