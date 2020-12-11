package com.ampnet.reportservice.service

import com.ampnet.crowdfunding.proto.TransactionInfo
import com.ampnet.crowdfunding.proto.TransactionState
import com.ampnet.crowdfunding.proto.TransactionType
import com.ampnet.reportservice.TestBase
import com.ampnet.reportservice.controller.pojo.PeriodServiceRequest
import com.ampnet.reportservice.service.data.DATE_FORMAT
import com.ampnet.reportservice.service.data.Transaction
import com.ampnet.reportservice.service.data.TransactionFactory
import com.ampnet.reportservice.service.data.TransactionsSummary
import com.ampnet.reportservice.service.data.UserInfo
import com.ampnet.reportservice.util.toMiliSeconds
import com.ampnet.userservice.proto.UserResponse
import com.ampnet.userservice.proto.UserWithInfoResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.UUID

class TransactionsSummaryTest : TestBase() {

    private val userUuid: UUID = UUID.fromString("89fb3b1c-9c0a-11e9-a2a3-2a2ae2dbcce4")

    @Test
    fun mustSetCorrectPeriodAndDateOfFinish() {
        val periodRequest = PeriodServiceRequest(
            LocalDate.of(2020, 7, 1),
            LocalDate.of(2020, 9, 1)
        )
        val txSummary = TransactionsSummary(
            createTransactions().mapNotNull { it },
            UserInfo(createUserWithInfoResponse()),
            periodRequest
        )
        assertThat(txSummary.period).isEqualTo(getPeriod(periodRequest))
        assertThat(txSummary.dateOfFinish).isEqualTo(getDateOfFinish(periodRequest))
    }

    @Test
    fun mustSetCorrectPeriodAndDateOfFinishForZeroTransactionsAndNullPeriodRequest() {
        val periodRequest = PeriodServiceRequest(null, null)
        val userInfo = UserInfo(createUserWithInfoResponse())
        val txSummary = TransactionsSummary(listOf(), userInfo, periodRequest)
        assertThat(txSummary.period).isEqualTo(getPeriodZeroTx(userInfo.createdAt))
        assertThat(txSummary.dateOfFinish).isEqualTo(getDateOfFinish(periodRequest))
    }

    private fun getPeriod(period: PeriodServiceRequest): String {
        return formatToYearMonthDay(period.from) + " - " + formatToYearMonthDay(period.to)
    }

    private fun getPeriodZeroTx(createdAt: LocalDateTime): String {
        return formatToYearMonthDay(createdAt) + " - " + formatToYearMonthDay(LocalDateTime.now())
    }

    private fun getDateOfFinish(period: PeriodServiceRequest): String {
        return if (period.to == null) formatToYearMonthDay(LocalDateTime.now())
        else formatToYearMonthDay(period.to)
    }

    private fun formatToYearMonthDay(date: LocalDateTime?): String {
        return date!!.format(DateTimeFormatter.ofPattern(DATE_FORMAT))
    }

    private fun createUserResponse(userUUid: UUID = userUuid): UserResponse {
        return UserResponse.newBuilder()
            .setUuid(userUUid.toString())
            .setFirstName("First")
            .setLastName("Last")
            .build()
    }

    private fun createUserWithInfoResponse(
        createdAt: LocalDateTime = LocalDateTime.now().minusMonths(6)
    ): UserWithInfoResponse {
        return UserWithInfoResponse.newBuilder()
            .setUser(createUserResponse())
            .setCreatedAt(createdAt.toMiliSeconds())
            .build()
    }

    private fun createTransaction(
        date: LocalDateTime = LocalDateTime.now(),
        type: TransactionType = TransactionType.DEPOSIT,
        fromTxHash: String = "from-tx-hash",
        toTxHash: String = "to-tx-hash",
        amount: String = "700000"
    ): TransactionInfo {
        return TransactionInfo.newBuilder()
            .setType(type)
            .setFromTxHash(fromTxHash)
            .setToTxHash(toTxHash)
            .setAmount(amount)
            .setDate(date.toInstant(ZoneOffset.UTC).toEpochMilli().toString())
            .setState(TransactionState.MINED)
            .build()
    }

    private fun createTransactions(): List<Transaction?> {
        return listOf(
            TransactionFactory.createTransaction(
                createTransaction(LocalDateTime.of(2020, 10, 1, 0, 0, 0, 0))
            ),
            TransactionFactory.createTransaction(
                createTransaction(LocalDateTime.of(2020, 9, 1, 0, 0, 0, 0))
            ),
            TransactionFactory.createTransaction(
                createTransaction(LocalDateTime.of(2020, 8, 1, 0, 0, 0, 0))
            ),
            TransactionFactory.createTransaction(
                createTransaction(LocalDateTime.of(2020, 7, 1, 0, 0, 0, 0))
            )
        )
    }
}
