package com.ampnet.reportservice.controller

import com.ampnet.reportservice.controller.pojo.XlsxType
import com.ampnet.reportservice.enums.PrivilegeType
import com.ampnet.reportservice.security.WithMockCrowdfundUser
import com.ampnet.reportservice.service.impl.toDateString
import com.ampnet.userservice.proto.Role
import com.ampnet.userservice.proto.UserExtendedResponse
import com.ampnet.walletservice.proto.WalletResponse
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito
import org.mockito.Mockito
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import java.io.ByteArrayInputStream
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
            testContext.users = listOf(user, secondUser, thirdUser)
            val coopResponse = createCoopResponse()
            val response = createUsersExtendedResponse(
                testContext.users, coopResponse
            )
            BDDMockito.given(userService.getAllActiveUsers(coop)).willReturn(response)
        }

        verify("Platform manager can get xlsx report") {
            val result = mockMvc.perform(
                MockMvcRequestBuilders.get("/admin/report/xlsx")
                    .param("type", XlsxType.VERIFIED.name)
            )
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andReturn()

            val content = result.response.contentAsByteArray
            val wb = WorkbookFactory.create(ByteArrayInputStream(content))
            assertThat(wb.numberOfSheets).isEqualTo(1)
            val sheet = wb.getSheetAt(0)
            assertThat(sheet.lastRowNum).isEqualTo(3)

            for (i in 1..sheet.lastRowNum) {
                verifyCellUser(sheet.getRow(i), testContext.users[i - 1])
            }

            // Uncomment to generate file locally.
            // File(getDownloadDirectory("test-xlsx.xlsx")).writeBytes(content)
        }
    }

    private fun verifyCellUser(row: Row, user: UserExtendedResponse) {
        assertThat(row.getCell(0).stringCellValue).isEqualTo(user.uuid)
        assertThat(row.getCell(1).stringCellValue).isEqualTo(user.email)
        assertThat(row.getCell(2).stringCellValue).isEqualTo(user.firstName)
        assertThat(row.getCell(3).stringCellValue).isEqualTo(user.lastName)
        assertThat(row.getCell(4).stringCellValue).isEqualTo(user.auth)
        assertThat(row.getCell(5).stringCellValue).isEqualTo(user.createdAt.toDateString())
    }

    private class TestContext {
        lateinit var wallet: WalletResponse
        lateinit var secondWallet: WalletResponse
        lateinit var thirdWallet: WalletResponse
        lateinit var users: List<UserExtendedResponse>
    }
}
