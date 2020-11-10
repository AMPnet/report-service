package com.ampnet.reportservice.service

import com.ampnet.crowdfunding.proto.TransactionResponse
import com.ampnet.crowdfunding.proto.TransactionState
import com.ampnet.crowdfunding.proto.TransactionType
import com.ampnet.reportservice.TestBase
import com.ampnet.reportservice.service.data.TransactionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

class TransactionFactoryTest : TestBase() {

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
    ): TransactionResponse {
        return TransactionResponse.newBuilder()
            .setType(type)
            .setFromTxHash("fromTxHash")
            .setToTxHash("toTxHash")
            .setAmount("amount")
            .setDate(ZonedDateTime.now().toInstant().toEpochMilli().toString())
            .setState(state)
            .build()
    }
}
