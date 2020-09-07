package com.ampnet.reportservice.service

import com.ampnet.crowdfunding.proto.TransactionsResponse
import com.ampnet.projectservice.proto.ProjectResponse
import com.ampnet.reportservice.service.impl.TemplateDataServiceImpl
import com.ampnet.userservice.proto.UserResponse
import com.ampnet.walletservice.proto.WalletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.util.UUID

class TemplateDataServiceTest : JpaServiceTestBase() {

    private val platformWalletName = "Platform"

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
                    mintHash, userWalletHash, "1000",
                    TransactionsResponse.Transaction.Type.DEPOSIT
                ),
                createTransaction(
                    userWalletHash, projectWalletHash, "1000",
                    TransactionsResponse.Transaction.Type.INVEST
                ),
                createTransaction(
                    projectWalletHash, userWalletHash, "1000",
                    TransactionsResponse.Transaction.Type.CANCEL_INVESTMENT
                ),
                createTransaction(
                    projectWalletHash, userWalletHash, "10",
                    TransactionsResponse.Transaction.Type.SHARE_PAYOUT
                ),
                createTransaction(
                    userWalletHash, burnHash, "10",
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
        verify("Template data service can get user transactions") {
            val user = testContext.user
            val project = testContext.project
            val transactions = templateDataService.getUserTransactionsData(userUuid).transactions
            assertThat(transactions).hasSize(5)

            val depositTx = transactions.first { it.type == TransactionsResponse.Transaction.Type.DEPOSIT }
            assertThat(depositTx.from).isEqualTo(platformWalletName)
            assertThat(depositTx.to).isEqualTo("${user.firstName} ${user.lastName}")
            val investTx = transactions.first { it.type == TransactionsResponse.Transaction.Type.INVEST }
            assertThat(investTx.from).isEqualTo("${user.firstName} ${user.lastName}")
            assertThat(investTx.to).isEqualTo(project.name)
            val cancelInvestmentTx = transactions.first { it.type == TransactionsResponse.Transaction.Type.CANCEL_INVESTMENT }
            assertThat(cancelInvestmentTx.from).isEqualTo(project.name)
            assertThat(cancelInvestmentTx.to).isEqualTo("${user.firstName} ${user.lastName}")
            val sharePayoutTx = transactions.first { it.type == TransactionsResponse.Transaction.Type.SHARE_PAYOUT }
            assertThat(sharePayoutTx.from).isEqualTo(project.name)
            assertThat(sharePayoutTx.to).isEqualTo("${user.firstName} ${user.lastName}")
            val withdrawTx = transactions.first { it.type == TransactionsResponse.Transaction.Type.WITHDRAW }
            assertThat(withdrawTx.from).isEqualTo("${user.firstName} ${user.lastName}")
            assertThat(withdrawTx.to).isEqualTo(platformWalletName)
        }
    }

    private class TestContext {
        lateinit var wallet: WalletResponse
        lateinit var wallets: List<WalletResponse>
        lateinit var transactions: List<TransactionsResponse.Transaction>
        lateinit var user: UserResponse
        lateinit var project: ProjectResponse
    }
}
