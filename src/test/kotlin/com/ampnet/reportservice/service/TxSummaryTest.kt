package com.ampnet.reportservice.service

import com.ampnet.crowdfunding.proto.TransactionsResponse
import com.ampnet.reportservice.TestBase
import com.ampnet.reportservice.controller.pojo.PeriodServiceRequest
import com.ampnet.reportservice.service.data.DATE_FORMAT
import com.ampnet.reportservice.service.data.Transaction
import com.ampnet.reportservice.service.data.TransactionFactory
import com.ampnet.reportservice.service.data.TxSummary
import com.ampnet.reportservice.service.data.UserInfo
import com.ampnet.userservice.proto.UserResponse
import com.ampnet.userservice.proto.UserWithInfoResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class TxSummaryTest : TestBase() {

    private val userUuid: UUID = UUID.fromString("89fb3b1c-9c0a-11e9-a2a3-2a2ae2dbcce4")

    @Test
    fun mustSetCorrectPeriodAndDateOfFinish() {
        val periodRequest = PeriodServiceRequest(
            LocalDate.of(2020, 7, 1),
            LocalDate.of(2020, 9, 1)
        )
        val txSummary = TxSummary(
            createTransactions().mapNotNull { it },
            UserInfo(userUuid, createUserWithInfoResponse()),
            periodRequest

        )
        assertThat(txSummary.period).isEqualTo(getPeriod(periodRequest))
        assertThat(txSummary.dateOfFinish).isEqualTo(getDateOfFinish(periodRequest))
    }

    private fun getPeriod(period: PeriodServiceRequest): String {
        return formatToYearMonthDay(period.from) + " to " + formatToYearMonthDay(period.to)
    }

    private fun getDateOfFinish(period: PeriodServiceRequest): String? {
        return formatToYearMonthDay(period.to)
    }

    private fun formatToYearMonthDay(date: LocalDate?): String? {
        return date?.format(DateTimeFormatter.ofPattern(DATE_FORMAT))
    }

    private fun createUserResponse(userUUid: UUID = userUuid): UserResponse {
        return UserResponse.newBuilder()
            .setUuid(userUUid.toString())
            .setFirstName("First")
            .setLastName("Last")
            .build()
    }

    private fun createUserWithInfoResponse(): UserWithInfoResponse {
        return UserWithInfoResponse.newBuilder()
            .setUser(createUserResponse())
            .setAddress("ZAGREB, GRAD ZAGREB, KARLOVAČKA CESTA 26 A")
            .build()
    }

    private fun createTransaction(
        date: ZonedDateTime = ZonedDateTime.now(),
        type: TransactionsResponse.Transaction.Type = TransactionsResponse.Transaction.Type.DEPOSIT,
        fromTxHash: String = "from-tx-hash",
        toTxHash: String = "to-tx-hash",
        amount: String = "700000"
    ): TransactionsResponse.Transaction {
        return TransactionsResponse.Transaction.newBuilder()
            .setType(type)
            .setFromTxHash(fromTxHash)
            .setToTxHash(toTxHash)
            .setAmount(amount)
            .setDate(date.toInstant().toEpochMilli().toString())
            .setState("MINTED")
            .build()
    }

    private fun createTransactions(): List<Transaction?> {
        return listOf(
            TransactionFactory.createTransaction(
                createTransaction(ZonedDateTime.of(2020, 10, 1, 0, 0, 0, 0, ZoneId.of("UTC")))
            ),
            TransactionFactory.createTransaction(
                createTransaction(ZonedDateTime.of(2020, 9, 1, 0, 0, 0, 0, ZoneId.of("UTC")))
            ),
            TransactionFactory.createTransaction(
                createTransaction(ZonedDateTime.of(2020, 8, 1, 0, 0, 0, 0, ZoneId.of("UTC")))
            ),
            TransactionFactory.createTransaction(
                createTransaction(ZonedDateTime.of(2020, 7, 1, 0, 0, 0, 0, ZoneId.of("UTC")))
            )
        )
    }
}
