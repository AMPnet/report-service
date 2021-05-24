package com.ampnet.reportservice.controller

import com.ampnet.reportservice.controller.pojo.XlsxType
import com.ampnet.reportservice.enums.PrivilegeType
import com.ampnet.reportservice.security.WithMockCrowdfundUser
import com.ampnet.userservice.proto.Role
import com.ampnet.walletservice.proto.WalletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.io.File
import java.time.LocalDate
import java.util.UUID

class AdminControllerTest : ControllerTestBase() {

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
                    .param("to", LocalDate.now().plusDays(2).toString())
            )
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andReturn()

            val pdfContent = result.response.contentAsByteArray
            verifyPdfFormat(pdfContent)
            // File(getDownloadDirectory("usersAccountsSummary.pdf")).writeBytes(pdfContent)
        }
    }

    @Test
    @WithMockCrowdfundUser(privileges = [PrivilegeType.PRA_PROFILE])
    fun mustBeAbleToDownloadXlsxForVerifiedUsers() {
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

        verify("Platform manager can get xlsx report") {
            val result = mockMvc.perform(
                MockMvcRequestBuilders.get("/admin/report/xlsx")
                    .param("type", XlsxType.VERIFIED.name)
            )
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andReturn()

            val content = result.response.contentAsByteArray
            File(getDownloadDirectory("test-xlsx.xlsx")).writeBytes(content)
        }
    }

    private class TestContext {
        lateinit var wallet: WalletResponse
        lateinit var secondWallet: WalletResponse
        lateinit var thirdWallet: WalletResponse
    }
}
