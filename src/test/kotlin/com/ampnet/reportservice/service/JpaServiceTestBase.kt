package com.ampnet.reportservice.service

import com.ampnet.core.jwt.UserPrincipal
import com.ampnet.crowdfunding.proto.TransactionInfo
import com.ampnet.crowdfunding.proto.TransactionState
import com.ampnet.crowdfunding.proto.TransactionType
import com.ampnet.projectservice.proto.ProjectResponse
import com.ampnet.reportservice.TestBase
import com.ampnet.reportservice.config.JsonConfig
import com.ampnet.reportservice.grpc.blockchain.BlockchainService
import com.ampnet.reportservice.grpc.projectservice.ProjectService
import com.ampnet.reportservice.grpc.userservice.UserService
import com.ampnet.reportservice.grpc.wallet.WalletService
import com.ampnet.reportservice.service.impl.TranslationServiceImpl
import com.ampnet.reportservice.util.toMiliSeconds
import com.ampnet.userservice.proto.CoopResponse
import com.ampnet.userservice.proto.UserExtendedResponse
import com.ampnet.userservice.proto.UserResponse
import com.ampnet.userservice.proto.UserWithInfoResponse
import com.ampnet.userservice.proto.UsersExtendedResponse
import com.ampnet.walletservice.proto.WalletResponse
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.time.ZonedDateTime
import java.util.UUID

@ExtendWith(SpringExtension::class)
@Import(JsonConfig::class)
abstract class JpaServiceTestBase : TestBase() {

    protected val userUuid: UUID = UUID.fromString("89fb3b1c-9c0a-11e9-a2a3-2a2ae2dbcce4")
    protected val secondUserUuid: UUID = UUID.randomUUID()
    protected val thirdUserUuid: UUID = UUID.randomUUID()
    protected val walletUuid: UUID = UUID.fromString("d3499ace-ee85-11ea-adc1-0242ac120002")
    protected val projectUuid: UUID = UUID.fromString("979dd8c5-765d-49a4-b64d-142a3c55f4df")
    protected val userWalletHash: String = "user wallet hash"
    protected val projectWalletHash: String = "project wallet hash"
    protected val mintHash: String = "mint"
    protected val burnHash: String = "burn"
    protected val txHash: String = "tx_hash"
    protected val logo = "https://ampnet.io/assets/images/logo-amp.png"
    protected val coop = "ampnet-test"

    @Mock
    protected lateinit var walletService: WalletService

    @Mock
    protected lateinit var blockchainService: BlockchainService

    @Mock
    protected lateinit var projectService: ProjectService

    @Mock
    protected lateinit var userService: UserService

    @Autowired
    @Qualifier("camelCaseObjectMapper")
    protected lateinit var camelCaseObjectMapper: ObjectMapper

    protected val translationService: TranslationService by lazy {
        TranslationServiceImpl(camelCaseObjectMapper)
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

    protected fun createUserResponse(userUuid: UUID, language: String = "en"): UserResponse {
        return UserResponse.newBuilder()
            .setUuid(userUuid.toString())
            .setFirstName("First")
            .setLastName("Last")
            .setEmail("email@as.co")
            .setLanguage(language)
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
        date: LocalDateTime = LocalDateTime.now().minusDays(1),
        state: TransactionState = TransactionState.MINED
    ): TransactionInfo {
        return TransactionInfo.newBuilder()
            .setType(type)
            .setFromTxHash(fromTxHash)
            .setToTxHash(toTxHash)
            .setAmount(amount)
            .setDate(date.toMiliSeconds().toString())
            .setState(state)
            .build()
    }

    protected fun createUserWithInfoResponse(
        userUUID: UUID,
        language: String = "en",
        createdAt: LocalDateTime = LocalDateTime.now().minusMonths(6)
    ): UserWithInfoResponse {
        return UserWithInfoResponse.newBuilder()
            .setUser(createUserResponse(userUUID, language))
            .setCreatedAt(createdAt.toMiliSeconds())
            .build()
    }

    protected fun createUserExtendedResponse(userUUID: UUID): UserExtendedResponse =
        UserExtendedResponse.newBuilder()
            .setUuid(userUUID.toString())
            .setFirstName("first name")
            .setLastName("last Name")
            .setCreatedAt(ZonedDateTime.now().minusDays(11).toEpochSecond())
            .setLanguage("en")
            .setDateOfBirth("15.11.1991.")
            .setDocumentNumber("document number")
            .setDateOfIssue("10.03.2021")
            .setDateOfExpiry("10.07.2021")
            .setPersonalNumber("personal number")
            .build()

    protected fun createUsersExtendedResponse(
        users: List<UserExtendedResponse>,
        coopResponse: CoopResponse
    ): UsersExtendedResponse =
        UsersExtendedResponse.newBuilder()
            .addAllUsers(users)
            .setCoop(coopResponse)
            .build()

    protected fun createCoopResponse(): CoopResponse =
        CoopResponse.newBuilder()
            .setCoop(coop)
            .setName("Name")
            .setHostname("http://ampnet.io")
            .setLogo(logo)
            .build()

    protected fun createUserPrincipal(): UserPrincipal =
        UserPrincipal(userUuid, "email", "name", setOf(), true, true, coop)
}
