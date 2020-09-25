package com.ampnet.reportservice.service

import com.ampnet.crowdfunding.proto.TransactionType
import com.ampnet.crowdfunding.proto.TransactionsResponse
import com.ampnet.projectservice.proto.ProjectResponse
import com.ampnet.reportservice.service.data.LENGTH_OF_PERCENTAGE
import com.ampnet.reportservice.service.data.TO_PERCENTAGE
import com.ampnet.reportservice.service.data.toEurAmount
import com.ampnet.reportservice.service.impl.TemplateDataServiceImpl
import com.ampnet.userservice.proto.UserResponse
import com.ampnet.userservice.proto.UserWithInfoResponse
import com.ampnet.walletservice.proto.WalletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.UUID

class TemplateDataServiceTest : JpaServiceTestBase() {

    private lateinit var testContext: TestContext

    private val templateDataService: TemplateDataService by lazy {
        TemplateDataServiceImpl(walletService, blockchainService, userService, projectService)
    }

    @BeforeEach
    fun init() {
        testContext = TestContext()
    }

    @Test
    fun mustGenerateCorrectTxSummary() {
        suppose("Wallet service will return wallet for the user") {
            testContext.wallet = createWalletResponse(walletUuid, userUuid)
            Mockito.`when`(walletService.getWalletsByOwner(listOf(userUuid))).thenReturn(listOf(testContext.wallet))
        }
        suppose("Blockchain service will return transactions for wallet") {
            testContext.transactions = listOf(
                createTransaction(
                    mintHash, userWalletHash, testContext.deposit.toString(),
                    TransactionType.DEPOSIT
                ),
                createTransaction(
                    userWalletHash, projectWalletHash, testContext.invest.toString(),
                    TransactionType.INVEST
                ),
                createTransaction(
                    projectWalletHash, userWalletHash, testContext.cancelInvestment.toString(),
                    TransactionType.CANCEL_INVESTMENT
                ),
                createTransaction(
                    projectWalletHash, userWalletHash, testContext.sharePayout.toString(),
                    TransactionType.SHARE_PAYOUT
                ),
                createTransaction(
                    userWalletHash, burnHash, testContext.withdraw.toString(),
                    TransactionType.WITHDRAW
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
            val project = testContext.project
            val txSummary = templateDataService.getUserTransactionsData(userUuid)
            assertThat(txSummary.balance).isEqualTo(
                (
                    testContext.deposit - testContext.invest + testContext.cancelInvestment +
                        testContext.sharePayout - testContext.withdraw
                    ).toEurAmount()
            )
            assertThat(txSummary.deposits).isEqualTo(testContext.deposit.toEurAmount())
            assertThat(txSummary.withdrawals).isEqualTo(testContext.withdraw.toEurAmount())
            assertThat(txSummary.investments).isEqualTo((testContext.invest - testContext.cancelInvestment).toEurAmount())
            assertThat(txSummary.revenueShare).isEqualTo(testContext.sharePayout.toEurAmount())

            val transactions = txSummary.transactions
            assertThat(transactions).hasSize(5)
            val depositTx = transactions.first { it.type == TransactionType.DEPOSIT }
            assertThat(depositTx.description).isNull()
            assertThat(depositTx.percentageInProject).isNull()
            assertThat(depositTx.txDate).isNotBlank()
            assertThat(depositTx.amountInEuro).isEqualTo(depositTx.amount.toEurAmount())
            val investTx = transactions.first { it.type == TransactionType.INVEST }
            assertThat(investTx.description).isEqualTo(project.name)
            assertThat(investTx.percentageInProject).isEqualTo(
                getPercentageInProject(project.expectedFunding, investTx.amount)
            )
            assertThat(investTx.txDate).isNotBlank()
            assertThat(investTx.amountInEuro).isEqualTo(investTx.amount.toEurAmount())
            val cancelInvestmentTx =
                transactions.first { it.type == TransactionType.CANCEL_INVESTMENT }
            assertThat(cancelInvestmentTx.description).isEqualTo(project.name)
            assertThat(cancelInvestmentTx.percentageInProject).isEqualTo(
                getPercentageInProject(project.expectedFunding, cancelInvestmentTx.amount)
            )
            assertThat(cancelInvestmentTx.txDate).isNotBlank()
            assertThat(cancelInvestmentTx.amountInEuro).isEqualTo(cancelInvestmentTx.amount.toEurAmount())
            val sharePayoutTx = transactions.first { it.type == TransactionType.SHARE_PAYOUT }
            assertThat(sharePayoutTx.description).isEqualTo(project.name)
            assertThat(sharePayoutTx.txDate).isNotBlank()
            assertThat(sharePayoutTx.amountInEuro).isEqualTo(sharePayoutTx.amount.toEurAmount())
            val withdrawTx = transactions.first { it.type == TransactionType.WITHDRAW }
            assertThat(withdrawTx.description).isNull()
            assertThat(withdrawTx.percentageInProject).isNull()
            assertThat(withdrawTx.txDate).isNotBlank()
            assertThat(withdrawTx.amountInEuro).isEqualTo(withdrawTx.amount.toEurAmount())
        }
    }

    private fun getPercentageInProject(expectedFunding: Long?, amount: Long): String? {
        return expectedFunding?.let {
            (TO_PERCENTAGE * amount / expectedFunding).toString().take(LENGTH_OF_PERCENTAGE)
        }
    }

    private class TestContext {
        lateinit var wallet: WalletResponse
        lateinit var wallets: List<WalletResponse>
        lateinit var transactions: List<TransactionsResponse.Transaction>
        lateinit var user: UserResponse
        lateinit var project: ProjectResponse
        lateinit var userWithInfo: UserWithInfoResponse
        val deposit = 100000L
        val invest = 20000L
        val cancelInvestment = 20000L
        val sharePayout = 1000L
        val withdraw = 10000L
    }
}
