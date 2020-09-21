package com.ampnet.reportservice.service

import com.ampnet.crowdfunding.proto.TransactionsResponse
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
                type = TransactionsResponse.Transaction.Type.APPROVE_INVESTMENT
            )
        )
        assertThat(tx).isNull()
    }

    @Test
    fun mustNotCreateUnrecognizedTransaction() {
        val tx = TransactionFactory.createTransaction(
            createTransaction(
                type = TransactionsResponse.Transaction.Type.APPROVE_INVESTMENT
            )
        )
        assertThat(tx).isNull()
    }

    @Test
    fun mustNotCreatePendingTransaction() {
        val tx = TransactionFactory.createTransaction(
            createTransaction(
                type = TransactionsResponse.Transaction.Type.DEPOSIT,
                state = TransactionsResponse.Transaction.State.PENDING
            )
        )
        assertThat(tx).isNull()
    }

    @Test
    fun mustNotCreateFailedTransaction() {
        val tx = TransactionFactory.createTransaction(
            createTransaction(
                type = TransactionsResponse.Transaction.Type.DEPOSIT,
                state = TransactionsResponse.Transaction.State.FAILED
            )
        )
        assertThat(tx).isNull()
    }

    private fun createTransaction(
        type: TransactionsResponse.Transaction.Type,
        state: TransactionsResponse.Transaction.State = TransactionsResponse.Transaction.State.MINED
    ): TransactionsResponse.Transaction {
        return TransactionsResponse.Transaction.newBuilder()
            .setType(type)
            .setFromTxHash("fromTxHash")
            .setToTxHash("toTxHash")
            .setAmount("amount")
            .setDate(ZonedDateTime.now().toInstant().toEpochMilli().toString())
            .setState(state)
            .build()
    }
}
