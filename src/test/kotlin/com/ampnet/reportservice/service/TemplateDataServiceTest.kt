package com.ampnet.reportservice.service

import com.ampnet.crowdfunding.proto.TransactionsResponse
import com.ampnet.projectservice.proto.ProjectResponse
import com.ampnet.reportservice.service.data.FROM_CENTS_TO_EUROS
import com.ampnet.reportservice.service.data.TO_PERCENTAGE
import com.ampnet.reportservice.service.impl.TemplateDataServiceImpl
import com.ampnet.userservice.proto.UserResponse
import com.ampnet.userservice.proto.UserWithInfoResponse
import com.ampnet.walletservice.proto.WalletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.text.DecimalFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class TemplateDataServiceTest : JpaServiceTestBase() {

    private lateinit var testContext: TestContext

    private val templateDataService: TemplateDataService by lazy {
        TemplateDataServiceImpl(
            walletService, blockchainService,
            userService, projectService
        )
    }

    @BeforeEach
    fun init() {
        testContext = TestContext()
    }

    @Test
    fun mustBeAbleToSetTransactionsFromToNames() {
        suppose("Wallet service will return wallet for the user") {
            testContext.wallet = createWalletResponse(walletUuid, userUuid)
            Mockito.`when`(walletService.getWalletsByOwner(listOf(userUuid))).thenReturn(listOf(testContext.wallet))
        }
        suppose("Blockchain service will return transactions for wallet") {
            testContext.transactions = listOf(
                createTransaction(
                    mintHash, userWalletHash, "100000",
                    TransactionsResponse.Transaction.Type.DEPOSIT
                ),
                createTransaction(
                    userWalletHash, projectWalletHash, "20000",
                    TransactionsResponse.Transaction.Type.INVEST
                ),
                createTransaction(
                    projectWalletHash, userWalletHash, "20000",
                    TransactionsResponse.Transaction.Type.CANCEL_INVESTMENT
                ),
                createTransaction(
                    projectWalletHash, userWalletHash, "1000",
                    TransactionsResponse.Transaction.Type.SHARE_PAYOUT
                ),
                createTransaction(
                    userWalletHash, burnHash, "10000",
                    TransactionsResponse.Transaction.Type.WITHDRAW
                )
            )
            Mockito.`when`(blockchainService.getTransactions(testContext.wallet.hash))
                .thenReturn(testContext.transactions)
        }
        suppose("Blockchain service will return wallets for hashes") {
            testContext.wallets = listOf(
                createWalletResponse(UUID.randomUUID(), userUuid, hash = userWalletHash),
                createWalletResponse(UUID.randomUUID(), projectUuid, hash = projectWalletHash)
            )
            Mockito.`when`(walletService.getWalletsByHash(setOf(mintHash, userWalletHash, projectWalletHash, burnHash)))
                .thenReturn(testContext.wallets)
        }
        suppose("User service will return a list of users") {
            testContext.user = createUserResponse(userUuid)
            Mockito.`when`(userService.getUsers(setOf(userUuid, projectUuid)))
                .thenReturn(listOf(testContext.user))
        }
        suppose("Project service will return a list of projects") {
            testContext.project = createProjectsResponse(projectUuid)
            Mockito.`when`(projectService.getProjects(listOf(userUuid, projectUuid)))
                .thenReturn(listOf(testContext.project))
        }
        suppose("User service will the userWithInfo") {
            testContext.userWithInfo = createUserWithInfoResponse(userUuid)
            Mockito.`when`(userService.getUserWithInfo(userUuid))
                .thenReturn(testContext.userWithInfo)
        }

        verify("Template data service can get user transactions") {
            val user = testContext.user
            val project = testContext.project
            val transactions = templateDataService.getUserTransactionsData(userUuid).transactions

            assertThat(transactions).hasSize(5)
            val depositTx = transactions.first { it.type == TransactionsResponse.Transaction.Type.DEPOSIT }
            assertThat(depositTx.from).isNull()
            assertThat(depositTx.to).isNull()
            assertThat(depositTx.expectedProjectFunding).isNull()
            assertThat(depositTx.percentageInProject).isNull()
            assertThat(depositTx.txDate).isEqualTo(formatToYearMonthDayTime(depositTx.date))
            assertThat(depositTx.amountInEuro).isEqualTo(getEurAmountFormatted(depositTx.amount.toLong()))
            val investTx = transactions.first { it.type == TransactionsResponse.Transaction.Type.INVEST }
            assertThat(investTx.from).isEqualTo("${user.firstName} ${user.lastName}")
            assertThat(investTx.to).isEqualTo(project.name)
            assertThat(investTx.expectedProjectFunding).isEqualTo(project.expectedFunding)
            assertThat(investTx.percentageInProject).isEqualTo(getPercentageInProject(project.expectedFunding, investTx.amount))
            assertThat(investTx.txDate).isEqualTo(formatToYearMonthDayTime(investTx.date))
            assertThat(investTx.amountInEuro).isEqualTo(getEurAmountFormatted(investTx.amount.toLong()))
            val cancelInvestmentTx = transactions.first { it.type == TransactionsResponse.Transaction.Type.CANCEL_INVESTMENT }
            assertThat(cancelInvestmentTx.from).isEqualTo(project.name)
            assertThat(cancelInvestmentTx.to).isEqualTo("${user.firstName} ${user.lastName}")
            assertThat(cancelInvestmentTx.expectedProjectFunding).isEqualTo(project.expectedFunding)
            assertThat(cancelInvestmentTx.percentageInProject).isEqualTo(getPercentageInProject(project.expectedFunding, cancelInvestmentTx.amount))
            assertThat(cancelInvestmentTx.txDate).isEqualTo(formatToYearMonthDayTime(cancelInvestmentTx.date))
            assertThat(cancelInvestmentTx.amountInEuro).isEqualTo(getEurAmountFormatted(cancelInvestmentTx.amount.toLong()))
            val sharePayoutTx = transactions.first { it.type == TransactionsResponse.Transaction.Type.SHARE_PAYOUT }
            assertThat(sharePayoutTx.from).isEqualTo(project.name)
            assertThat(sharePayoutTx.to).isEqualTo("${user.firstName} ${user.lastName}")
            assertThat(sharePayoutTx.expectedProjectFunding).isNull()
            assertThat(sharePayoutTx.percentageInProject).isNull()
            assertThat(sharePayoutTx.txDate).isEqualTo(formatToYearMonthDayTime(sharePayoutTx.date))
            assertThat(sharePayoutTx.amountInEuro).isEqualTo(getEurAmountFormatted(sharePayoutTx.amount.toLong()))
            val withdrawTx = transactions.first { it.type == TransactionsResponse.Transaction.Type.WITHDRAW }
            assertThat(withdrawTx.from).isNull()
            assertThat(withdrawTx.to).isNull()
            assertThat(withdrawTx.expectedProjectFunding).isNull()
            assertThat(withdrawTx.percentageInProject).isNull()
            assertThat(withdrawTx.txDate).isEqualTo(formatToYearMonthDayTime(withdrawTx.date))
            assertThat(withdrawTx.amountInEuro).isEqualTo(getEurAmountFormatted(withdrawTx.amount.toLong()))
        }
    }

    private fun getPercentageInProject(expectedFunding: Long?, amount: String): String? {
        return expectedFunding?.let {
            (TO_PERCENTAGE * amount.toLong() / expectedFunding).toString()
        }
    }

    private fun formatToYearMonthDayTime(date: String): String {
        val pattern = "MMM dd, yyyy HH:mm"
        return DateTimeFormatter.ofPattern(pattern).format(ZonedDateTime.parse(date))
    }

    private fun getEurAmountFormatted(amount: Long): String {
        val decimalFormat = DecimalFormat("#,##0.00")
        return decimalFormat.format(amount / FROM_CENTS_TO_EUROS)
    }

    private class TestContext {
        lateinit var wallet: WalletResponse
        lateinit var wallets: List<WalletResponse>
        lateinit var transactions: List<TransactionsResponse.Transaction>
        lateinit var user: UserResponse
        lateinit var project: ProjectResponse
        lateinit var userWithInfo: UserWithInfoResponse
    }
}
