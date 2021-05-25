package com.ampnet.reportservice.controller

import com.ampnet.crowdfunding.proto.TransactionInfo
import com.ampnet.crowdfunding.proto.TransactionType
import com.ampnet.projectservice.proto.ProjectResponse
import com.ampnet.reportservice.security.WithMockCrowdfundUser
import com.ampnet.userservice.proto.UserResponse
import com.ampnet.userservice.proto.UserWithInfoResponse
import com.ampnet.walletservice.proto.WalletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDate
import java.util.UUID

class ReportingControllerTest : ControllerTestBase() {

    private val reportPath = "/report/user/"
    private val transaction = "transaction"
    private val transactions = "transactions"

    private val userTransactionsPath = reportPath + transactions
    private val userTransactionPath = reportPath + transaction

    private lateinit var testContext: TestContext

    @BeforeEach
    fun init() {
        testContext = TestContext()
    }

    @Test
    @WithMockCrowdfundUser
    fun mustBeAbleToGeneratePdfForAllUserTransactions() {
        suppose("Wallet service will return wallet for the user") {
            testContext.wallet = createWalletResponse(walletUuid, userUuid)
            Mockito.`when`(walletService.getWalletsByOwner(listOf(userUuid)))
                .thenReturn(listOf(testContext.wallet))
        }
        suppose("Blockchain service will return transactions for wallet") {
            testContext.transactions = createTransactionsResponse()
            Mockito.`when`(blockchainService.getTransactions(testContext.wallet.hash))
                .thenReturn(testContext.transactions)
        }
        suppose("Wallet service will return wallets for hashes") {
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
        suppose("User service will return userWithInfo") {
            testContext.userWithInfo = createUserWithInfoResponse(userUuid)
            Mockito.`when`(userService.getUserWithInfo(userUuid))
                .thenReturn(testContext.userWithInfo)
        }

        verify("User can get pdf with all transactions") {
            val result = mockMvc.perform(
                get(userTransactionsPath)
                    .param("from", "2019-10-10")
                    .param("to", LocalDate.now().plusDays(1).toString())
            )
                .andExpect(status().isOk)
                .andReturn()

            val pdfContent = result.response.contentAsByteArray
            verifyPdfFormat(pdfContent)
            // File(getDownloadDirectory("transactions.pdf")).writeBytes(pdfContent)
        }
    }

    @Test
    @WithMockCrowdfundUser
    fun mustBeAbleToGeneratePdfForUserTransaction() {
        suppose("Wallet service will return wallet for the user") {
            testContext.wallet = createWalletResponse(walletUuid, userUuid, hash = userWalletHash)
            Mockito.`when`(walletService.getWalletsByOwner(listOf(userUuid)))
                .thenReturn(listOf(testContext.wallet))
        }
        suppose("Blockchain service will return transaction info for txHash, fromTxHash and toTxHash") {
            testContext.transaction = createTransaction(
                TransactionType.DEPOSIT,
                mintHash,
                userWalletHash,
                "1000000"
            )
            Mockito.`when`(
                blockchainService.getTransactionInfo(
                    txHash, testContext.transaction.fromTxHash, testContext.transaction.toTxHash
                )
            ).thenReturn(testContext.transaction)
        }
        suppose("Wallet service will return wallets for hashes") {
            testContext.wallets = listOf(
                createWalletResponse(UUID.randomUUID(), userUuid, hash = userWalletHash),
                createWalletResponse(UUID.randomUUID(), projectUuid, hash = projectWalletHash)
            )
            Mockito.`when`(walletService.getWalletsByHash(setOf(testContext.transaction.fromTxHash, testContext.transaction.toTxHash)))
                .thenReturn(testContext.wallets)
        }
        suppose("Project service will return a list of projects") {
            testContext.project = createProjectsResponse(projectUuid)
            Mockito.`when`(projectService.getProjects(listOf(userUuid, projectUuid)))
                .thenReturn(listOf(testContext.project))
        }
        suppose("User service will return userWithInfo") {
            testContext.userWithInfo = createUserWithInfoResponse(userUuid)
            Mockito.`when`(userService.getUserWithInfo(userUuid))
                .thenReturn(testContext.userWithInfo)
        }

        verify("User can get pdf with single transaction") {
            val result = mockMvc.perform(
                get(userTransactionPath)
                    .param("txHash", txHash)
                    .param("fromTxHash", testContext.transaction.fromTxHash)
                    .param("toTxHash", testContext.transaction.toTxHash)
            )
                .andExpect(status().isOk)
                .andReturn()

            val pdfContent = result.response.contentAsByteArray
            verifyPdfFormat(pdfContent)
            // File(getDownloadDirectory("transaction.pdf")).writeBytes(pdfContent)
        }
    }

    private class TestContext {
        lateinit var wallet: WalletResponse
        lateinit var wallets: List<WalletResponse>
        lateinit var transactions: List<TransactionInfo>
        lateinit var transaction: TransactionInfo
        lateinit var user: UserResponse
        lateinit var project: ProjectResponse
        lateinit var userWithInfo: UserWithInfoResponse
    }
}
