package com.ampnet.reportservice.controller

import com.ampnet.crowdfunding.proto.TransactionsResponse
import com.ampnet.projectservice.proto.ProjectResponse
import com.ampnet.reportservice.security.WithMockCrowdfundUser
import com.ampnet.userservice.proto.UserResponse
import com.ampnet.walletservice.proto.WalletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.io.File
import java.util.UUID

class ReportingControllerTest : ControllerTestBase() {

    private val userTransactionsPath = "/report/user/transactions"
    private val downloadDir = System.getProperty("user.home") + File.separator +
        "Desktop" + File.separator + "transactions.pdf"

    private lateinit var testContext: TestContext

    @BeforeEach
    fun init() {
        testContext = TestContext()
    }

    @Test
    @WithMockCrowdfundUser
    fun mustBeAbleToGeneratePdfForUserTransactions() {
        suppose("Wallet service will return wallet for the user") {
            testContext.wallet = createWalletResponse(walletUuid, userUuid)

            Mockito.`when`(walletService.getWalletsByOwner(listOf(userUuid))).thenReturn(listOf(testContext.wallet))
        }
        suppose("Blockchain service will return transactions for wallet") {
            testContext.transactions = createTransactionsResponse()
            Mockito.`when`(blockchainService.getTransactions(testContext.wallet.hash))
                .thenReturn(testContext.transactions)
        }
        suppose("Blockchain service will return wallets for hashes") {
            testContext.wallets = listOf(
                createWalletResponse(UUID.randomUUID(), userUuid, hash = fromTxHash),
                createWalletResponse(UUID.randomUUID(), projectUuid, hash = toTxHash)
            )
            Mockito.`when`(walletService.getWalletsByHash(setOf(fromTxHash, toTxHash)))
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

        verify("User can get pdf with all transactions") {
            val result = mockMvc.perform(
                get(userTransactionsPath)
            )
                .andExpect(status().isOk)
                .andReturn()

            val pdfContent = result.response.contentAsByteArray
            verifyPdfFormat(pdfContent)
            File(downloadDir).writeBytes(pdfContent)
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
