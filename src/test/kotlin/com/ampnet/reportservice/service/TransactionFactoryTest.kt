package com.ampnet.reportservice.service

import com.ampnet.crowdfunding.proto.TransactionInfo
import com.ampnet.crowdfunding.proto.TransactionState
import com.ampnet.crowdfunding.proto.TransactionType
import com.ampnet.reportservice.TestBase
import com.ampnet.reportservice.service.data.TransactionFactory
import com.ampnet.reportservice.service.data.Translations
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class TransactionFactoryTest : TestBase() {

    lateinit var translations: Translations

    @BeforeEach
    fun init() {
        translations = getTranslations(userLanguage)
    }

    @Test
    fun mustNotCreateApprovedInvestmentTransaction() {
        val tx = TransactionFactory.createTransaction(
            createTransaction(
                type = TransactionType.APPROVE_INVESTMENT
            )
        )
        assertThat(tx).isNull()
    }

    @Test
    fun mustNotCreateUnrecognizedTransaction() {
        val tx = TransactionFactory.createTransaction(
            createTransaction(
                type = TransactionType.APPROVE_INVESTMENT
            )
        )
        assertThat(tx).isNull()
    }

    @Test
    fun mustNotCreatePendingTransaction() {
        val tx = TransactionFactory.createTransaction(
            createTransaction(
                type = TransactionType.DEPOSIT,
                state = TransactionState.PENDING
            )
        )
        assertThat(tx).isNull()
    }

    @Test
    fun mustNotCreateFailedTransaction() {
        val tx = TransactionFactory.createTransaction(
            createTransaction(
                type = TransactionType.DEPOSIT,
                state = TransactionState.FAILED
            )
        )
        assertThat(tx).isNull()
    }

    private fun createTransaction(
        type: TransactionType,
        state: TransactionState = TransactionState.MINED
    ): TransactionInfo {
        return TransactionInfo.newBuilder()
            .setType(type)
            .setFromTxHash("fromTxHash")
            .setToTxHash("toTxHash")
            .setAmount("amount")
            .setDate(ZonedDateTime.now().toInstant().toEpochMilli().toString())
            .setState(state)
            .build()
    }
}
