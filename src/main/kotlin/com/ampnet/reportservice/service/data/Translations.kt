package com.ampnet.reportservice.service.data

open class Translations {

    companion object {
        fun forLanguage(language: String): Translations =
            when (language) {
                "el" -> GreekTranslations()
                "es" -> SpanishTranslations()
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
        get() = "Συναλλαγές"
    override val accountId: String
        get() = "Αριθμός λογαριασμού"
    override val deposit: String
        get() = "Κατάθεση"
    override val amount: String
        get() = "Ποσό"
    override val date: String
        get() = "Ημερομηνία"
    override val type: String
        get() = "Τύπος"
    override val paidIn: String
        get() = "Πληρώθηκε σε"
    override val paidOut: String
        get() = "Εξοφλήθηκε"
    override val pending: String
        get() = "Σε εκκρεμότητα"
    override val period: String
        get() = "Περίοδος"
    override val transactionsStatement: String
        get() = "Αποδεικτικό Συναλλαγής"
    override val accountSummary: String
        get() = "Περίληψη λογαριασμού"
    override val deposits: String
        get() = "Καταθέσεις"
    override val withdrawals: String
        get() = "Αναλήψεις"
    override val totalRevenue: String
        get() = "Συνολικό μερίδιο εσόδων που ελήφθη"
    override val totalInvestments: String
        get() = "Συνολικές επενδύσεις"
    override val marketplaceBought: String
        get() = "Αγορές μεριδίων"
    override val marketplaceSold: String
        get() = "Πωλήσεις μεριδίων"
    override val totalBalanceAsOf: String
        get() = "Συνολικό υπόλοιπο από "
    override val investment: String
        get() = "Επένδυση"
    override val investmentCancel: String
        get() = "Ακύρωση Επένδυσης"
    override val revenueSharePayout: String
        get() = "Πληρωμή μεριδίου εσόδων"
    override val withdraw: String
        get() = "Απόσυρση"
}

class SpanishTranslations : Translations() {
    override val transactions: String
        get() = "Transacciones"
    override val accountId: String
        get() = "ID de la Cuenta"
    override val deposit: String
        get() = "Depósito"
    override val amount: String
        get() = "Importe"
    override val date: String
        get() = "Fecha"
    override val type: String
        get() = "Tipo"
    override val paidIn: String
        get() = "Ingresado"
    override val paidOut: String
        get() = "Desembolsado"
    override val pending: String
        get() = "Espera"
    override val period: String
        get() = "Periodo de"
    override val transactionsStatement: String
        get() = "Estado de las Transacciones"
    override val accountSummary: String
        get() = "Saldo de la Cuenta"
    override val deposits: String
        get() = "Depósitos"
    override val withdrawals: String
        get() = "Retiradas"
    override val totalRevenue: String
        get() = "Total de la cuota de ingresos recibidos"
    override val totalInvestments: String
        get() = "Total de inversiones realizadas"
    override val marketplaceBought: String
        get() = "Cuotas de mercado compradas"
    override val marketplaceSold: String
        get() = "Cuotas de mercado vendidas"
    override val totalBalanceAsOf: String
        get() = "Saldo total en "
    override val investment: String
        get() = "Inversión"
    override val investmentCancel: String
        get() = "Inversión cancelada"
    override val revenueSharePayout: String
        get() = "Pago de la cuota de ingresos"
    override val withdraw: String
        get() = "Retirar"
}
