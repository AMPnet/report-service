package com.ampnet.reportservice.controller

import com.ampnet.crowdfunding.proto.TransactionsResponse
import com.ampnet.projectservice.proto.ProjectResponse
import com.ampnet.reportservice.TestBase
import com.ampnet.reportservice.grpc.blockchain.BlockchainService
import com.ampnet.reportservice.grpc.projectservice.ProjectService
import com.ampnet.reportservice.grpc.userservice.UserService
import com.ampnet.reportservice.grpc.wallet.WalletService
import com.ampnet.userservice.proto.UserResponse
import com.ampnet.userservice.proto.UserWithInfoResponse
import com.ampnet.walletservice.proto.WalletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation
import org.springframework.restdocs.operation.preprocess.Preprocessors
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.time.ZonedDateTime
import java.util.UUID

@ExtendWith(value = [SpringExtension::class, RestDocumentationExtension::class])
@SpringBootTest
abstract class ControllerTestBase : TestBase() {

    protected val userUuid: UUID = UUID.fromString("89fb3b1c-9c0a-11e9-a2a3-2a2ae2dbcce4")
    protected val walletUuid: UUID = UUID.fromString("d3499ace-ee85-11ea-adc1-0242ac120002")
    protected val projectUuid: UUID = UUID.fromString("979dd8c5-765d-49a4-b64d-142a3c55f4df")
    protected val userWalletHash: String = "user wallet hash"
    protected val projectWalletHash: String = "project wallet hash"
    protected val mintHash: String = "mint"
    protected val burnHash: String = "burn"

    @MockBean
    protected lateinit var walletService: WalletService

    @MockBean
    protected lateinit var blockchainService: BlockchainService

    @MockBean
    protected lateinit var projectService: ProjectService

    @MockBean
    protected lateinit var userService: UserService

    protected lateinit var mockMvc: MockMvc

    @BeforeEach
    fun init(wac: WebApplicationContext, restDocumentation: RestDocumentationContextProvider) {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac)
            .apply<DefaultMockMvcBuilder>(SecurityMockMvcConfigurers.springSecurity())
            .apply<DefaultMockMvcBuilder>(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
            .alwaysDo<DefaultMockMvcBuilder>(
                MockMvcRestDocumentation.document(
                    "{ClassName}/{methodName}",
                    Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                    Preprocessors.preprocessResponse(Preprocessors.prettyPrint())
                )
            )
            .build()
    }

    protected fun createWalletResponse(
        uuid: UUID,
        owner: UUID,
        activationData: String = "activation data",
        type: WalletResponse.Type = WalletResponse.Type.USER,
        currency: String = "EUR",
        hash: String = "wallet hash"
    ): WalletResponse {
        return WalletResponse.newBuilder()
            .setUuid(uuid.toString())
            .setOwner(owner.toString())
            .setActivationData(activationData)
            .setType(type)
            .setCurrency(currency)
            .setHash(hash)
            .build()
    }

    protected fun createTransactionsResponse(): List<TransactionsResponse.Transaction> {
        val deposits = MutableList(3) {
            createTransaction(TransactionsResponse.Transaction.Type.DEPOSIT, mintHash, userWalletHash)
        }
        val invests = MutableList(2) {
            createTransaction(TransactionsResponse.Transaction.Type.INVEST, userWalletHash, projectWalletHash)
        }
        val withdrawals = MutableList(2) {
            createTransaction(TransactionsResponse.Transaction.Type.WITHDRAW, userWalletHash, burnHash)
        }
        val revenueShares =
            MutableList(2) {
                createTransaction(TransactionsResponse.Transaction.Type.SHARE_PAYOUT, projectWalletHash, userWalletHash)
            }
        val cancelInvestments = MutableList(2) {
            createTransaction(TransactionsResponse.Transaction.Type.CANCEL_INVESTMENT, projectWalletHash, userWalletHash)
        }
        return deposits + invests + withdrawals + revenueShares + cancelInvestments
    }

    protected fun createUserResponse(userUUID: UUID): UserResponse {
        return UserResponse.newBuilder()
            .setUuid(userUuid.toString())
            .setFirstName("First")
            .setLastName("Last")
            .build()
    }

    protected fun createUserWithInfoResponse(userUUID: UUID): UserWithInfoResponse {
        return UserWithInfoResponse.newBuilder()
            .setUser(createUserResponse(userUUID))
            .setAddress("User address")
            .build()
    }

    protected fun createProjectsResponse(projectUUID: UUID): ProjectResponse {
        return ProjectResponse.newBuilder()
            .setUuid(projectUUID.toString())
            .setName("Project name")
            .setExpectedFunding(100000000L)
            .build()
    }

    protected fun verifyPdfFormat(data: ByteArray) {
        assertThat(data.isNotEmpty()).isTrue()
        assertThat(data.size).isGreaterThan(4)

        // header
        assertThat(data[0]).isEqualTo(0x25) // %
        assertThat(data[1]).isEqualTo(0x50) // P
        assertThat(data[2]).isEqualTo(0x44) // D
        assertThat(data[3]).isEqualTo(0x46) // F
        assertThat(data[4]).isEqualTo(0x2D) // -

        // version is 1.3
        if (data[5].compareTo(0x31) == 0 && data[6].compareTo(0x2E) == 0 && data[7].compareTo(0x33) == 0) {
            // file terminator
            assertThat(data[data.size - 7]).isEqualTo(0x25) // %
            assertThat(data[data.size - 6]).isEqualTo(0x25) // %
            assertThat(data[data.size - 5]).isEqualTo(0x45) // E
            assertThat(data[data.size - 4]).isEqualTo(0x4F) // O
            assertThat(data[data.size - 3]).isEqualTo(0x46) // F
            assertThat(data[data.size - 2]).isEqualTo(0x20) // SPACE
            assertThat(data[data.size - 1]).isEqualTo(0x0A) // EOL
            return
        }

        // version is 1.4
        if (data[5].compareTo(0x31) == 0 && data[6].compareTo(0x2E) == 0 && data[7].compareTo(0x34) == 0) {
            // file terminator
            assertThat(data[data.size - 6]).isEqualTo(0x25) // %
            assertThat(data[data.size - 5]).isEqualTo(0x25) // %
            assertThat(data[data.size - 4]).isEqualTo(0x45) // E
            assertThat(data[data.size - 3]).isEqualTo(0x4F) // O
            assertThat(data[data.size - 2]).isEqualTo(0x46) // F
            assertThat(data[data.size - 1]).isEqualTo(0x0A) // EOL
            return
        }
        Assert.fail("Unsupported file format")
    }

    private fun createTransaction(
        type: TransactionsResponse.Transaction.Type,
        fromTxHash: String,
        toTxHash: String,
        amount: String = "700000"
    ): TransactionsResponse.Transaction {
        return TransactionsResponse.Transaction.newBuilder()
            .setType(type)
            .setFromTxHash(fromTxHash)
            .setToTxHash(toTxHash)
            .setAmount(amount)
            .setDate(ZonedDateTime.now().toString())
            .setState("MINTED")
            .build()
    }
}
