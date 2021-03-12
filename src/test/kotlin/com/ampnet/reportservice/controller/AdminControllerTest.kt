package com.ampnet.reportservice.controller

import com.ampnet.reportservice.enums.PrivilegeType
import com.ampnet.reportservice.security.WithMockCrowdfundUser
import com.ampnet.userservice.proto.Role
import com.ampnet.walletservice.proto.WalletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.time.LocalDate
import java.util.UUID

class AdminControllerTest : ControllerTestBase() {

    private val usersAccountsSummary = "usersAccountsSummary"
    private val userAccountsSummaryPath = "/admin/report/user"

    private lateinit var testContext: TestContext

    @BeforeEach
    fun init() {
        testContext = TestContext()
    }

    @Test
    @WithMockCrowdfundUser(privileges = [PrivilegeType.PRA_PROFILE])
    fun mustBeAbleToGeneratePdfForAllActiveUsers() {
        suppose("User service will return a list of users") {
            val user = createUserExtendedResponse(userUuid, role = Role.PLATFORM_MANAGER)
            val secondUser = createUserExtendedResponse(secondUserUuid)
            val thirdUser = createUserExtendedResponse(thirdUserUuid)
            val coopResponse = createCoopResponse()
            val response = createUsersExtendedResponse(
                listOf(user, secondUser, thirdUser), coopResponse
            )
            Mockito.`when`(userService.getAllActiveUsers(coop))
                .thenReturn(response)
        }
        suppose("Wallet service will return wallets for the users") {
            testContext.wallet = createWalletResponse(walletUuid, userUuid, hash = "wallet_hash_1")
            testContext.secondWallet = createWalletResponse(UUID.randomUUID(), secondUserUuid, hash = "wallet_hash_2")
            testContext.thirdWallet = createWalletResponse(UUID.randomUUID(), thirdUserUuid, hash = "wallet_hash_3")
            Mockito.`when`(walletService.getWalletsByOwner(listOf(userUuid, secondUserUuid, thirdUserUuid)))
                .thenReturn(listOf(testContext.wallet, testContext.secondWallet, testContext.thirdWallet))
        }
        suppose("Blockchain service will return transactions for wallets") {
            Mockito.`when`(blockchainService.getTransactions(testContext.wallet.hash))
                .thenReturn(createTransactionsResponse())
            Mockito.`when`(blockchainService.getTransactions(testContext.secondWallet.hash))
                .thenReturn(createDeposits())
            Mockito.`when`(blockchainService.getTransactions(testContext.thirdWallet.hash))
                .thenReturn(listOf())
        }

        verify("Platform manager can get pdf with all user accounts summary") {
            val result = mockMvc.perform(
                MockMvcRequestBuilders.get(userAccountsSummaryPath)
                    .param("from", "2019-10-10")
                    .param("to", LocalDate.now().plusDays(1).toString())
            )
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andReturn()

            val pdfContent = result.response.contentAsByteArray
            verifyPdfFormat(pdfContent)
            // File(getDownloadDirectory(usersAccountsSummary)).writeBytes(pdfContent)
        }
    }

    private class TestContext {
        lateinit var wallet: WalletResponse
        lateinit var secondWallet: WalletResponse
        lateinit var thirdWallet: WalletResponse
    }
}
