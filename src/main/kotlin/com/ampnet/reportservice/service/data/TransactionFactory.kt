package com.ampnet.reportservice.service.data

import com.ampnet.crowdfunding.proto.TransactionInfo
import com.ampnet.crowdfunding.proto.TransactionState
import com.ampnet.crowdfunding.proto.TransactionType
import com.ampnet.reportservice.enums.TransactionStatusType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

const val TO_PERCENTAGE = 100.0
const val LENGTH_OF_PERCENTAGE = 8

class TransactionFactory private constructor() {
    companion object {
        fun createTransaction(transaction: TransactionInfo, translations: Translations): Transaction? {
            if (transaction.state == TransactionState.MINED) {
                transaction.type?.let {
                    return when (it) {
                        TransactionType.DEPOSIT -> TransactionDeposit(transaction, translations)
                        TransactionType.WITHDRAW -> TransactionWithdraw(transaction, translations)
                        TransactionType.INVEST -> TransactionInvest(transaction, translations)
                        TransactionType.SHARE_PAYOUT -> TransactionSharePayout(transaction, translations)
                        TransactionType.CANCEL_INVESTMENT -> TransactionCancelInvestment(transaction, translations)
                        else -> null
                    }
                }
            }
            return null
        }
    }
}

abstract class Transaction(transaction: TransactionInfo, var translations: Translations) {

    val type: TransactionType = transaction.type
    val fromTxHash: String = transaction.fromTxHash
    val toTxHash: String = transaction.toTxHash
    val amount: Long = transaction.amount.toLong()
    val date: LocalDateTime =
        Instant.ofEpochMilli(transaction.date.toLong()).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val state: TransactionState = transaction.state
    val txDate: String
        get() = date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm").withLocale(locale))
    val amountInEuro: String = amount.toEurAmount()
    abstract val txStatus: TransactionStatusType
    abstract val name: String

    var description: String? = null
    var percentageInProject: String? = null
    var locale: Locale = Locale.ENGLISH

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

    fun setLanguage(language: String) {
        if (language.isNotBlank()) {
            this.locale = Locale.forLanguageTag(language)
        }
    }
}

class TransactionInvest(
    transaction: TransactionInfo,
    translations: Translations
) : Transaction(transaction, translations) {
    override val txStatus = TransactionStatusType.PAID_OUT
    override val name: String
        get() = translations.investment
}

class TransactionCancelInvestment(
    transaction: TransactionInfo,
    translations: Translations
) : Transaction(transaction, translations) {
    override val txStatus = TransactionStatusType.PAID_IN
    override val name: String
        get() = translations.investmentCancel
}

class TransactionSharePayout(
    transaction: TransactionInfo,
    translations: Translations
) : Transaction(transaction, translations) {
    override val txStatus = TransactionStatusType.PAID_IN
    override val name: String
        get() = translations.revenueSharePayout
}

class TransactionDeposit(
    transaction: TransactionInfo,
    translations: Translations
) : Transaction(transaction, translations) {
    override val txStatus = TransactionStatusType.PAID_IN
    override val name: String
        get() = translations.deposit
}

class TransactionWithdraw(
    transaction: TransactionInfo,
    translations: Translations
) : Transaction(transaction, translations) {
    override val txStatus = TransactionStatusType.PAID_OUT
    override val name: String
        get() = translations.withdraw
}
