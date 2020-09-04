package com.ampnet.reportservice.controller

import com.ampnet.crowdfunding.proto.TransactionsResponse
import com.ampnet.reportservice.security.WithMockCrowdfundUser
import com.ampnet.walletservice.proto.WalletResponse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class ReportingControllerTest : ControllerTestBase() {

    private val userTransactionsPath = "/report/user/transactions"

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
            Mockito.`when`(walletService.getWallet(userUuid)).thenReturn(testContext.wallet)
        }
        suppose("Blockchain service will return transactions for wallet") {
            testContext.transactions = createTransactionsResponse()
            Mockito.`when`(blockchainService.getTransactions(testContext.wallet.hash))
                .thenReturn(testContext.transactions)
        }

        verify("User can get pdf with all transactions") {
            val result = mockMvc.perform(
                get(userTransactionsPath)
            )
                .andExpect(status().isOk)
                .andReturn()

            val pdfContent = result.response.contentAsByteArray
            checkIsPDF(pdfContent)
        }
    }
}

private class TestContext {
    lateinit var wallet: WalletResponse
    lateinit var transactions: List<TransactionsResponse.Transaction>
}
