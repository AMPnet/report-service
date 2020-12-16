package com.ampnet.reportservice.service.data

data class SingleTransactionSummary(
    val transaction: Transaction,
    val userInfo: UserInfo,
    val translations: Translations
) {
    constructor(transaction: Transaction, userInfo: UserInfo) : this(
        transaction,
        userInfo,
        Translations.forLanguage(userInfo.language)
    )
}
