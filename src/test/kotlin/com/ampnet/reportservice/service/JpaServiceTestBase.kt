package com.ampnet.reportservice.service

import com.ampnet.crowdfunding.proto.TransactionState
import com.ampnet.crowdfunding.proto.TransactionType
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
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.ZonedDateTime
import java.util.UUID

@ExtendWith(SpringExtension::class)
abstract class JpaServiceTestBase : TestBase() {

    protected val userUuid: UUID = UUID.fromString("89fb3b1c-9c0a-11e9-a2a3-2a2ae2dbcce4")
    protected val walletUuid: UUID = UUID.fromString("d3499ace-ee85-11ea-adc1-0242ac120002")
    protected val projectUuid: UUID = UUID.fromString("979dd8c5-765d-49a4-b64d-142a3c55f4df")
    protected val userWalletHash: String = "user wallet hash"
    protected val projectWalletHash: String = "project wallet hash"
    protected val mintHash: String = "mint"
    protected val burnHash: String = "burn"

    @Mock
    protected lateinit var walletService: WalletService

    @Mock
    protected lateinit var blockchainService: BlockchainService

    @Mock
    protected lateinit var projectService: ProjectService

    @Mock
    protected lateinit var userService: UserService

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

    protected fun createUserResponse(userUuid: UUID): UserResponse {
        return UserResponse.newBuilder()
            .setUuid(userUuid.toString())
            .setFirstName("First")
            .setLastName("Last")
            .setEmail("email@as.co")
            .build()
    }

    protected fun createProjectsResponse(projectUUID: UUID): ProjectResponse {
        return ProjectResponse.newBuilder()
            .setUuid(projectUUID.toString())
            .setName("Project name")
            .setActive(true)
            .setCreatedByUser(UUID.randomUUID().toString())
            .setOrganizationUuid(UUID.randomUUID().toString())
            .setCurrency("EUR")
            .setDescription("Project description")
            .setEndDate(ZonedDateTime.now().toEpochSecond())
            .setStartDate(ZonedDateTime.now().minusDays(11).toEpochSecond())
            .setImageUrl("image-url")
            .setMaxPerUser(100000000L)
            .setMinPerUser(1000L)
            .setExpectedFunding(100000000L)
            .build()
    }

    protected fun createTransaction(
        fromTxHash: String,
        toTxHash: String,
        amount: String,
        type: TransactionType,
        date: String = ZonedDateTime.now().toInstant().toEpochMilli().toString(),
        state: TransactionState = TransactionState.MINED
    ): TransactionsResponse.Transaction {
        return TransactionsResponse.Transaction.newBuilder()
            .setType(type)
            .setFromTxHash(fromTxHash)
            .setToTxHash(toTxHash)
            .setAmount(amount)
            .setDate(date)
            .setState(state)
            .build()
    }

    protected fun createUserWithInfoResponse(userUUID: UUID): UserWithInfoResponse {
        return UserWithInfoResponse.newBuilder()
            .setUser(createUserResponse(userUUID))
            .setAddress("ZAGREB, GRAD ZAGREB, KARLOVAČKA CESTA 26 A")
            .build()
    }
}
